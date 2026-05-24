package com.cnchem.guardian.service.impl;

import com.cnchem.guardian.domain.StandardLibraryEntry;
import com.cnchem.guardian.domain.StandardLibraryVersionHistory;
import com.cnchem.guardian.dto.StandardLibraryFieldDiff;
import com.cnchem.guardian.dto.StandardLibraryItem;
import com.cnchem.guardian.dto.StandardLibraryPreviewResult;
import com.cnchem.guardian.dto.StandardLibrarySyncResult;
import com.cnchem.guardian.dto.StandardLibraryVersionItem;
import com.cnchem.guardian.repository.StandardLibraryEntryRepository;
import com.cnchem.guardian.repository.StandardLibraryVersionHistoryRepository;
import com.cnchem.guardian.service.StandardLibraryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class DefaultStandardLibraryService implements StandardLibraryService {

    private static final TypeReference<List<String>> STRING_LIST_TYPE = new TypeReference<List<String>>() {};

    private final StandardLibraryEntryRepository repository;
    private final StandardLibraryVersionHistoryRepository historyRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;
    private final String aiServiceUrl;

    public DefaultStandardLibraryService(
            StandardLibraryEntryRepository repository,
            StandardLibraryVersionHistoryRepository historyRepository,
            ObjectMapper objectMapper,
            RestTemplate restTemplate,
            @Value("${app.ai-service-url}") String aiServiceUrl) {
        this.repository = repository;
        this.historyRepository = historyRepository;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
        this.aiServiceUrl = aiServiceUrl;
    }

    @PostConstruct
    public void init() {
        if (listAll().isEmpty()) {
            seedDefaults();
        }
    }

    @Override
    public List<StandardLibraryItem> listAll() {
        List<StandardLibraryEntry> entries = repository.findAllByOrderByCodeAsc();
        List<StandardLibraryItem> items = new ArrayList<StandardLibraryItem>();
        for (StandardLibraryEntry entry : entries) {
            if (!isArchived(entry)) {
                items.add(toItem(entry));
            }
        }
        return Collections.unmodifiableList(items);
    }

    @Override
    public List<StandardLibraryItem> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return listAll();
        }
        String needle = query.trim().toLowerCase();
        List<StandardLibraryItem> result = new ArrayList<StandardLibraryItem>();
        for (StandardLibraryItem item : listAll()) {
            if (matches(item, needle)) {
                result.add(item);
            }
        }
        return result;
    }

    @Override
    public StandardLibraryItem findByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        Optional<StandardLibraryEntry> entry = repository.findByCodeIgnoreCase(code.trim());
        return entry.isPresent() && !isArchived(entry.get()) ? toItem(entry.get()) : null;
    }

    @Override
    @Transactional
    public List<StandardLibraryItem> importItems(List<StandardLibraryItem> items) {
        if (items == null || items.isEmpty()) {
            return listAll();
        }
        List<StandardLibraryItem> imported = new ArrayList<StandardLibraryItem>();
        for (StandardLibraryItem item : items) {
            if (item == null || item.getCode() == null || item.getCode().trim().isEmpty()) {
                continue;
            }
            imported.add(upsert(item, "IMPORT", "批量导入/更新"));
        }
        return imported;
    }

    @Override
    public List<StandardLibraryItem> batchImport(List<StandardLibraryItem> items) {
        return importItems(items);
    }

    @Override
    public StandardLibrarySyncResult syncToKnowledgeBase() {
        List<StandardLibraryItem> items = listAll();
        StandardLibrarySyncResult result = new StandardLibrarySyncResult();
        result.setTarget(aiServiceUrl + "/ingest-document");
        result.setTotal(items.size());
        int synced = 0;
        int failed = 0;
        for (StandardLibraryItem item : items) {
            if (item == null || trimToNull(item.getCode()) == null) {
                failed++;
                result.getFailedCodes().add("UNKNOWN");
                continue;
            }
            try {
                Map<String, Object> payload = buildKnowledgePayload(item);
                Map response = restTemplate.postForObject(aiServiceUrl + "/ingest-document", payload, Map.class);
                if (response != null) {
                    synced++;
                    result.getSyncedCodes().add(item.getCode());
                } else {
                    failed++;
                    result.getFailedCodes().add(item.getCode());
                }
            } catch (RestClientException ex) {
                failed++;
                result.getFailedCodes().add(item.getCode());
            }
        }
        result.setSynced(synced);
        result.setFailed(failed);
        result.setMessage("Synced " + synced + " standards into the AI knowledge base.");
        return result;
    }

    @Override
    public String exportLibrary(String format) {
        String normalized = format == null ? "json" : format.trim().toLowerCase();
        List<StandardLibraryItem> items = listAll();
        if ("csv".equals(normalized)) {
            return exportCsv(items);
        }
        if ("md".equals(normalized) || "markdown".equals(normalized)) {
            return exportMarkdown(items);
        }
        return exportJson(items);
    }

    @Override
    @Transactional
    public List<StandardLibraryItem> batchArchive(List<String> codes, String note) {
        return batchSwitchArchived(codes, true, note);
    }

    @Override
    @Transactional
    public List<StandardLibraryItem> batchRestore(List<String> codes, String note) {
        return batchSwitchArchived(codes, false, note);
    }

    @Override
    @Transactional
    public StandardLibraryItem updateItem(String code, StandardLibraryItem item) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        StandardLibraryEntry entry = loadActiveEntry(code.trim());
        if (entry == null) {
            return null;
        }
        StandardLibraryItem payload = item == null ? new StandardLibraryItem() : item;
        payload.setCode(code.trim());
        return upsert(payload, "UPDATE", "单条编辑更新");
    }

    @Override
    public StandardLibraryPreviewResult previewItem(String code, StandardLibraryItem item) {
        StandardLibraryPreviewResult preview = new StandardLibraryPreviewResult();
        String normalizedCode = trimToNull(code);
        preview.setCode(normalizedCode);

        StandardLibraryItem payload = item == null ? new StandardLibraryItem() : item;
        if (normalizedCode != null) {
            payload.setCode(normalizedCode);
        }

        validatePayload(payload, preview);

        StandardLibraryItem existing = normalizedCode == null ? null : findByCode(normalizedCode);
        preview.setExists(existing != null);
        if (existing != null) {
            appendDiff(preview, "name", existing.getName(), payload.getName());
            appendDiff(preview, "status", existing.getStatus(), payload.getStatus());
            appendDiff(preview, "publishDate", existing.getPublishDate(), payload.getPublishDate());
            appendDiff(preview, "implementDate", existing.getImplementDate(), payload.getImplementDate());
            appendDiff(preview, "scope", existing.getScope(), payload.getScope());
            appendDiff(preview, "summary", existing.getSummary(), payload.getSummary());
            appendDiff(preview, "tags", joinList(existing.getTags()), joinList(payload.getTags()));
            appendDiff(preview, "scenarios", joinList(existing.getScenarios()), joinList(payload.getScenarios()));
            appendDiff(preview, "sourceUrl", existing.getSourceUrl(), payload.getSourceUrl());
        } else {
            appendDiff(preview, "name", null, payload.getName());
            appendDiff(preview, "status", null, payload.getStatus());
            appendDiff(preview, "publishDate", null, payload.getPublishDate());
            appendDiff(preview, "implementDate", null, payload.getImplementDate());
            appendDiff(preview, "scope", null, payload.getScope());
            appendDiff(preview, "summary", null, payload.getSummary());
            appendDiff(preview, "tags", null, joinList(payload.getTags()));
            appendDiff(preview, "scenarios", null, joinList(payload.getScenarios()));
            appendDiff(preview, "sourceUrl", null, payload.getSourceUrl());
        }

        preview.setValid(preview.getIssues().isEmpty());
        return preview;
    }

    @Override
    public StandardLibraryPreviewResult compareVersion(String code, Integer versionNo) {
        StandardLibraryPreviewResult preview = new StandardLibraryPreviewResult();
        String normalizedCode = trimToNull(code);
        preview.setCode(normalizedCode);
        if (normalizedCode == null) {
            preview.getIssues().add("标准号不能为空");
            preview.setValid(false);
            return preview;
        }
        if (versionNo == null) {
            preview.getIssues().add("版本号不能为空");
            preview.setValid(false);
            return preview;
        }

        StandardLibraryItem current = findByCode(normalizedCode);
        if (current == null) {
            preview.getIssues().add("未找到当前标准条目");
            preview.setValid(false);
            return preview;
        }

        StandardLibraryEntry entry = loadAnyEntry(normalizedCode);
        if (entry == null) {
            preview.getIssues().add("未找到标准历史记录");
            preview.setValid(false);
            return preview;
        }
        List<StandardLibraryVersionHistory> histories = historyRepository.findByEntryIdOrderByVersionNoDescCreatedAtDesc(entry.getId());
        for (StandardLibraryVersionHistory history : histories) {
            if (versionNo.equals(history.getVersionNo())) {
                StandardLibraryItem snapshot = readSnapshot(history.getSnapshotJson());
                if (snapshot == null) {
                    preview.getIssues().add("版本快照读取失败");
                    preview.setValid(false);
                    return preview;
                }
                preview.setExists(true);
                appendDiff(preview, "name", snapshot.getName(), current.getName());
                appendDiff(preview, "status", snapshot.getStatus(), current.getStatus());
                appendDiff(preview, "publishDate", snapshot.getPublishDate(), current.getPublishDate());
                appendDiff(preview, "implementDate", snapshot.getImplementDate(), current.getImplementDate());
                appendDiff(preview, "scope", snapshot.getScope(), current.getScope());
                appendDiff(preview, "summary", snapshot.getSummary(), current.getSummary());
                appendDiff(preview, "tags", joinList(snapshot.getTags()), joinList(current.getTags()));
                appendDiff(preview, "scenarios", joinList(snapshot.getScenarios()), joinList(current.getScenarios()));
                appendDiff(preview, "sourceUrl", snapshot.getSourceUrl(), current.getSourceUrl());
                preview.setValid(true);
                return preview;
            }
        }

        preview.getIssues().add("未找到指定版本");
        preview.setValid(false);
        return preview;
    }

    @Override
    @Transactional
    public boolean deleteByCode(String code, String note) {
        StandardLibraryEntry entry = loadActiveEntry(code);
        if (entry == null) {
            return false;
        }
        recordSnapshot(entry, "DELETE", note, entry.getCurrentVersionNo());
        entry.setArchived(Boolean.TRUE);
        entry.setDeletedAt(LocalDateTime.now());
        entry.setUpdatedAt(LocalDateTime.now());
        repository.save(entry);
        return true;
    }

    @Override
    public List<StandardLibraryVersionItem> listVersions(String code) {
        StandardLibraryEntry entry = loadAnyEntry(code);
        if (entry == null) {
            return Collections.emptyList();
        }
        List<StandardLibraryVersionHistory> histories = historyRepository.findByEntryIdOrderByVersionNoDescCreatedAtDesc(entry.getId());
        List<StandardLibraryVersionItem> result = new ArrayList<StandardLibraryVersionItem>();
        for (StandardLibraryVersionHistory history : histories) {
            StandardLibraryVersionItem versionItem = new StandardLibraryVersionItem();
            versionItem.setVersionNo(history.getVersionNo());
            versionItem.setChangeType(history.getChangeType());
            versionItem.setChangeNote(history.getChangeNote());
            versionItem.setSnapshotJson(history.getSnapshotJson());
            versionItem.setCreatedAt(history.getCreatedAt() == null ? null : history.getCreatedAt().toString());
            result.add(versionItem);
        }
        return result;
    }

    @Override
    @Transactional
    public StandardLibraryItem restoreVersion(String code, Integer versionNo) {
        StandardLibraryEntry entry = loadAnyEntry(code);
        if (entry == null || versionNo == null) {
            return null;
        }
        List<StandardLibraryVersionHistory> histories = historyRepository.findByEntryIdOrderByVersionNoDescCreatedAtDesc(entry.getId());
        for (StandardLibraryVersionHistory history : histories) {
            if (versionNo.equals(history.getVersionNo())) {
                StandardLibraryItem snapshot = readSnapshot(history.getSnapshotJson());
                if (snapshot == null) {
                    return null;
                }
                snapshot.setCode(code.trim());
                snapshot.setTenantId(entry.getTenantId());
                StandardLibraryItem restored = upsert(snapshot, "RESTORE", "恢复到版本 " + versionNo);
                return restored;
            }
        }
        return null;
    }

    @Override
    @Transactional
    public int seedDefaults() {
        return importItems(buildSeedCatalog()).size();
    }

    @Override
    public long count() {
        return repository.count();
    }

    private StandardLibraryItem upsert(StandardLibraryItem item, String changeType, String note) {
        String code = trimToNull(item.getCode());
        if (code == null) {
            return null;
        }
        StandardLibraryEntry entry = repository.findByCodeIgnoreCase(code).orElse(null);
        boolean isNew = false;
        if (entry == null) {
            entry = new StandardLibraryEntry();
            entry.setCurrentVersionNo(0);
            isNew = true;
        }
        applyItem(entry, item);
        entry.setArchived(Boolean.FALSE);
        entry.setDeletedAt(null);
        entry.setCurrentVersionNo(nextVersionNo(entry));
        repository.save(entry);
        StandardLibraryItem saved = toItem(entry);
        recordSnapshot(entry, isNew ? "CREATE" : changeType, note, entry.getCurrentVersionNo());
        return saved;
    }

    private boolean matches(StandardLibraryItem item, String needle) {
        return contains(item.getCode(), needle)
                || contains(item.getName(), needle)
                || contains(item.getSummary(), needle)
                || contains(item.getScope(), needle)
                || contains(item.getStatus(), needle)
                || containsList(item.getTags(), needle)
                || containsList(item.getScenarios(), needle);
    }

    private boolean contains(String value, String needle) {
        return value != null && value.toLowerCase().contains(needle);
    }

    private boolean containsList(List<String> values, String needle) {
        if (values == null) {
            return false;
        }
        for (String value : values) {
            if (contains(value, needle)) {
                return true;
            }
        }
        return false;
    }

    private List<StandardLibraryItem> batchSwitchArchived(List<String> codes, boolean archived, String note) {
        if (codes == null || codes.isEmpty()) {
            return Collections.emptyList();
        }
        List<StandardLibraryItem> result = new ArrayList<StandardLibraryItem>();
        for (String code : codes) {
            StandardLibraryEntry entry = loadAnyEntry(code);
            if (entry == null) {
                continue;
            }
            recordSnapshot(entry, archived ? "BATCH_ARCHIVE" : "BATCH_RESTORE", note, entry.getCurrentVersionNo());
            entry.setArchived(Boolean.valueOf(archived));
            entry.setDeletedAt(archived ? LocalDateTime.now() : null);
            entry.setUpdatedAt(LocalDateTime.now());
            repository.save(entry);
            if (!archived) {
                result.add(toItem(entry));
            } else {
                StandardLibraryItem archivedItem = toItem(entry);
                archivedItem.setArchived(Boolean.TRUE);
                result.add(archivedItem);
            }
        }
        return result;
    }

    private String exportJson(List<StandardLibraryItem> items) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(items);
        } catch (Exception ex) {
            return "[]";
        }
    }

    private String exportMarkdown(List<StandardLibraryItem> items) {
        StringBuilder builder = new StringBuilder();
        builder.append("# 标准库导出\n\n");
        for (StandardLibraryItem item : items) {
            builder.append("## ").append(nullToEmpty(item.getCode())).append(" ").append(nullToEmpty(item.getName())).append("\n");
            builder.append("- 状态：").append(nullToEmpty(item.getStatus())).append("\n");
            builder.append("- 当前版本：").append(item.getCurrentVersionNo() == null ? "-" : item.getCurrentVersionNo()).append("\n");
            builder.append("- 发布日期：").append(nullToEmpty(item.getPublishDate())).append("\n");
            builder.append("- 实施日期：").append(nullToEmpty(item.getImplementDate())).append("\n");
            builder.append("- 适用范围：").append(nullToEmpty(item.getScope())).append("\n");
            builder.append("- 摘要：").append(nullToEmpty(item.getSummary())).append("\n");
            builder.append("- 标签：").append(joinList(item.getTags())).append("\n");
            builder.append("- 场景：").append(joinList(item.getScenarios())).append("\n");
            builder.append("\n");
        }
        return builder.toString();
    }

    private String exportCsv(List<StandardLibraryItem> items) {
        StringBuilder builder = new StringBuilder();
        builder.append("code,name,status,currentVersionNo,publishDate,implementDate,scope,summary,tags,scenarios,sourceUrl\n");
        for (StandardLibraryItem item : items) {
            builder.append(csv(nullToEmpty(item.getCode()))).append(",");
            builder.append(csv(nullToEmpty(item.getName()))).append(",");
            builder.append(csv(nullToEmpty(item.getStatus()))).append(",");
            builder.append(csv(item.getCurrentVersionNo() == null ? "" : String.valueOf(item.getCurrentVersionNo()))).append(",");
            builder.append(csv(nullToEmpty(item.getPublishDate()))).append(",");
            builder.append(csv(nullToEmpty(item.getImplementDate()))).append(",");
            builder.append(csv(nullToEmpty(item.getScope()))).append(",");
            builder.append(csv(nullToEmpty(item.getSummary()))).append(",");
            builder.append(csv(joinList(item.getTags()))).append(",");
            builder.append(csv(joinList(item.getScenarios()))).append(",");
            builder.append(csv(nullToEmpty(item.getSourceUrl()))).append("\n");
        }
        return builder.toString();
    }

    private Map<String, Object> buildKnowledgePayload(StandardLibraryItem item) {
        Map<String, Object> payload = new LinkedHashMap<String, Object>();
        payload.put("document_code", item.getCode());
        payload.put("document_name", defaultString(item.getName(), item.getCode()));
        payload.put("doc_type", "GB");
        payload.put("version", item.getCurrentVersionNo() == null ? null : String.valueOf(item.getCurrentVersionNo()));
        payload.put("source_uri", item.getSourceUrl());
        payload.put("tenant_id", item.getTenantId() == null ? 1L : item.getTenantId());
        payload.put("raw_text", buildKnowledgeText(item));
        List<Map<String, Object>> chunks = new ArrayList<Map<String, Object>>();
        Map<String, Object> chunk = new LinkedHashMap<String, Object>();
        chunk.put("clause_no", "STANDARD-OVERVIEW");
        chunk.put("clause_title", defaultString(item.getName(), item.getCode()));
        chunk.put("text", buildKnowledgeText(item));
        chunks.add(chunk);
        payload.put("chunks", chunks);
        return payload;
    }

    private String buildKnowledgeText(StandardLibraryItem item) {
        StringBuilder builder = new StringBuilder();
        builder.append("标准编号: ").append(nullToEmpty(item.getCode())).append("\n");
        builder.append("标准名称: ").append(nullToEmpty(item.getName())).append("\n");
        builder.append("状态: ").append(nullToEmpty(item.getStatus())).append("\n");
        builder.append("发布日期: ").append(nullToEmpty(item.getPublishDate())).append("\n");
        builder.append("实施日期: ").append(nullToEmpty(item.getImplementDate())).append("\n");
        builder.append("适用范围: ").append(nullToEmpty(item.getScope())).append("\n");
        builder.append("摘要: ").append(nullToEmpty(item.getSummary())).append("\n");
        builder.append("标签: ").append(joinList(item.getTags())).append("\n");
        builder.append("场景: ").append(joinList(item.getScenarios())).append("\n");
        builder.append("来源: ").append(nullToEmpty(item.getSourceUrl())).append("\n");
        return builder.toString();
    }

    private String csv(String value) {
        String normalized = value == null ? "" : value.replace("\"", "\"\"");
        return "\"" + normalized + "\"";
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private void validatePayload(StandardLibraryItem item, StandardLibraryPreviewResult preview) {
        if (item == null) {
            preview.getIssues().add("条目不能为空");
            return;
        }
        if (trimToNull(item.getCode()) == null) {
            preview.getIssues().add("标准号不能为空");
        }
        if (trimToNull(item.getName()) == null) {
            preview.getIssues().add("标准名称不能为空");
        }
        if (trimToNull(item.getCode()) != null && !item.getCode().trim().toUpperCase().startsWith("GB")) {
            preview.getIssues().add("标准号建议使用 GB / GB/T 规范格式");
        }
        if (item.getTags() != null && item.getTags().size() > 20) {
            preview.getIssues().add("标签数量过多，建议控制在 20 个以内");
        }
        if (item.getScenarios() != null && item.getScenarios().size() > 20) {
            preview.getIssues().add("场景数量过多，建议控制在 20 个以内");
        }
    }

    private void appendDiff(StandardLibraryPreviewResult preview, String field, String beforeValue, String afterValue) {
        String before = normalizeText(beforeValue);
        String after = normalizeText(afterValue);
        if (equalsNullable(before, after)) {
            return;
        }
        StandardLibraryFieldDiff diff = new StandardLibraryFieldDiff();
        diff.setField(field);
        diff.setBeforeValue(before);
        diff.setAfterValue(after);
        preview.getDiffs().add(diff);
    }

    private boolean equalsNullable(String left, String right) {
        if (left == null) {
            return right == null;
        }
        return left.equals(right);
    }

    private String normalizeText(String value) {
        String trimmed = trimToNull(value);
        return trimmed == null ? null : trimmed;
    }

    private String joinList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            String normalized = trimToNull(value);
            if (normalized == null) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(normalized);
        }
        return builder.length() == 0 ? null : builder.toString();
    }

    private boolean isArchived(StandardLibraryEntry entry) {
        return entry != null && Boolean.TRUE.equals(entry.getArchived());
    }

    private StandardLibraryEntry loadActiveEntry(String code) {
        StandardLibraryEntry entry = loadAnyEntry(code);
        if (entry == null || isArchived(entry)) {
            return null;
        }
        return entry;
    }

    private StandardLibraryEntry loadAnyEntry(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        Optional<StandardLibraryEntry> entry = repository.findByCodeIgnoreCase(code.trim());
        return entry.orElse(null);
    }

    private Integer nextVersionNo(StandardLibraryEntry entry) {
        Integer current = entry.getCurrentVersionNo();
        return current == null || current.intValue() < 1 ? Integer.valueOf(1) : Integer.valueOf(current.intValue() + 1);
    }

    private void recordSnapshot(StandardLibraryEntry entry, String changeType, String note, Integer versionNo) {
        try {
            StandardLibraryVersionHistory history = new StandardLibraryVersionHistory();
            history.setEntryId(entry.getId());
            history.setVersionNo(versionNo == null ? entry.getCurrentVersionNo() : versionNo);
            history.setChangeType(changeType);
            history.setChangeNote(note);
            history.setSnapshotJson(objectMapper.writeValueAsString(toItem(entry)));
            history.setTenantId(entry.getTenantId() == null ? 1L : entry.getTenantId());
            history.setCreatedAt(LocalDateTime.now());
            historyRepository.save(history);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to record standard library history", ex);
        }
    }

    private StandardLibraryItem readSnapshot(String snapshotJson) {
        if (snapshotJson == null || snapshotJson.trim().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(snapshotJson, StandardLibraryItem.class);
        } catch (Exception ex) {
            return null;
        }
    }

    private void applyItem(StandardLibraryEntry entry, StandardLibraryItem item) {
        entry.setCode(trimToNull(item.getCode()));
        entry.setName(trimToNull(item.getName()));
        entry.setStatus(defaultString(item.getStatus(), "现行"));
        entry.setPublishDate(trimToNull(item.getPublishDate()));
        entry.setImplementDate(trimToNull(item.getImplementDate()));
        entry.setScope(trimToNull(item.getScope()));
        entry.setSummary(trimToNull(item.getSummary()));
        entry.setTagsJson(writeJson(item.getTags()));
        entry.setScenariosJson(writeJson(item.getScenarios()));
        entry.setSourceUrl(trimToNull(item.getSourceUrl()));
        entry.setTenantId(item.getTenantId() == null ? 1L : item.getTenantId());
        LocalDateTime now = LocalDateTime.now();
        if (entry.getCreatedAt() == null) {
            entry.setCreatedAt(now);
        }
        entry.setUpdatedAt(now);
    }

    private StandardLibraryItem toItem(StandardLibraryEntry entry) {
        StandardLibraryItem item = new StandardLibraryItem();
        item.setCode(entry.getCode());
        item.setName(entry.getName());
        item.setStatus(entry.getStatus());
        item.setPublishDate(entry.getPublishDate());
        item.setImplementDate(entry.getImplementDate());
        item.setScope(entry.getScope());
        item.setSummary(entry.getSummary());
        item.setTags(readJsonList(entry.getTagsJson()));
        item.setScenarios(readJsonList(entry.getScenariosJson()));
        item.setSourceUrl(entry.getSourceUrl());
        item.setTenantId(entry.getTenantId());
        item.setCurrentVersionNo(entry.getCurrentVersionNo());
        item.setArchived(entry.getArchived());
        return item;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String defaultString(String value, String fallback) {
        String trimmed = trimToNull(value);
        return trimmed == null ? fallback : trimmed;
    }

    private String writeJson(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values == null ? Collections.<String>emptyList() : values);
        } catch (JsonProcessingException ex) {
            return "[]";
        }
    }

    private List<String> readJsonList(String json) {
        if (json == null || json.trim().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            List<String> values = objectMapper.readValue(json, STRING_LIST_TYPE);
            return values == null ? Collections.<String>emptyList() : values;
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    private List<StandardLibraryItem> buildSeedCatalog() {
        List<StandardLibraryItem> items = new ArrayList<StandardLibraryItem>();

        items.add(item(
                "GB 30871-2022",
                "危险化学品企业特殊作业安全规范",
                "现行",
                "2022-03-15",
                "2022-10-01",
                "危险化学品企业的特殊作业管理",
                "重点面向特殊作业许可、审批、现场条件确认、监护、检测、隔离和应急准备。",
                Arrays.asList("特殊作业", "作业票", "监护", "检测", "审批"),
                Arrays.asList("动火作业", "受限空间", "高处作业", "临时用电", "盲板抽堵"),
                "https://std.samr.gov.cn/gb/search/gbDetailed?id=DAB6B92C0762FC96E05397BE0A0A5F84"
        ));

        items.add(item(
                "GB 45673-2025",
                "危险化学品企业安全生产标准化通用规范",
                "现行",
                "2025-04-25",
                "2025-11-01",
                "危险化学品生产企业、使用危险化学品从事生产的化工企业，以及储存危险化学品经营的企业",
                "规定企业开展安全生产标准化工作的基本要求和管理要求。",
                Arrays.asList("标准化", "迎检自查", "整改闭环", "管理体系"),
                Arrays.asList("标准化建设", "迎检", "管理制度", "台账检查"),
                "https://std.samr.gov.cn/gb/search/gbDetailed?id=33BE015BACE689EBE06397BE0A0A2966"
        ));

        items.add(item(
                "GB/T 40640.4-2021",
                "化学品管理信息化 第4部分：化学品定位系统通用规范",
                "现行",
                "2021",
                "2021",
                "采用信息化定位技术实现化学品生产储存现场作业人员定位系统的设计与建设",
                "适合人员定位系统、区域管控、现场在岗在位、应急疏散和电子围栏初始化。",
                Arrays.asList("人员定位", "电子围栏", "区域管控", "在岗在位"),
                Arrays.asList("人员定位", "应急疏散", "区域识别", "现场管控"),
                "https://std.samr.gov.cn/gb/search/gbDetailed?id=CE1E6A1DD51758F6E05397BE0A0A68DF"
        ));

        items.add(item(
                "GB 2894-2025",
                "安全色和安全标志",
                "待核实",
                null,
                null,
                "工作场所安全标识、安全警示、颜色和标志使用",
                "适合现场警示牌、区域标识、风险提示和目视化管理。",
                Arrays.asList("安全标志", "目视化", "区域标识", "警示牌"),
                Arrays.asList("现场标识", "区域管控", "迎检自查"),
                null
        ));

        items.add(item(
                "GB 2811",
                "头部防护 安全帽",
                "现行",
                null,
                null,
                "头部防护用品核查",
                "适合作业人员个体防护装备核查，常用于工地、检修、巡检和入场检查场景。",
                Arrays.asList("PPE", "安全帽", "个体防护"),
                Arrays.asList("入场检查", "检修", "巡检"),
                null
        ));

        items.add(item(
                "GB 6095",
                "安全带",
                "现行",
                null,
                null,
                "高处作业防护",
                "适用于高处作业、登高检修和临边作业的防护核查。",
                Arrays.asList("PPE", "高处作业", "安全带"),
                Arrays.asList("高处作业", "临边作业", "检修"),
                null
        ));

        items.add(item(
                "GB/T 11651",
                "个体防护装备选用规范",
                "现行",
                null,
                null,
                "个体防护装备选用",
                "适合岗位 PPE 配备核对、入场检查和作业防护建议。",
                Arrays.asList("PPE", "选用规范", "防护装备"),
                Arrays.asList("入场检查", "承包商管理", "安全交底"),
                null
        ));

        items.add(item(
                "GB 16483",
                "化学品安全技术说明书 内容和项目顺序",
                "现行",
                null,
                null,
                "化学品安全技术说明书",
                "适合 SDS/MSDS 查询、化学品信息核对和危险特性说明。",
                Arrays.asList("SDS", "MSDS", "化学品", "危险特性"),
                Arrays.asList("物料入库", "危化品接收", "应急查询"),
                null
        ));

        items.add(item(
                "GB 50016",
                "建筑设计防火规范",
                "现行",
                null,
                null,
                "建筑防火与疏散",
                "适合厂房、仓库、构筑物和疏散/防火相关问题检索。",
                Arrays.asList("防火", "建筑", "疏散", "消防"),
                Arrays.asList("厂房", "仓库", "消防检查"),
                null
        ));

        items.add(item(
                "GB 50160",
                "石油化工企业设计防火规范",
                "现行",
                null,
                null,
                "石油化工设计防火",
                "适合石化装置区、罐区、工艺区的防火间距、布置和消防问题检索。",
                Arrays.asList("石化", "防火", "装置区", "罐区"),
                Arrays.asList("装置设计", "整改评估", "迎检"),
                null
        ));

        items.add(item(
                "GB/T 50493",
                "石油化工可燃气体和有毒气体检测报警设计标准",
                "现行",
                null,
                null,
                "气体检测报警设计",
                "适合气体报警器布点、检测报警联动和现场报警核查场景。",
                Arrays.asList("气体报警", "检测", "联动", "布点"),
                Arrays.asList("受限空间", "泄漏预警", "装置巡检"),
                null
        ));

        items.add(item(
                "GB 3869",
                "体力劳动强度分级",
                "现行",
                null,
                null,
                "劳动强度与作业安排",
                "适合岗位劳动强度、作业安排和工时/负荷提示。",
                Arrays.asList("劳动强度", "作业安排", "负荷"),
                Arrays.asList("高强度作业", "人力安排"),
                null
        ));

        items.add(item(
                "GB/T 4200",
                "高温作业分级",
                "现行",
                null,
                null,
                "高温作业风险提示",
                "适合高温环境作业、夏季检修和热暴露风险提示。",
                Arrays.asList("高温", "作业分级", "热暴露"),
                Arrays.asList("夏季检修", "高温作业"),
                null
        ));

        items.add(item(
                "GB 5082",
                "起重吊运指挥信号",
                "现行",
                null,
                null,
                "起重吊运与指挥",
                "适合吊装作业、起重指挥和现场信号核查。",
                Arrays.asList("吊装", "起重", "指挥信号"),
                Arrays.asList("吊装作业", "设备检修"),
                null
        ));

        return items;
    }

    private StandardLibraryItem item(
            String code,
            String name,
            String status,
            String publishDate,
            String implementDate,
            String scope,
            String summary,
            List<String> tags,
            List<String> scenarios,
            String sourceUrl) {
        StandardLibraryItem item = new StandardLibraryItem();
        item.setCode(code);
        item.setName(name);
        item.setStatus(status);
        item.setPublishDate(publishDate);
        item.setImplementDate(implementDate);
        item.setScope(scope);
        item.setSummary(summary);
        item.setTags(tags);
        item.setScenarios(scenarios);
        item.setSourceUrl(sourceUrl);
        return item;
    }
}

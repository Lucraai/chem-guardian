package com.cnchem.guardian.controller;

import com.cnchem.guardian.dto.StandardLibraryItem;
import com.cnchem.guardian.dto.StandardLibraryBatchRequest;
import com.cnchem.guardian.dto.StandardLibraryImportRequest;
import com.cnchem.guardian.dto.StandardLibraryPreviewResult;
import com.cnchem.guardian.dto.StandardLibrarySyncResult;
import com.cnchem.guardian.dto.StandardLibraryVersionItem;
import com.cnchem.guardian.service.StandardLibraryService;
import java.util.List;
import javax.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StandardLibraryController {

    private final StandardLibraryService standardLibraryService;

    public StandardLibraryController(StandardLibraryService standardLibraryService) {
        this.standardLibraryService = standardLibraryService;
    }

    @GetMapping(value = "/api/standards/library", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<StandardLibraryItem> listAll() {
        return standardLibraryService.listAll();
    }

    @GetMapping(value = "/api/standards/library/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<StandardLibraryItem> search(@RequestParam(value = "q", required = false) String query) {
        return standardLibraryService.search(query);
    }

    @GetMapping(value = "/api/standards/library/detail", produces = MediaType.APPLICATION_JSON_VALUE)
    public StandardLibraryItem detail(@RequestParam("code") String code) {
        return standardLibraryService.findByCode(code);
    }

    @PostMapping(value = "/api/standards/library/import", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<StandardLibraryItem> importItems(@Valid @RequestBody StandardLibraryImportRequest request) {
        return standardLibraryService.importItems(request == null ? null : request.getItems());
    }

    @PostMapping(value = "/api/standards/library/batch/import", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<StandardLibraryItem> batchImport(@Valid @RequestBody StandardLibraryImportRequest request) {
        return standardLibraryService.batchImport(request == null ? null : request.getItems());
    }

    @PostMapping(value = "/api/standards/library/sync", produces = MediaType.APPLICATION_JSON_VALUE)
    public StandardLibrarySyncResult syncToKnowledgeBase() {
        return standardLibraryService.syncToKnowledgeBase();
    }

    @PutMapping(value = "/api/standards/library/detail", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public StandardLibraryItem updateItem(
            @RequestParam("code") String code,
            @RequestBody StandardLibraryItem item) {
        return standardLibraryService.updateItem(code, item);
    }

    @PostMapping(value = "/api/standards/library/preview", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public StandardLibraryPreviewResult previewItem(
            @RequestParam("code") String code,
            @RequestBody StandardLibraryItem item) {
        return standardLibraryService.previewItem(code, item);
    }

    @GetMapping(value = "/api/standards/library/compare", produces = MediaType.APPLICATION_JSON_VALUE)
    public StandardLibraryPreviewResult compareVersion(
            @RequestParam("code") String code,
            @RequestParam("versionNo") Integer versionNo) {
        return standardLibraryService.compareVersion(code, versionNo);
    }

    @GetMapping(value = "/api/standards/library/export", produces = MediaType.TEXT_PLAIN_VALUE)
    public String exportLibrary(@RequestParam(value = "format", required = false) String format) {
        return standardLibraryService.exportLibrary(format);
    }

    @GetMapping(value = "/api/standards/library/template", produces = MediaType.APPLICATION_JSON_VALUE)
    public StandardLibraryImportRequest template() {
        StandardLibraryImportRequest request = new StandardLibraryImportRequest();
        StandardLibraryItem item = new StandardLibraryItem();
        item.setCode("GB 30871-2022");
        item.setName("危险化学品企业特殊作业安全规范");
        item.setStatus("现行");
        item.setPublishDate("2022-03-15");
        item.setImplementDate("2022-10-01");
        item.setScope("危险化学品企业的特殊作业管理");
        item.setSummary("重点面向特殊作业许可、审批、现场条件确认、监护、检测、隔离和应急准备。");
        request.setItems(java.util.Collections.singletonList(item));
        return request;
    }

    @DeleteMapping(value = "/api/standards/library/detail", produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean deleteItem(
            @RequestParam("code") String code,
            @RequestParam(value = "note", required = false) String note) {
        return standardLibraryService.deleteByCode(code, note);
    }

    @PostMapping(value = "/api/standards/library/batch/archive", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<StandardLibraryItem> batchArchive(@Valid @RequestBody StandardLibraryBatchRequest request) {
        return standardLibraryService.batchArchive(request == null ? null : request.getCodes(), request == null ? null : request.getNote());
    }

    @PostMapping(value = "/api/standards/library/batch/restore", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<StandardLibraryItem> batchRestore(@Valid @RequestBody StandardLibraryBatchRequest request) {
        return standardLibraryService.batchRestore(request == null ? null : request.getCodes(), request == null ? null : request.getNote());
    }

    @GetMapping(value = "/api/standards/library/versions", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<StandardLibraryVersionItem> versions(@RequestParam("code") String code) {
        return standardLibraryService.listVersions(code);
    }

    @PostMapping(value = "/api/standards/library/restore", produces = MediaType.APPLICATION_JSON_VALUE)
    public StandardLibraryItem restore(
            @RequestParam("code") String code,
            @RequestParam("versionNo") Integer versionNo) {
        return standardLibraryService.restoreVersion(code, versionNo);
    }

    @PostMapping(value = "/api/standards/library/seed", produces = MediaType.APPLICATION_JSON_VALUE)
    public long seedDefaults() {
        standardLibraryService.seedDefaults();
        return standardLibraryService.count();
    }
}

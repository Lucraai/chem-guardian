package com.cnchem.guardian.service;

import com.cnchem.guardian.dto.StandardLibraryItem;
import com.cnchem.guardian.dto.StandardLibraryBatchRequest;
import com.cnchem.guardian.dto.StandardLibraryPreviewResult;
import com.cnchem.guardian.dto.StandardLibraryVersionItem;
import com.cnchem.guardian.dto.StandardLibrarySyncResult;
import java.util.List;

public interface StandardLibraryService {
    List<StandardLibraryItem> listAll();
    List<StandardLibraryItem> search(String query);
    StandardLibraryItem findByCode(String code);
    List<StandardLibraryItem> importItems(List<StandardLibraryItem> items);
    String exportLibrary(String format);
    List<StandardLibraryItem> batchArchive(List<String> codes, String note);
    List<StandardLibraryItem> batchRestore(List<String> codes, String note);
    List<StandardLibraryItem> batchImport(List<StandardLibraryItem> items);
    StandardLibrarySyncResult syncToKnowledgeBase();
    StandardLibraryItem updateItem(String code, StandardLibraryItem item);
    StandardLibraryPreviewResult previewItem(String code, StandardLibraryItem item);
    StandardLibraryPreviewResult compareVersion(String code, Integer versionNo);
    boolean deleteByCode(String code, String note);
    List<StandardLibraryVersionItem> listVersions(String code);
    StandardLibraryItem restoreVersion(String code, Integer versionNo);
    int seedDefaults();
    long count();
}

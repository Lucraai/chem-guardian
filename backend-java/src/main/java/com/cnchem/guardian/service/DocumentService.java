package com.cnchem.guardian.service;

import com.cnchem.guardian.dto.DocumentIngestRequest;
import com.cnchem.guardian.dto.DocumentIngestResponse;
import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentService {
    DocumentIngestResponse ingest(DocumentIngestRequest request);
    DocumentIngestResponse upload(MultipartFile file,
                                   String documentCode,
                                   String documentName,
                                   String docType,
                                   String version,
                                   String sourceUri,
                                   Long tenantId) throws IOException;
}

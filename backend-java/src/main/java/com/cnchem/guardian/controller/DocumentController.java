package com.cnchem.guardian.controller;

import com.cnchem.guardian.dto.DocumentIngestRequest;
import com.cnchem.guardian.dto.DocumentIngestResponse;
import com.cnchem.guardian.service.DocumentService;
import java.io.IOException;
import javax.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Validated
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping(value = "/api/documents/ingest", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public DocumentIngestResponse ingest(@Valid @RequestBody DocumentIngestRequest request) {
        return documentService.ingest(request);
    }

    @PostMapping(value = "/api/documents/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public DocumentIngestResponse upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "document_code", required = false) String documentCode,
            @RequestParam("document_name") String documentName,
            @RequestParam("doc_type") String docType,
            @RequestParam(value = "version", required = false) String version,
            @RequestParam(value = "source_uri", required = false) String sourceUri,
            @RequestParam(value = "tenant_id", required = false, defaultValue = "1") Long tenantId
    ) throws IOException {
        return documentService.upload(file, documentCode, documentName, docType, version, sourceUri, tenantId);
    }
}

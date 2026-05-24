package com.cnchem.guardian.service.impl;

import com.cnchem.guardian.dto.DocumentIngestRequest;
import com.cnchem.guardian.dto.DocumentIngestResponse;
import com.cnchem.guardian.service.DocumentService;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DefaultDocumentService implements DocumentService {

    private final RestTemplate restTemplate;
    private final String aiServiceUrl;

    public DefaultDocumentService(RestTemplate restTemplate,
                                  @Value("${app.ai-service-url}") String aiServiceUrl) {
        this.restTemplate = restTemplate;
        this.aiServiceUrl = aiServiceUrl;
    }

    @Override
    public DocumentIngestResponse ingest(DocumentIngestRequest request) {
        try {
            return restTemplate.postForObject(
                    aiServiceUrl + "/ingest-document",
                    request,
                    DocumentIngestResponse.class
            );
        } catch (RestClientException ex) {
            DocumentIngestResponse response = new DocumentIngestResponse();
            response.setDocumentCode(request.getDocumentCode());
            response.setDocumentName(request.getDocumentName());
            response.setChunkCount(request.getChunks() == null ? 0 : request.getChunks().size());
            response.setStatus("FALLBACK");
            return response;
        }
    }

    @Override
    public DocumentIngestResponse upload(MultipartFile file,
                                         String documentCode,
                                         String documentName,
                                         String docType,
                                         String version,
                                         String sourceUri,
                                         Long tenantId) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<String, Object>();
        body.add("file", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        });
        body.add("document_name", documentName);
        body.add("doc_type", docType);
        if (documentCode != null) {
            body.add("document_code", documentCode);
        }
        if (version != null) {
            body.add("version", version);
        }
        if (sourceUri != null) {
            body.add("source_uri", sourceUri);
        }
        body.add("tenant_id", tenantId == null ? "1" : String.valueOf(tenantId));

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<MultiValueMap<String, Object>>(body, headers);
        try {
            ResponseEntity<DocumentIngestResponse> responseEntity = restTemplate.postForEntity(
                    aiServiceUrl + "/ingest-file",
                    entity,
                    DocumentIngestResponse.class
            );
            DocumentIngestResponse body = responseEntity.getBody();
            if (body != null) {
                return body;
            }
            DocumentIngestResponse response = new DocumentIngestResponse();
            response.setDocumentCode(documentCode);
            response.setDocumentName(documentName);
            response.setChunkCount(0);
            response.setStatus("EMPTY");
            return response;
        } catch (RestClientException ex) {
            DocumentIngestResponse response = new DocumentIngestResponse();
            response.setDocumentCode(documentCode);
            response.setDocumentName(documentName);
            response.setChunkCount(0);
            response.setStatus("FALLBACK");
            return response;
        }
    }
}

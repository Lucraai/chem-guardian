package com.cnchem.guardian.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

public class DocumentIngestRequest {

    @JsonProperty("document_code")
    private String documentCode;

    @JsonProperty("document_name")
    @NotBlank
    private String documentName;

    @JsonProperty("doc_type")
    @NotBlank
    private String docType;

    private String version;

    @JsonProperty("source_uri")
    private String sourceUri;

    @JsonProperty("raw_text")
    private String rawText;

    @JsonProperty("tenant_id")
    private Long tenantId = 1L;

    @Valid
    private List<DocumentChunkRequest> chunks = new ArrayList<DocumentChunkRequest>();

    public String getDocumentCode() {
        return documentCode;
    }

    public void setDocumentCode(String documentCode) {
        this.documentCode = documentCode;
    }

    public String getDocumentName() {
        return documentName;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getSourceUri() {
        return sourceUri;
    }

    public void setSourceUri(String sourceUri) {
        this.sourceUri = sourceUri;
    }

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public List<DocumentChunkRequest> getChunks() {
        return chunks;
    }

    public void setChunks(List<DocumentChunkRequest> chunks) {
        this.chunks = chunks;
    }
}


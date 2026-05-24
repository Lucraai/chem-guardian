package com.cnchem.guardian.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AdvisorSourceRef {

    @JsonProperty("document_code")
    private String documentCode;
    @JsonProperty("document_name")
    private String documentName;
    @JsonProperty("clause_no")
    private String clauseNo;
    @JsonProperty("clause_text")
    private String clauseText;
    private Double score;

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

    public String getClauseNo() {
        return clauseNo;
    }

    public void setClauseNo(String clauseNo) {
        this.clauseNo = clauseNo;
    }

    public String getClauseText() {
        return clauseText;
    }

    public void setClauseText(String clauseText) {
        this.clauseText = clauseText;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }
}

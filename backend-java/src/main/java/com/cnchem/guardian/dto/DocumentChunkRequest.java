package com.cnchem.guardian.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DocumentChunkRequest {

    @JsonProperty("clause_no")
    private String clauseNo;

    @JsonProperty("clause_title")
    private String clauseTitle;

    private String text;

    public String getClauseNo() {
        return clauseNo;
    }

    public void setClauseNo(String clauseNo) {
        this.clauseNo = clauseNo;
    }

    public String getClauseTitle() {
        return clauseTitle;
    }

    public void setClauseTitle(String clauseTitle) {
        this.clauseTitle = clauseTitle;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}


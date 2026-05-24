package com.cnchem.guardian.dto;

import java.util.List;

public class StandardLibraryBatchRequest {

    private List<String> codes;
    private String note;

    public List<String> getCodes() {
        return codes;
    }

    public void setCodes(List<String> codes) {
        this.codes = codes;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}

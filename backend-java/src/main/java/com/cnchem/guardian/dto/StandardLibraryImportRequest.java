package com.cnchem.guardian.dto;

import java.util.List;

public class StandardLibraryImportRequest {

    private List<StandardLibraryItem> items;

    public List<StandardLibraryItem> getItems() {
        return items;
    }

    public void setItems(List<StandardLibraryItem> items) {
        this.items = items;
    }
}

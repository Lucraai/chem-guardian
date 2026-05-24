package com.cnchem.guardian.dto;

import java.util.ArrayList;
import java.util.List;

public class StandardLibraryPreviewResult {

    private boolean valid;
    private boolean exists;
    private String code;
    private List<String> issues = new ArrayList<String>();
    private List<StandardLibraryFieldDiff> diffs = new ArrayList<StandardLibraryFieldDiff>();

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public boolean isExists() {
        return exists;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<String> getIssues() {
        return issues;
    }

    public void setIssues(List<String> issues) {
        this.issues = issues;
    }

    public List<StandardLibraryFieldDiff> getDiffs() {
        return diffs;
    }

    public void setDiffs(List<StandardLibraryFieldDiff> diffs) {
        this.diffs = diffs;
    }
}

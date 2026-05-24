package com.cnchem.guardian.dto;

import java.util.ArrayList;
import java.util.List;

public class StandardLibrarySyncResult {

    private int total;
    private int synced;
    private int failed;
    private String target;
    private String message;
    private final List<String> syncedCodes = new ArrayList<String>();
    private final List<String> failedCodes = new ArrayList<String>();

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getSynced() {
        return synced;
    }

    public void setSynced(int synced) {
        this.synced = synced;
    }

    public int getFailed() {
        return failed;
    }

    public void setFailed(int failed) {
        this.failed = failed;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getSyncedCodes() {
        return syncedCodes;
    }

    public List<String> getFailedCodes() {
        return failedCodes;
    }
}

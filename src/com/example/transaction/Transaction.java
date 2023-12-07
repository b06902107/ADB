package com.example.transaction;

import java.util.ArrayList;
import java.util.List;

public class Transaction {
    private String tid; // Transaction ID
    private int timestamp; // Timestamp of the transaction
    private boolean isReadOnly; // Indicates if the transaction is read-only
    private boolean isAborted; // Indicates if the transaction is aborted
    private List<Integer> visitedSites; // List of visited site IDs

    // Constructor
    public Transaction(String tid, int timestamp, boolean isReadOnly) {
        this.tid = tid;
        this.timestamp = timestamp;
        this.isReadOnly = isReadOnly;
        this.isAborted = false;
        this.visitedSites = new ArrayList<>();
    }

    // Getters and setters
    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

    public void setReadOnly(boolean readOnly) {
        isReadOnly = readOnly;
    }

    public boolean isAborted() {
        return isAborted;
    }

    public void setAborted(boolean aborted) {
        isAborted = aborted;
    }

    public List<Integer> getVisitedSites() {
        return visitedSites;
    }

    public void addVisitedSite(int siteId) {
        this.visitedSites.add(siteId);
    }
}

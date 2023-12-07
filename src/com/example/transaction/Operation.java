package com.example.transaction;

public abstract class Operation {
    private OperationType operationType;
    private String tid; // Transaction ID
    private String vid; // Variable ID

    // Constructor
    public Operation(OperationType operationType, String tid, String vid) {
        this.operationType = operationType;
        this.tid = tid;
        this.vid = vid;
    }

    // Getters
    public OperationType getOperationType() {
        return operationType;
    }

    public String getTid() {
        return tid;
    }

    public String getVid() {
        return vid;
    }
}

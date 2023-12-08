package com.example.data;

public class TemporaryValue extends Value {
    private int commitTimes;
    private String tid;

    public TemporaryValue(int value, String tid, int commitTimes) {
        super(value);
        this.tid = tid;
        this.commitTimes = commitTimes;
    }

    // Getters and setters
    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public int getCommitTimes() { return commitTimes;}

    public void setCommitTimes(int value) {
        this.commitTimes = value;
    }

}

package com.example.data;

public class CommitValue extends Value {
    private int commitTime;

    public CommitValue(int value, int commitTime) {
        super(value);
        this.commitTime = commitTime;
    }

    // Getters and setters
    public int getCommitTime() {
        return commitTime;
    }

    public void setCommitTime(int commitTime) {
        this.commitTime = commitTime;
    }
}

package com.example.data;

public class TemporaryValue extends Value {
    private String tid;

    public TemporaryValue(int value, String tid) {
        super(value);
        this.tid = tid;
    }

    // Getters and setters
    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

}

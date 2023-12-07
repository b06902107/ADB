package com.example.transaction;

public class WriteOperation extends Operation {
    private int value; // Value to be written

    // Constructor
    public WriteOperation(String tid, String vid, int value) {
        super(OperationType.W, tid, vid);
        this.value = value;
    }

    // Getter
    public int getValue() {
        return value;
    }
}

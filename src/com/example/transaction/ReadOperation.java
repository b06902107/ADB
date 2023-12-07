package com.example.transaction;

public class ReadOperation extends Operation {
    // Constructor
    public ReadOperation(String tid, String vid) {
        super(OperationType.R, tid, vid);
    }
}


package com.example.lock;

public class WriteLock extends Lock {
    public WriteLock(String tid, String vid) {
        super(tid, vid, LockType.W);
    }

    @Override
    public String toString() {
        return "(WriteLock " + tid + " " + vid + ")";
    }
}

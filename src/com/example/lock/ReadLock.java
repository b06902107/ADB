package com.example.lock;

public class ReadLock extends Lock {
    public ReadLock(String tid, String vid) {
        super(tid, vid, LockType.R);
    }

    @Override
    public String toString() {
        return "(ReadLock " + tid + " " + vid + ")";
    }
}

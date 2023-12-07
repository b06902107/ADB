package com.example.lock;

public class Lock {
    protected String tid;  // Transaction ID
    protected String vid;  // Variable ID
    protected LockType lockType;  // Either R or W

    public Lock(String tid, String vid, LockType lockType) {
        this.tid = tid;
        this.vid = vid;
        this.lockType = lockType;
    }

    // Getter and setter for tid
    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    // Getter and setter for vid
    public String getVid() {
        return vid;
    }

    public void setVid(String vid) {
        this.vid = vid;
    }

    // Getter and setter for lockType
    public LockType getLockType() {
        return lockType;
    }

    public void setLockType(LockType lockType) {
        this.lockType = lockType;
    }

    public static boolean isConflict(Lock lock1, Lock lock2) {
        if (lock1.getLockType() == LockType.R && lock2.getLockType() == LockType.R) {
            return false; // No conflict if both are read locks
        } else {
            return !lock1.getTid().equals(lock2.getTid()); // Conflict if they belong to different transactions
        }
    }

    @Override
    public String toString() {
        return "Lock{" +
                "tid='" + tid + '\'' +
                ", vid='" + vid + '\'' +
                ", lockType=" + lockType +
                '}';
    }
}

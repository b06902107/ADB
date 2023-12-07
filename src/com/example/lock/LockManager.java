package com.example.lock;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

public class LockManager {
    private String vid; // Variable ID
    private Lock currentLock; // The current lock on the variable
    private Deque<Lock> lockQueue; // Queue of locks waiting to be granted
    private Deque<String> sharedReadLock; // Transaction IDs sharing the read lock

    public LockManager(String vid) {
        this.vid = vid;
        this.currentLock = null;
        this.lockQueue = new LinkedList<>();
        this.sharedReadLock = new LinkedList<>();
    }

    public void promoteCurrentLock(WriteLock writeLock) throws LockError {
        // Promote read lock to write lock if conditions are met

        // 1. No lock
        if (currentLock == null) {
            throw new LockError("ERROR[0]: No lock on variable " + vid);
        }
        // 2. The type of current lock is not R
        if (currentLock.getLockType() != LockType.R) {
            throw new LockError("ERROR[1]: Current lock on variable " + vid + " is not a read lock can't promote.");
        }
        // 3. There are other transactions that share this R lock
        if (sharedReadLock.size() != 1) {
            throw new LockError("ERROR[2]: Other transactions are sharing the read lock on variable " + vid);
        }
        // The transaction does not hold the R lock
        if (!sharedReadLock.contains(writeLock.getTid())) {
            throw new LockError("ERROR[3]: Transaction " + writeLock.getTid() +
                    " is not holding the read lock of variable " + vid + ", can't promote.");
        }
        // Remove the current read lock from the shared read lock set, then promote it to write lock
        sharedReadLock.remove(writeLock.getTid());
        currentLock = writeLock;
        // Optionally, print the promoted lock information
        System.out.println("After promotion: " + currentLock);

    }

    public void clear() {
        // Clear
        currentLock = null;
        lockQueue.clear();
        sharedReadLock.clear();
    }

    public void shareCurrentLock(String tid) throws LockError {
        // The type of current lock is R and the transaction (tid) does not exist in sharedReadLock
        if (currentLock != null && currentLock.getLockType() == LockType.R && !sharedReadLock.contains(tid)) {
            sharedReadLock.addLast(tid);
        } else {
            throw new LockError("ERROR[4]: Transaction " + (currentLock != null ? currentLock.getTid() : "unknown") +
                    "'s current lock on variable " + vid +
                    " is a write lock, which cannot be shared.");
        }
    }

    public void releaseCurrentLock(String tid) {
        if (currentLock != null) {
            if (currentLock.getLockType() == LockType.R) {
                // For read lock
                if(sharedReadLock.contains(tid)){
                    sharedReadLock.remove(tid);
                }
                // If the list becomes empty after remove
                if (sharedReadLock.isEmpty()) {
                    currentLock = null;
                }
            }
            else {
                // For write lock
                if (currentLock.getTid().equals(tid)) {
                    currentLock = null;
                }
            }
        }
    }

    public void addLockToQueue(Lock lock) {
        for (Lock waitedLock : lockQueue) {
            if (waitedLock.getTid().equals(lock.getTid())) {
                if (waitedLock.getLockType() == lock.getLockType() || lock.getLockType() == LockType.R) {
                    return; // Do not add the lock to the queue
                }
            }
        }
        lockQueue.addLast(lock); // Add the lock to the end of the queue
    }

    public void removeLockFromQueue(String tid) {
        Iterator<Lock> iterator = lockQueue.iterator();
        while (iterator.hasNext()) {
            Lock lock = iterator.next();
            if (lock.getTid().equals(tid)) {
                iterator.remove(); // Safely remove the lock from the queue
            }
        }
    }


    public void setCurrentLock(Lock lock) {
        // Set the current lock
        if (lock.getLockType() == LockType.R) {
            sharedReadLock.addLast(lock.getTid());
        }
        currentLock = lock;
    }

    public boolean hasWriteLock() {
        for (Lock lock : lockQueue) {
            if (lock.getLockType() == LockType.W) {
                return true; // Found a Write lock in the queue
            }
        }
        return false; // No write lock found in the queue
    }


    public boolean hasOtherWriteLock(String tid) {
        for (Lock lock : lockQueue) {
            if (lock.getLockType() == LockType.W && !lock.getTid().equals(tid)) {
                return true; // Found a Write lock from a different transaction
            }
        }
        return false; // No other write lock found in the queue
    }

    // Getters and setters for the fields
    // ...
    public Lock getCurrentLock(){ return currentLock;}

    public Deque<String> getSharedReadLock() {
        return sharedReadLock;
    }

    public Deque<Lock> getLockQueue(){
        return lockQueue;
    }

    public String getVid(){
        return vid;
    }

}

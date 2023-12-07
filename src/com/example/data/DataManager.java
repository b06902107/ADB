package com.example.data;

import com.example.lock.*;

import java.util.*;

import static com.example.lock.Lock.isConflict;


public class DataManager {
    private int sid; // Site ID
    private boolean isUp; // Site status: up or down
    private Map<String, Variable> data; // Data storage: all the variables stored in this site
    private Map<String, LockManager> lockTable; // Lock managers for each variable
    private List<Integer> failTimestamp; // Fail timestamps for this site
    private List<Integer> recoverTimestamp; // Recover timestamps for this site

    public DataManager(int sid) {
        this.sid = sid;
        this.isUp = true;
        this.data = new HashMap<>();
        this.lockTable = new HashMap<>();
        this.failTimestamp = new ArrayList<>();
        this.recoverTimestamp = new ArrayList<>();

        // Initialize variables and lock managers
        for (int i = 1; i <= 20; i++) {
            String vid = "x" + i;
            int initVal = i * 10;
            boolean isReplicated = (i % 2 == 0);
            CommitValue initValue = new CommitValue(initVal, 0);

            if (isReplicated || (i % 10 + 1) == this.sid) {
                // Add variable (either replicated or not, based on the condition)
                data.put(vid, new Variable(vid, initValue, isReplicated));
            }
            // Create lock table for each variable
            lockTable.put(vid, new LockManager(vid));
        }
    }

    // Check if the DataManager has a variable with the specified ID
    public boolean hasVariable(String vid) {
        return data.containsKey(vid);
    }

    // Method to perform a snapshot read
    public ResultValue snapshotRead(String vid, int timestamp) {
        Variable v = data.get(vid);
        if (!v.isReadable()) {
            return new ResultValue(-1, false);
        } else {
            for (Value commitValue : v.getCommitValueList()) {
                if (commitValue instanceof CommitValue) {
                    CommitValue cValue = (CommitValue) commitValue;
                    if (cValue.getCommitTime() <= timestamp) {
                        if (v.isReplicated()) {
                            for (int t : failTimestamp) {
                                if (cValue.getCommitTime() < t && t <= timestamp) {
                                    return new ResultValue(-1, false);
                                }
                            }
                        }
                        return new ResultValue(cValue.getValue(), true);
                    }
                }
            }
            return new ResultValue(-1, false);
        }
    }

    public ResultValue read(String tid, String vid) throws LockError, DataError {
        Variable v = data.get(vid);

        // If the variable is not readable or does not exist, return a failed read
        if (!v.isReadable()) {
            System.out.println(tid + " failed to read " + vid + "." + sid + " [Site just recovered, not readable]");
            return new ResultValue(-1, false);
        } else {
            LockManager lockManager = lockTable.get(vid);
            Lock currentLock = lockManager.getCurrentLock();

            // If there's no lock on the variable, set a read lock then read directly
            if (currentLock == null) {
                lockManager.setCurrentLock(new ReadLock(tid, vid));
                return new ResultValue(v.getLastCommitValue(), true);
            }

            // There is a read lock on the variable
            if (currentLock.getLockType() == LockType.R) {
                // If the transaction shares the read lock, then it can read the variable
                if (lockManager.getSharedReadLock().contains(tid)) {
                    return new ResultValue(v.getLastCommitValue(), true);
                } else {
                    // The transaction doesn't share the read lock, and there are other write
                    // locks waiting in front, so the read lock should wait in queue.
                    if (lockManager.hasWriteLock()) {
                        lockManager.addLockToQueue(new ReadLock(tid, vid));
                        System.out.println(tid + " failed to read " + vid + "." + sid + " [Exist write locks waiting in front]");
                        return new ResultValue(-1, false);
                    } else {
                        // Share the current read lock and return the read value
                        lockManager.shareCurrentLock(tid);
                        return new ResultValue(v.getLastCommitValue(), true);
                    }
                }
            } else {
                // There is a Write lock on the variable
                if (tid.equals(currentLock.getTid())) {
                    // If current transaction holds the Write lock, read the temporary value
                    return new ResultValue(v.getTemporaryValue(), true);
                } else {
                    lockManager.addLockToQueue(new ReadLock(tid, vid));
                    System.out.println(tid + " failed to read " + vid + "." + sid + " [Lock conflict]");
                    return new ResultValue(-1, false);
                }
            }
        }
    }

    public boolean getWriteLock(String tid, String vid) {
        // Retrieve LockManager for the variable identified by vid
        LockManager lockManager = lockTable.get(vid);
        Lock currentLock = lockManager.getCurrentLock();

        // There is no lock on the variable currently
        if (currentLock == null) {
            return true;
        } else {
            if (currentLock.getLockType() == LockType.R) {
                if (lockManager.getSharedReadLock().size() != 1) {
                    // Multiple transactions are holding the read lock
                    lockManager.addLockToQueue(new WriteLock(tid, vid));
                    return false;
                } else {
                    // If the current lock is a read lock held only by the requesting transaction
                    if (lockManager.getSharedReadLock().contains(tid)) {
                        if (!lockManager.hasOtherWriteLock(tid)) {
                            // The transaction holds the read lock and no other write locks are waiting
                            return true;
                        } else {
                            lockManager.addLockToQueue(new WriteLock(tid, vid));
                            return false;
                        }
                    } else {
                        // Other transactions are holding the read lock
                        lockManager.addLockToQueue(new WriteLock(tid, vid));
                        return false;
                    }
                }
            } else {
                // If the current lock is type W
                // There is a Write lock on the variable
                if (currentLock.getTid().equals(tid)) {
                    return true; // The current transaction already holds the Write lock
                } else {
                    lockManager.addLockToQueue(new WriteLock(tid, vid));
                    return false;
                }
            }
        }
    }

    public void write(String tid, String vid, int value) throws LockError {
        LockManager lockManager = lockTable.get(vid);
        Variable v = data.get(vid);

        // Basic checks
        if (lockManager == null || v == null) {
            throw new IllegalStateException("LockManager or Variable not found for " + vid);
        }

        Lock currentLock = lockManager.getCurrentLock();

        if (currentLock == null) {
            lockManager.setCurrentLock(new WriteLock(tid, vid));
            v.setTemporaryValue(new TemporaryValue(value, tid));
        } else {
            if (currentLock.getLockType() == LockType.R) {
                if (lockManager.getSharedReadLock().size() == 1 &&
                        lockManager.getSharedReadLock().contains(tid) &&
                        !lockManager.hasOtherWriteLock(tid)) {

                    lockManager.promoteCurrentLock(new WriteLock(tid, vid));
                    v.setTemporaryValue(new TemporaryValue(value, tid));
                } else {
                    throw new IllegalStateException("Cannot promote read lock to write lock for " + vid);
                }
            } else {
                if (currentLock.getTid().equals(tid)) {
                    v.setTemporaryValue(new TemporaryValue(value, tid));
                } else {
                    throw new IllegalStateException("Write lock conflict for " + vid);
                }
            }
        }
    }

    public void dump() {
        String siteStatus = isUp ? "up" : "down";
        StringBuilder output = new StringBuilder("site " + sid + " [" + siteStatus + "] - ");

        ArrayList<String> sortedKeys = new ArrayList<String>(data.keySet());

        sortedKeys.sort((s1, s2) -> (Integer.parseInt(s1.substring(1)) > Integer.parseInt(s2.substring(1))) ? 1 : -1);

        for (String key : sortedKeys) {
            Variable v = data.get(key);
            output.append(v.getVid()).append(": ").append(v.getLastCommitValue()).append(", ");
        }
        System.out.println(output.toString());
    }

    public void abort(String tid) throws LockError {
        for (LockManager lockManager : lockTable.values()) {
            lockManager.releaseCurrentLock(tid);
            lockManager.removeLockFromQueue(tid);
        }
        updateLockTable();
    }

    public void commit(String tid, int commitTime) throws LockError {
        // Release locks
        for (LockManager lockManager : lockTable.values()) {
            lockManager.releaseCurrentLock(tid);
        }

        // Commit temporary values
        for (Variable v : data.values()) {
            Value tempValue = v.getTemp();
            if (tempValue != null && tempValue instanceof TemporaryValue) {
                TemporaryValue tempVal = (TemporaryValue) tempValue;
                if (tempVal.getTid().equals(tid)) {
                    int commitValue = tempVal.getValue();
                    v.addCommitValue(new CommitValue(commitValue, commitTime));
                    v.setTemporaryValue(null); // Assuming this method accepts null
                    v.setReadable(true);
                }
            }
        }
        updateLockTable();
    }

    public void updateLockTable() throws LockError {
        for (LockManager lockManager : lockTable.values()) {
            if (lockManager.getCurrentLock() != null) {
                continue; // Skip if there's already a current lock
            }

            if (lockManager.getLockQueue().isEmpty()) {
                continue; // Skip if no locks are waiting
            }

            Lock firstWaitingLock = lockManager.getLockQueue().pollFirst(); // First lock in queue
            lockManager.setCurrentLock(firstWaitingLock);

            if (firstWaitingLock.getLockType() == LockType.R && !lockManager.getLockQueue().isEmpty()) {
                Lock nextLock = lockManager.getLockQueue().peekFirst();
                while (nextLock != null && nextLock.getLockType() == LockType.R) {
                    lockManager.getSharedReadLock().add(nextLock.getTid());
                    lockManager.getLockQueue().pollFirst(); // Remove the processed read lock
                    nextLock = lockManager.getLockQueue().peekFirst();
                }

                // The next lock is either null (empty queue) or a write lock
                if (nextLock != null && lockManager.getSharedReadLock().size() == 1 &&
                        nextLock.getTid().equals(lockManager.getSharedReadLock().peekFirst())) {
                    lockManager.promoteCurrentLock(new WriteLock(nextLock.getTid(), nextLock.getVid()));
                    lockManager.getLockQueue().pollFirst(); // Remove the promoted lock
                }
            }

        }
    }

    public void fail(int timestamp) {
        this.isUp = false;
        this.failTimestamp.add(timestamp);
        for (LockManager lockManager : lockTable.values()) {
            lockManager.clear();
        }
    }

    public void recover(int timestamp) {
        this.isUp = true;
        this.recoverTimestamp.add(timestamp);
        for (Variable v : data.values()) {
            if (v.isReplicated()) {
                v.setReadable(false);
            }
        }
    }


    public Map<String, Set<String>> generateBlockingGraph() {
        Map<String, Set<String>> blockingGraph = new HashMap<>();

        for (LockManager lockManager : lockTable.values()) {
            Lock currentLock = lockManager.getCurrentLock();
            if (currentLock == null || lockManager.getLockQueue().isEmpty()) {
                continue;
            }

            for (Lock lock : lockManager.getLockQueue()) {
                if (isConflict(currentLock, lock)) {
                    if (currentLock.getLockType() == LockType.R) {
                        for (String sharedLockTid : lockManager.getSharedReadLock()) {
                            if (!sharedLockTid.equals(lock.getTid())) {
                                blockingGraph.computeIfAbsent(lock.getTid(), k -> new HashSet<>()).add(sharedLockTid);
                            }
                        }
                    } else {
                        blockingGraph.computeIfAbsent(lock.getTid(), k -> new HashSet<>()).add(currentLock.getTid());
                    }
                }
            }

            // Process conflicts within the lock queue
            List<Lock> lockQueueList = new ArrayList<>(lockManager.getLockQueue());
            for (int i = 0; i < lockQueueList.size(); i++) {
                Lock lock1 = lockQueueList.get(i);
                for (int j = 0; j < i; j++) {
                    Lock lock2 = lockQueueList.get(j);
                    if (isConflict(lock2, lock1)) {
                        blockingGraph.computeIfAbsent(lock1.getTid(), k -> new HashSet<>()).add(lock2.getTid());
                    }
                }
            }
        }


        return blockingGraph;
    }

    public boolean isUp(){
        return isUp;
    }

    public int getSid(){
        return sid;
    }

    public Map<String, LockManager> getLockTable(){
        return lockTable;
    }

}

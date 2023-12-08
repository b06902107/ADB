package com.example.data;

import java.util.*;



public class DataManager {
    private int sid; // Site ID
    private boolean isUp; // Site status: up or down
    private Map<String, Variable> data; // Data storage: all the variables stored in this site
    private List<Integer> failTimestamp; // Fail timestamps for this site
    private List<Integer> recoverTimestamp; // Recover timestamps for this site

    public DataManager(int sid) {
        this.sid = sid;
        this.isUp = true;
        this.data = new HashMap<>();
        this.failTimestamp = new ArrayList<>();
        this.recoverTimestamp = new ArrayList<>();

        // Initialize variables and lock managers
        for (int i = 1; i <= 20; i++) {
            String vid = "x" + i;
            int initVal = i * 10;
            boolean isReplicated = (i % 2 == 0);
            CommitValue initValue = new CommitValue(initVal, -1);

            if (isReplicated || (i % 10 + 1) == this.sid) {
                // Add variable (either replicated or not, based on the condition)
                data.put(vid, new Variable(vid, initValue, isReplicated));
            }
        }
    }

    // Check if the DataManager has a variable with the specified ID
    public boolean hasVariable(String vid) {
        return data.containsKey(vid);
    }

    public ResultValue read(String tid, String vid, int timestamp) throws DataError {
        Variable v = data.get(vid);

        // If the variable is not readable or does not exist, return a failed read
        if (!v.isReadable()) {
            System.out.println(tid + " failed to read " + vid + "." + sid + " [Site just recovered, not readable]");
            return new ResultValue(-1, false);
        } else {
            int lastRecoverTimestamp = -1;
            if (!recoverTimestamp.isEmpty() && v.isReplicated() && failTimestamp.get(failTimestamp.size() - 1) < timestamp) {
                lastRecoverTimestamp = recoverTimestamp.get(recoverTimestamp.size() - 1);
            }
            int val = v.getReadValue(timestamp, lastRecoverTimestamp);
            if ( val == -1 ) {
                System.out.println(tid + " failed to read " + vid + "." + sid + " [Site just recovered, not readable]");
                return new ResultValue(-1, false);
            } else {
                return new ResultValue(val, true);
            }
        }
    }

    public void write(String tid, String vid, int value) {
        Variable v = data.get(vid);
        v.setTemporaryValue(new TemporaryValue(value, tid, v.getCommitTimes()));
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

    public void abort(String tid) throws DataError {
        for (Variable v : data.values()) {
            v.removeTemporaryValue(tid);
        }
    }

    public boolean checkCommit(String tid) throws DataError {
        for (Variable v : data.values()) {
            TemporaryValue tempValue = v.getTemporaryValue(tid);
            if (tempValue != null && tempValue instanceof TemporaryValue) {
                int oldTimes = tempValue.getCommitTimes();
                int nowTimes = v.getCommitTimes();

                // System.out.println("sid: " + sid + " vid " + v.getVid() + " " + oldValue + " " + nowValue);
                if (oldTimes != nowTimes) { return false; }
            }
        }
        return true;
    }
    public void commit(String tid, int commitTime) throws DataError {
        // Commit temporary values
        for (Variable v : data.values()) {
            TemporaryValue tempValue = v.getTemporaryValue(tid);
            if (tempValue != null && tempValue instanceof TemporaryValue) {
                int commitValue = tempValue.getValue();
                v.addCommitValue(new CommitValue(commitValue, commitTime));
                v.removeTemporaryValue(tid);
                // v.setTemporaryValue(null); // Assuming this method accepts null
                v.setReadable(true);
            }
        }
    }

    public void fail(int timestamp) {
        this.isUp = false;
        this.failTimestamp.add(timestamp);
    }

    public void recover(int timestamp) {
        this.isUp = true;
        this.recoverTimestamp.add(timestamp);
        for (Variable v : data.values()) {
            if (v.isReplicated()) {
                v.setReadable(true);
            }
        }
    }

    public int getFailTimestamp() {
        return this.failTimestamp.get(this.failTimestamp.size() - 1);
    }


//    public Map<String, Set<String>> generateBlockingGraph() {
//        Map<String, Set<String>> blockingGraph = new HashMap<>();
//
//        for (LockManager lockManager : lockTable.values()) {
//            Lock currentLock = lockManager.getCurrentLock();
//            if (currentLock == null || lockManager.getLockQueue().isEmpty()) {
//                continue;
//            }
//
//            for (Lock lock : lockManager.getLockQueue()) {
//                if (isConflict(currentLock, lock)) {
//                    if (currentLock.getLockType() == LockType.R) {
//                        for (String sharedLockTid : lockManager.getSharedReadLock()) {
//                            if (!sharedLockTid.equals(lock.getTid())) {
//                                blockingGraph.computeIfAbsent(lock.getTid(), k -> new HashSet<>()).add(sharedLockTid);
//                            }
//                        }
//                    } else {
//                        blockingGraph.computeIfAbsent(lock.getTid(), k -> new HashSet<>()).add(currentLock.getTid());
//                    }
//                }
//            }
//
//            // Process conflicts within the lock queue
//            List<Lock> lockQueueList = new ArrayList<>(lockManager.getLockQueue());
//            for (int i = 0; i < lockQueueList.size(); i++) {
//                Lock lock1 = lockQueueList.get(i);
//                for (int j = 0; j < i; j++) {
//                    Lock lock2 = lockQueueList.get(j);
//                    if (isConflict(lock2, lock1)) {
//                        blockingGraph.computeIfAbsent(lock1.getTid(), k -> new HashSet<>()).add(lock2.getTid());
//                    }
//                }
//            }
//        }
//
//
//        return blockingGraph;
//    }

    public boolean isUp(){
        return isUp;
    }

    public int getSid(){
        return sid;
    }

}

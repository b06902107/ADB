package com.example.data;

import com.example.lock.LockManager;

import java.util.*;

public class Variable {
    private String vid;
    private Deque<Value> commitValueList;
    private Map<String, TemporaryValue> temporaryValueList;
    private boolean isReplicated;
    private boolean isReadable;

    public Variable(String vid, Value initValue, boolean isReplicated) {
        this.vid = vid;
        this.commitValueList = new LinkedList<>();
        this.commitValueList.addFirst(initValue);
        this.temporaryValueList = new HashMap<>();
        this.isReplicated = isReplicated;
        this.isReadable = true;
    }

    public int getLastCommitValue() {
        // Retrieve the most recent commit value
        Value recentValue = this.commitValueList.peekFirst();
        return recentValue != null ? recentValue.getValue() : null;
    }

    public int getCommitTimes() {
        return commitValueList.size();
    }

    public int getReadValue(int timestamp) {
        Iterator iteratorVals = commitValueList.iterator();

        // prints the elements using an iterator
        while (iteratorVals.hasNext()) {
            CommitValue now = (CommitValue) iteratorVals.next();
            if (now.getCommitTime() < timestamp){
                return now.getValue();
            }
        }
        return commitValueList.peekLast().getValue();
    }

    public void addCommitValue(Value v) {
        this.commitValueList.addFirst(v);
    }

    public TemporaryValue getTemporaryValue(String tid) throws DataError {
        if (!temporaryValueList.containsKey(tid)) {
            //throw new DataError("Transaction " + tid + "Variable " + vid + " has no temporary value.");
            return null;
        }
        return temporaryValueList.get(tid);
    }


//    public int getTemporaryValue() throws DataError {
//        if (temporaryValue == null) {
//            throw new DataError("Variable " + vid + " has no temporary value.");
//        }
//        return temporaryValue.getValue();
//    }

    public void setTemporaryValue(TemporaryValue value){
        temporaryValueList.put(value.getTid(), value);
        // temporaryValue = value;
    }

    public void removeTemporaryValue(String tid) throws DataError {
        if ( temporaryValueList.containsKey(tid) ) {
            temporaryValueList.remove(tid);
        }
    }

    // Getters and setters for other fields
    public String getVid() {
        return vid;
    }

    public void setVid(String vid) {
        this.vid = vid;
    }

    public boolean isReplicated() {
        return isReplicated;
    }

    public void setReplicated(boolean replicated) {
        isReplicated = replicated;
    }

    public boolean isReadable() {
        return isReadable;
    }

    public void setReadable(boolean readable) {
        isReadable = readable;
    }

    public Deque<Value> getCommitValueList() {
        return commitValueList;
    }

}

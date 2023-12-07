package com.example.data;

import java.util.Deque;
import java.util.LinkedList;

public class Variable {
    private String vid;
    private Deque<Value> commitValueList;
    private Value temporaryValue;
    private boolean isReplicated;
    private boolean isReadable;

    public Variable(String vid, Value initValue, boolean isReplicated) {
        this.vid = vid;
        this.commitValueList = new LinkedList<>();
        this.commitValueList.addFirst(initValue);
        this.temporaryValue = null;
        this.isReplicated = isReplicated;
        this.isReadable = true;
    }

    public int getLastCommitValue() {
        // Retrieve the most recent commit value
        Value recentValue = this.commitValueList.peekFirst();
        return recentValue != null ? recentValue.getValue() : null;
    }

    public void addCommitValue(Value v) {
        this.commitValueList.addFirst(v);
    }

    public int getTemporaryValue() throws DataError {
        if (temporaryValue == null) {
            throw new DataError("Variable " + vid + " has no temporary value.");
        }
        return temporaryValue.getValue();
    }

    public Value getTemp(){
        return temporaryValue;
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

    public void setTemporaryValue(Value value){
        temporaryValue = value;
    }


}

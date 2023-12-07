package com.example.data;

public class ResultValue extends Value {
    private boolean isSuccess;

    public ResultValue(int value, boolean isSuccess) {
        super(value);
        this.isSuccess = isSuccess;
    }

    // Getters and setters
    public boolean isSuccess() {
        return isSuccess;
    }

    public void setIsSuccess(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }
}

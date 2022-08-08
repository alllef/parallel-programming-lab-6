package com.github.alllef;

public class ResultsBean {
private int workersNum;
private int matrixSize;
private double timeInMilliSeconds;

    public ResultsBean(int workersNum, int matrixSize, double timeInMilliSeconds) {
        this.workersNum = workersNum;
        this.matrixSize = matrixSize;
        this.timeInMilliSeconds = timeInMilliSeconds;
    }

    public int getWorkersNum() {
        return workersNum;
    }

    public void setWorkersNum(int workersNum) {
        this.workersNum = workersNum;
    }

    public int getMatrixSize() {
        return matrixSize;
    }

    public void setMatrixSize(int matrixSize) {
        this.matrixSize = matrixSize;
    }

    public double getTimeInMilliSeconds() {
        return timeInMilliSeconds;
    }

    public void setTimeInMilliSeconds(double timeInMilliSeconds) {
        this.timeInMilliSeconds = timeInMilliSeconds;
    }
}

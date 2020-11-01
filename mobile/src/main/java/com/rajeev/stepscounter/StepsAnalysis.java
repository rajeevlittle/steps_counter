package com.rajeev.stepscounter;

import com.rajeev.stepscounter.utilities.Utils;

public class StepsAnalysis {
    private StepsRecord[] stepsRecords;
    private int min;
    private int max;
    private int totalSteps;
    private String activityGroup;
    private double mean, standardDiviation;

    public StepsAnalysis(StepsRecord[] stepsRecords) {

        for (StepsRecord stepsRecord : stepsRecords) {
            if (stepsRecord.getSteps() < min) {
                min = stepsRecord.getSteps();
            }
            if (stepsRecord.getSteps() > max) {
                max = stepsRecord.getSteps();
            }
            totalSteps += stepsRecord.getSteps();
        }

        mean = totalSteps / stepsRecords.length;
        double temp = 0;
        for (int i = 0; i < stepsRecords.length; i++) {
            temp += (stepsRecords[i].getSteps() - mean) * (stepsRecords[i].getSteps() - mean);
        }

        standardDiviation = Math.sqrt((1 / mean) * temp);
        activityGroup = Utils.getActivityStatus(totalSteps);
    }

    public StepsRecord[] getStepsRecords() {
        return stepsRecords;
    }

    public void setStepsRecords(StepsRecord[] stepsRecords) {
        this.stepsRecords = stepsRecords;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public double getMean() {
        return mean;
    }

    public void setMean(double mean) {
        this.mean = mean;
    }

    public double getStandardDiviation() {
        return standardDiviation;
    }

    public void setStandardDiviation(double standardDiviation) {
        this.standardDiviation = standardDiviation;
    }

    public int getTotalSteps() {
        return totalSteps;
    }

    public void setTotalSteps(int totalSteps) {
        this.totalSteps = totalSteps;
    }

    public String getActivityGroup() {
        return activityGroup;
    }

    public void setActivityGroup(String activityGroup) {
        this.activityGroup = activityGroup;
    }
}

package com.rajeev.stepscounter;

public class StepsRecord {

    private String uuid;
    private int steps;
    private String date;


    /**
     * Constructing a class with the following values
     *
     * @param uuid  The user unique id
     * @param steps The steps
     * @param date  The date
     */
    public StepsRecord(String uuid, int steps, String date) {
        this.uuid = uuid;
        this.steps = steps;
        this.date = date;
    }

    /**
     * Gets the uuid
     *
     * @return Returns the uuid
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Sets the uuid to a new one
     *
     * @param uuid The new uuid
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * Gets the steps
     *
     * @return Returns the steps
     */
    public int getSteps() {
        return steps;
    }

    /**
     * Sets the steps
     *
     * @param steps The steps to be set
     */
    public void setSteps(int steps) {
        this.steps = steps;
    }

    /**
     * Gets the date of the record
     *
     * @return Returns the date of the record
     */
    public String getDate() {
        return date;
    }


    /**
     * Sets the date of the record
     *
     * @param date The new date to be set
     */
    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "[StepsRecord] - uuid=" + uuid + ", steps=" + steps + ", date=" + date;
    }

}
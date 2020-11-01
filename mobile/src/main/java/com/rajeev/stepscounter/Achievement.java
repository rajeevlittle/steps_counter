package com.rajeev.stepscounter;

public class Achievement {


    public enum Medal {
        Bronze, Silver, Gold;
    }


    private Medal medal;
    private String title;
    private String description;
    private int reward;
    private int requiredSteps;


    /**
     * A constructor
     *
     * @param medal         The medal of the achievement
     * @param title         The title of the achievement
     * @param description   The description of the achievement
     * @param reward        The reward of the achievement
     * @param requiredSteps Number of steps required to complete the achievement
     */
    public Achievement(Medal medal, String title, String description, int reward, int requiredSteps) {
        this.medal = medal;
        this.title = title;
        this.description = description;
        this.reward = reward;
        this.requiredSteps = requiredSteps;
    }


    /**
     * Gets the title
     *
     * @return Returns the title
     */
    public String getTitle() {
        return title;
    }


    /**
     * Sets the title of an achievement to another one
     *
     * @param title The new title
     */
    public void setTitle(String title) {
        this.title = title;
    }


    /**
     * Gets the description
     *
     * @return Returns the description
     */
    public String getDescription() {
        return description;
    }


    /**
     * Sets a new description for the achievement
     *
     * @param description The description
     */
    public void setDescription(String description) {
        this.description = description;
    }


    /**
     * Gets the reward
     *
     * @return Return the reward
     */
    public int getReward() {
        return reward;
    }


    /**
     * Sets the reard
     *
     * @param reward The reward
     */
    public void setReward(int reward) {
        this.reward = reward;
    }


    /**
     * Gets the required steps
     *
     * @return Returns the required steps to complete an achievement
     */
    public int getRequiredSteps() {
        return requiredSteps;
    }

    /**
     * Sets the new required steps of an achievement
     *
     * @param requiredSteps The new requiredSteps value needed to complete the achievement
     */
    public void setRequiredSteps(int requiredSteps) {
        this.requiredSteps = requiredSteps;
    }

    /**
     * Gets the medalious type of the game
     *
     * @return Returns the medal
     */
    public Medal getMedal() {
        return medal;
    }


    /**
     * Sets the medal
     *
     * @param medal The new medal value
     */
    public void setMedal(Medal medal) {
        this.medal = medal;
    }
}

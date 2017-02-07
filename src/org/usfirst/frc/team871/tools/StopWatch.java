package org.usfirst.frc.team871.tools;

/**
 * sets time and then waits
 */
public class StopWatch {
    /**
     * lets you know the time
     */
    private long appriseTime;

    public StopWatch(long waitTime) {
        appriseTime = System.currentTimeMillis() + waitTime;
    }

    /**
     * let's you know if the time has passed
     */
    public boolean timeUp() {
        return (appriseTime <= System.currentTimeMillis());
    }

}

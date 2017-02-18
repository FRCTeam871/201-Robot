package org.usfirst.frc.team871.tools;

/**
 * Lets you know when a specified time has passed after the StopWatch was
 * created.
 */
public class StopWatch {

    private long appriseTime;

/**
 * Creates a new StopWatch which is set to wait the specified time.
 * 
 * @param waitTime - Long
 */
    public StopWatch(long waitTime) {
        appriseTime = System.currentTimeMillis() + waitTime;
    }

/**
 * Returns if the specified time has passed or not.
 * 
 * @return Boolean representing if the time is up
 */
    public boolean timeUp() {
        return (appriseTime <= System.currentTimeMillis());
    }

}

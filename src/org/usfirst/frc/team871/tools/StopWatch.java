package org.usfirst.frc.team871.tools;

public class StopWatch {

    long appriseTime;

    public StopWatch(long waitTime) {
        appriseTime = System.currentTimeMillis() + waitTime;
    }

    public boolean timeUp() {
        return (appriseTime <= System.currentTimeMillis());
    }

}

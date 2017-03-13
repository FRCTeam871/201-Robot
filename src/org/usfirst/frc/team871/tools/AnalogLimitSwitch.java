package org.usfirst.frc.team871.tools;

import edu.wpi.first.wpilibj.AnalogInput;

/**
 * An implementation of the ILimitSwitch interface which uses analog inputs.
 * 
 * @author Team871
 */
public class AnalogLimitSwitch implements ILimitSwitch {
    private boolean triggerAboveThreshhold;

    private double threshold;

    private AnalogInput input;

/**
 * @param input - AnalogInput
 * @param threshold - Double representing the trigger threshold
 * @param triggerAboveThreshhold - Boolean indicating if the sensor should
 * trigger above or below the threshold
 */
    public AnalogLimitSwitch(AnalogInput input, double threshold, boolean triggerAboveThreshhold) {
        this.input = input;
        this.threshold = threshold;
        this.triggerAboveThreshhold = triggerAboveThreshhold;
    }

/**
 * Changes the threshold to the specified value
 * 
 * @param threshhold - Double
 */
    public void setThreshhold(double threshhold) {
        this.threshold = threshhold;
    }

/**
 * Sets whether the sensor should trigger above or below the threshold.  True
 * indicates it should trigger above the threshold and false indicates below.
 * 
 * @param triggerAboveThreshhold - Boolean
 */
    public void setTrigger(boolean triggerAboveThreshhold) {
        this.triggerAboveThreshhold = triggerAboveThreshhold;
    }

/**
 * @return The status of the sensor
 */
    @Override
    public boolean isAtLimit() {
        if (triggerAboveThreshhold) {
            return input.getVoltage() > threshold;
        } else {
            return input.getVoltage() < threshold;
        }
    }
}

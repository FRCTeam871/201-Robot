package org.usfirst.frc.team871.tools;

import edu.wpi.first.wpilibj.AnalogInput;

public class AnalogLimitSwitch implements ILimitSwitch {
    boolean triggerAboveThreshhold;

    double threshhold;

    AnalogInput input;

    public AnalogLimitSwitch(AnalogInput input, double threshhold, boolean triggerAboveThreshhold) {
        this.input = input;
        this.threshhold = threshhold;
        this.triggerAboveThreshhold = triggerAboveThreshhold;
    }

    public void setThreshhold(double threshhold) {
        this.threshhold = threshhold;
    }

    public void setTrigger(boolean triggerAboveThreshhold) {
        this.triggerAboveThreshhold = triggerAboveThreshhold;
    }

    @Override
    public boolean isAtLimit() {
        if (triggerAboveThreshhold) {
            return input.getVoltage() > threshhold;
        } else {
            return input.getVoltage() < threshhold;
        }
    }
}

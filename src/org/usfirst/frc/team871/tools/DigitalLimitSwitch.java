package org.usfirst.frc.team871.tools;

import edu.wpi.first.wpilibj.DigitalInput;

public class DigitalLimitSwitch implements ILimitSwitch {
    boolean activeLow;

    DigitalInput input;

    public DigitalLimitSwitch(DigitalInput input, boolean activeLow) {
        this.input = input;
        this.activeLow = activeLow;
    }

    public DigitalLimitSwitch(DigitalInput input) {
        this.input = input;
        this.activeLow = false;
    }

    public void setActiveLow(boolean activeLow) {
        this.activeLow = activeLow;
    }

    @Override
    public boolean isAtLimit() {
        return (activeLow) ? !input.get() : input.get();
    }
}

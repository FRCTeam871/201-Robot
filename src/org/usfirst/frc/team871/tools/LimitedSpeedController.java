package org.usfirst.frc.team871.tools;

import edu.wpi.first.wpilibj.SpeedController;

public class LimitedSpeedController implements SpeedController {
    SpeedController motor;

    ILimitSwitch upper;
    ILimitSwitch lower;

    public LimitedSpeedController(SpeedController motor, ILimitSwitch upper, ILimitSwitch lower, boolean inverted) {
        this.motor = motor;
        this.upper = upper;
        this.lower = lower;

        motor.setInverted(inverted);
    }

    public LimitedSpeedController(SpeedController motor, ILimitSwitch upper, ILimitSwitch lower) {
        this.motor = motor;
        this.upper = upper;
        this.lower = lower;

        motor.setInverted(false);
    }

    @Override
    public void pidWrite(double output) {
        set(output);
    }

    @Override
    public double get() {
        return motor.get();
    }

    @Override
    public void set(double speed) {
        if ((upper.isAtLimit() && (speed > 0.0)) || (lower.isAtLimit() && (speed < 0.0))) {
            motor.set(0.0);
        } else {
            motor.set(speed);
        }
    }

    @Override
    public void setInverted(boolean isInverted) {
        motor.setInverted(isInverted);
    }

    @Override
    public boolean getInverted() {
        return motor.getInverted();
    }

    @Override
    public void disable() {
        motor.disable();
    }

    @Override
    public void stopMotor() {
        motor.stopMotor();
    }

}

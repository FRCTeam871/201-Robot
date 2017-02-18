package org.usfirst.frc.team871.tools;

import edu.wpi.first.wpilibj.SpeedController;

/**
 * An implementation of SpeedController which stops the motor at the specified
 * limits.
 * 
 * @author Team871
 */
public class LimitedSpeedController implements SpeedController {
    SpeedController motor;

    ILimitSwitch upper;
    ILimitSwitch lower;

/**
 * Constructor which specifies if the motor is to be inverted or not.
 * 
 * @param motor - SpeedController
 * @param upper - ILimitSwitch
 * @param lower - ILimitSwitch
 * @param inverted - Boolean indicating if the motor is inverted or not.
 */
    public LimitedSpeedController(SpeedController motor, ILimitSwitch upper, ILimitSwitch lower, boolean inverted) {
        this.motor = motor;
        this.upper = upper;
        this.lower = lower;

        motor.setInverted(inverted);
    }

/**
 * Basic constructor.
 * 
 * @param motor - SpeedController
 * @param upper - ILimitSwitch
 * @param lower - ILimitSwitch
 */
    public LimitedSpeedController(SpeedController motor, ILimitSwitch upper, ILimitSwitch lower) {
        this.motor = motor;
        this.upper = upper;
        this.lower = lower;

        motor.setInverted(false);
    }

/**
 * Implementation of the SpeedController method
 */
    @Override
    public void pidWrite(double output) {
        set(output);
    }

/**
 * @return Double containing the current speed of the motor
 */
    @Override
    public double get() {
        return motor.get();
    }

/**
 * Sets the speed of the motor.
 * 
 * @param speed - Double
 */
    @Override
    public void set(double speed) {
        if ((upper.isAtLimit() && (speed > 0.0)) || (lower.isAtLimit() && (speed < 0.0))) {
            motor.set(0.0);
        } else {
            motor.set(speed);
        }
    }

/**
 * Sets if the motor output should be inverted or not.
 * 
 * @param isInverted - Boolean
 */
    @Override
    public void setInverted(boolean isInverted) {
        motor.setInverted(isInverted);
    }

/**
 * @return Boolean representing if the motor is inverted or not
 */
    @Override
    public boolean getInverted() {
        return motor.getInverted();
    }

/**
 * Disables the motor.
 */
    @Override
    public void disable() {
        motor.disable();
    }

/**
 * Stops the motor.
 */
    @Override
    public void stopMotor() {
        motor.stopMotor();
    }

}

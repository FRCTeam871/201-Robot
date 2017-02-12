package org.usfirst.frc.team871.robot;

import org.usfirst.frc.team871.tools.StopWatch;
import org.usfirst.frc.team871.tools.actuators.LimitedSpeedController;
import org.usfirst.frc.team871.tools.controller.ButtonTypes;
import org.usfirst.frc.team871.tools.controller.EnhancedXBoxController;
import org.usfirst.frc.team871.tools.controller.XBoxAxes;
import org.usfirst.frc.team871.tools.controller.XBoxButtons;
import org.usfirst.frc.team871.tools.sensors.DigitalLimitSwitch;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.livewindow.LiveWindowSendable;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class BergDevice {
    private static final double LIFT_UP_SPEED = -.75;

    private static final double LIFT_DOWN_SPEED = .40;

    boolean pistonState;

    double motorSpeed;

    final DoubleSolenoid.Value release = DoubleSolenoid.Value.kForward;
    final DoubleSolenoid.Value grab = DoubleSolenoid.Value.kReverse;

    final SpeedController liftMotor;

    final DigitalInput upperLimit;
    final DigitalInput lowerLimit;
    final DigitalInput loadedSensor;

    DoubleSolenoid grabPiston;

    States currState = States.RESET;

    StopWatch timer;

    enum States {
        RESET, AWAITGEAR, CLAMP, MOVEUP, AWAITRELEASE, RELEASE;
    }

    public BergDevice(SpeedController liftMotor, DigitalInput upperLimit, DigitalInput lowerLimit, DigitalInput loadedSensor, DoubleSolenoid grabPiston) {
        pistonState = false;

        motorSpeed = 0.0;
        DigitalLimitSwitch lim = new DigitalLimitSwitch(upperLimit);
        lim.setActiveLow(true);

        DigitalLimitSwitch limdown = new DigitalLimitSwitch(lowerLimit);
        limdown.setActiveLow(true);

        this.liftMotor = new LimitedSpeedController(liftMotor, lim, limdown);

        this.upperLimit = upperLimit;
        this.lowerLimit = lowerLimit;

        this.loadedSensor = loadedSensor;

        this.grabPiston = grabPiston;
        if (liftMotor instanceof LiveWindowSendable) {
            LiveWindow.addActuator("Berg Device", "liftMotor", (LiveWindowSendable) liftMotor);
        }

        LiveWindow.addSensor("Berg Device", "Upper Limit", upperLimit);
        LiveWindow.addSensor("Berg Device", "Lower Limit", lowerLimit);
        LiveWindow.addSensor("Berg Device", "Loaded Sensor", loadedSensor);
        LiveWindow.addSensor("Berg Device", "Upper Limit", upperLimit);
        LiveWindow.addActuator("Berg Device", "Grab Piston", grabPiston);
    }

    public boolean isGearLoaded() {
        return loadedSensor.get();
    }

    public void update(EnhancedXBoxController joystick) {
        if (joystick.getValue(XBoxButtons.START)) {
            joystick.setButtonMode(XBoxButtons.A, ButtonTypes.TOGGLE);
            grabPiston.set(joystick.getValue(XBoxButtons.A) ? release : grab);
            if (joystick.getValue(XBoxAxes.TRIGGER) >= 0.3) {
                liftMotor.set(LIFT_UP_SPEED);
            } else if (joystick.getValue(XBoxAxes.TRIGGER) < -0.3) {
                liftMotor.set(LIFT_DOWN_SPEED);
            } else {
                liftMotor.set(0);
            }

        } else {
            joystick.setButtonMode(XBoxButtons.A, ButtonTypes.RISING);
            if (joystick.getValue(XBoxButtons.Y)) {
                currState = States.RESET;
            }
            SmartDashboard.putString("LiftMode", currState.toString());
            SmartDashboard.putBoolean("Up", upperLimit.get());
            SmartDashboard.putBoolean("Down", lowerLimit.get());

            switch (currState) {
                case RESET:
                    liftMotor.set(LIFT_DOWN_SPEED);
                    grabPiston.set(release);
                    if (!upperLimit.get()) {
                        currState = States.AWAITGEAR;
                    }
                    break;

                case AWAITGEAR:
                    if (!loadedSensor.get()) {
                        currState = States.CLAMP;
                        timer = new StopWatch(500);
                    }
                    break;

                case AWAITRELEASE:
                    liftMotor.set(0);
                    if (joystick.getValue(XBoxButtons.A)) {
                        currState = States.RELEASE;
                    }
                    break;

                case CLAMP:
                    grabPiston.set(grab);

                    if (timer.timeUp()) {
                        currState = States.MOVEUP;
                    }
                    break;

                case MOVEUP:
                    liftMotor.set(LIFT_UP_SPEED);
                    if (!lowerLimit.get()) {
                        currState = States.AWAITRELEASE;
                    }

                    break;

                case RELEASE:// TODO: implement timer
                    grabPiston.set(release);
                    if (joystick.getValue(XBoxButtons.A)){
                    currState = States.RESET;
                    }
                    break;

            }
        }
    }
}

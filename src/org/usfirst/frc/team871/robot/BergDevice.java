package org.usfirst.frc.team871.robot;

import org.usfirst.frc.team871.tools.ButtonTypes;
import org.usfirst.frc.team871.tools.DigitalLimitSwitch;
import org.usfirst.frc.team871.tools.EnhancedXBoxController;
import org.usfirst.frc.team871.tools.LimitedSpeedController;
import org.usfirst.frc.team871.tools.StopWatch;
import org.usfirst.frc.team871.tools.XBoxAxes;
import org.usfirst.frc.team871.tools.XBoxButtons;
import org.usfirst.frc.team871.tools.XBoxJoypads;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.livewindow.LiveWindowSendable;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class BergDevice {

    enum States {
        RESET, AWAITGEAR, CLAMP, MOVEUP, AWAITRELEASE, RELEASE;
    }

    enum ControlMode {
        AUTO, MANUAL, SEMI
    }

    private static final double LIFT_UP_SPEED = -.75;

    private static final double LIFT_DOWN_SPEED = .40;

    boolean pistonState;
    boolean shouldAdvance = false;

    double motorSpeed;

    final DoubleSolenoid.Value release = DoubleSolenoid.Value.kForward;
    final DoubleSolenoid.Value grab = DoubleSolenoid.Value.kReverse;

    final SpeedController liftMotor;

    final DigitalInput upperLimit;
    final DigitalInput lowerLimit;
    final DigitalInput loadedSensor;

    DoubleSolenoid grabPiston;

    private States currState = States.RESET;
    private ControlMode currMode = ControlMode.AUTO;
    private StopWatch timer;



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

    public void setModeAuto() {
        currMode = ControlMode.AUTO;
    }

    public void setModeManual() {
        currMode = ControlMode.MANUAL;
    }

    public void setModeSemi() {
        currMode = ControlMode.SEMI;
    }

    public void advanceState() {
        shouldAdvance = true;
    }
    
    private void doManual(EnhancedXBoxController joystick) {
        joystick.setButtonMode(XBoxButtons.A, ButtonTypes.TOGGLE);
        grabPiston.set(joystick.getValue(XBoxButtons.A) ? release : grab);
        if (joystick.getValue(XBoxAxes.TRIGGER) >= 0.3) {
            liftMotor.set(LIFT_UP_SPEED);
        } else if (joystick.getValue(XBoxAxes.TRIGGER) < -0.3) {
            liftMotor.set(LIFT_DOWN_SPEED);
        } else {
            liftMotor.set(0);
        }


    }

    private void changeState(States newState){
        if((currMode == ControlMode.AUTO) || ((currMode == ControlMode.SEMI) && shouldAdvance)) {
            currState = newState;
            shouldAdvance = false;
        }
    }

    public void update(EnhancedXBoxController joystick) {
        
        NetworkTable.getTable("SmartDashboard").putString("bergState", currState.toString());
        
        switch(joystick.getValue(XBoxJoypads.LJOYPAD)){
            case 0:
                currMode = ControlMode.SEMI;
                break;
            case 90:
                currMode = ControlMode.AUTO;
                break;
            case 270:
                currMode = ControlMode.MANUAL;
                break;
        }
        
        if (currMode == ControlMode.MANUAL) {
            doManual(joystick);
        } else {
            joystick.setButtonMode(XBoxButtons.A, ButtonTypes.RISING);
            if (joystick.getValue(XBoxButtons.Y)) {
                changeState(States.RESET);
            }
            doStates(joystick);
        }
    }

    private void doStates(EnhancedXBoxController joystick) {
        SmartDashboard.putString("LiftMode", currState.toString());
        SmartDashboard.putBoolean("Up", upperLimit.get());
        SmartDashboard.putBoolean("Down", lowerLimit.get());

        switch (currState) {
            case RESET:
                liftMotor.set(LIFT_DOWN_SPEED);
                grabPiston.set(release);
                /* 
                 * Don't use changeState because we should never stay in this state.
                 * Jack is still a butt.
                 */
                if (!upperLimit.get()) {
                    currState = States.AWAITGEAR;
                }
                break;

            case AWAITGEAR:
                liftMotor.set(0);
                if (!loadedSensor.get()) { 
                    // Don't use changeState because we should never stay in this state.
                    currState = States.CLAMP;
                    timer = new StopWatch(500);
                }
                break;

            case AWAITRELEASE:
                liftMotor.set(0);
                if (joystick.getValue(XBoxButtons.A) || shouldAdvance) {
                    changeState(States.RELEASE);
                }
                break;

            case CLAMP:
                grabPiston.set(grab);
                liftMotor.set(0);
                if (timer.timeUp()) {
                    changeState(States.MOVEUP);
                }
                break;

            case MOVEUP:
                liftMotor.set(LIFT_UP_SPEED);
                if (!lowerLimit.get()) {
                    // Don't use changeState because we should never stay in this state.
                    currState = States.AWAITRELEASE;
                }

                break;

            case RELEASE:
                grabPiston.set(release);
                liftMotor.set(0);
                if (joystick.getValue(XBoxButtons.A) || shouldAdvance){
                    changeState(States.RESET);
                }
                break;

        }
    }
    
    public void reset() {
        shouldAdvance = false;
        currState = States.RESET;
    }

    public ControlMode getMode() {
        return currMode;
    }
}


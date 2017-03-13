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
import edu.wpi.first.wpilibj.Joystick.ButtonType;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.livewindow.LiveWindowSendable;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class BergDevice {

    public static enum States {
        RESET, AWAITGEAR, CLAMP, MOVEUP, AWAITRELEASE, RELEASE, LOWER;
    }

    public static enum ControlMode {
        AUTO, MANUAL, SEMI
    }
    
    boolean pistonState;
    boolean shouldAdvance = false;

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

        DigitalLimitSwitch lim = new DigitalLimitSwitch(upperLimit);
        lim.setActiveLow(true);

        DigitalLimitSwitch limdown = new DigitalLimitSwitch(lowerLimit);
        limdown.setActiveLow(true);

        this.liftMotor = new LimitedSpeedController(liftMotor, lim, limdown);

        this.upperLimit = upperLimit;
        this.lowerLimit = lowerLimit;

        this.loadedSensor = loadedSensor;

        this.grabPiston = grabPiston;
        //if (liftMotor instanceof LiveWindowSendable) {
        //    LiveWindow.addActuator("Berg Device", "liftMotor", (LiveWindowSendable) liftMotor);
        //}

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
       setModeSemi(false);
    }
    
    public void setModeSemi(boolean lower) {
        currMode = ControlMode.SEMI;
        
        if(lower){
        	currState = States.LOWER;
        }
    }

    public void advanceState() {
        shouldAdvance = true;
    }
    
    private void doManual(EnhancedXBoxController joystick) {
    	double val = joystick.getValue(XBoxAxes.TRIGGER);
    	
        if (val >= 0.3) {
            liftMotor.set(Vars.BERG_UP_SPEED);
        } else if (val < -0.3) {
            liftMotor.set(Vars.BERG_DOWN_SPEED);
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
        
        switch(joystick.getValue(Vars.DPAD)){
            case 0:
                currMode = ControlMode.SEMI;
                break;
            case 90:
                currMode = ControlMode.AUTO;
                currState = States.RESET;
                break;
            case 270:
                currMode = ControlMode.MANUAL;
                currState = States.RESET;
                break;
        }
        
        if (currMode == ControlMode.MANUAL) {
            doManual(joystick);
        } else {
            //joystick.setButtonMode(Vars.BERG_PIST_GRAB, ButtonTypes.RISING);
            if (joystick.getValue(Vars.BERG_AUTO_RESET)) {
                changeState(States.RESET);
            }
            doStates(joystick);
        }
        
        SmartDashboard.putString("LiftMode", currState.toString());
        SmartDashboard.putBoolean("Up", upperLimit.get());
        SmartDashboard.putBoolean("Down", lowerLimit.get());
        ((LimitedSpeedController)liftMotor).printInternals("BD");
        
    }

    private void doStates(EnhancedXBoxController joystick) {
  
        DoubleSolenoid.Value pist = grabPiston.get();
        double liftMotorSpeed = 0.0d;
        
        switch (currState) {
            case RESET:
                liftMotorSpeed = Vars.BERG_DOWN_SPEED;
                pist = release;
                /* 
                 * Don't use changeState because we should never stay in this state.
                 * Jack is still a butt.
                 */
                if (!upperLimit.get()) {
                    currState = States.AWAITGEAR;
                }
                break;

            case AWAITGEAR:
                liftMotorSpeed = 0;
                if (!loadedSensor.get()) { 
                    // Don't use changeState because we should never stay in this state.
                    currState = States.CLAMP;
                    timer = new StopWatch(500);
                }
                
                
                liftMotorSpeed = Vars.BERG_DOWN_SPEED;
                break;

            case AWAITRELEASE:
                liftMotorSpeed = 0;
                if (joystick.getValue(Vars.BERG_ADVANCE) || shouldAdvance) {
                    changeState(States.RELEASE);
                }
                
                liftMotorSpeed = Vars.BERG_UP_SPEED;
                break;

            case CLAMP:
                pist = grab;
                liftMotorSpeed = 0;
                if (timer.timeUp()) {
                    changeState(States.MOVEUP);
                }
                break;

            case MOVEUP:
                liftMotorSpeed = Vars.BERG_UP_SPEED;
                if (!lowerLimit.get()) {
                    // Don't use changeState because we should never stay in this state.
                    currState = States.AWAITRELEASE;
                }
                break;

            case RELEASE:
                pist = release;
                liftMotorSpeed = 0;
                if (joystick.getValue(Vars.BERG_ADVANCE) || shouldAdvance){
                    changeState(States.RESET);
                }
                break;
            case LOWER:
            	liftMotorSpeed = Vars.BERG_UP_SPEED;
                if (joystick.getValue(Vars.BERG_ADVANCE) || shouldAdvance){
                    changeState(States.MOVEUP);
                }
            	break;

        }
        
        pist = joystick.getValue(Vars.MANUAL_BERG_CLAMP) ? grab : pist;
        
//        if(joystick.getValue(XBoxButtons.LBUMPER)){ 
//            pist = grab;
//        }
        
        grabPiston.set(pist);
        
        liftMotor.set(liftMotorSpeed);
        
    }
    
    public void reset() {
        shouldAdvance = false;
        currState = States.RESET;
    }

    public ControlMode getMode() {
        return currMode;
    }
}


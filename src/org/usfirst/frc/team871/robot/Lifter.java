package org.usfirst.frc.team871.robot;

import org.usfirst.frc.team871.tools.EnhancedXBoxController;
import org.usfirst.frc.team871.tools.ILimitSwitch;
import org.usfirst.frc.team871.tools.XBoxAxes;
import org.usfirst.frc.team871.tools.XBoxButtons;

import edu.wpi.first.wpilibj.CANSpeedController;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.livewindow.LiveWindowSendable;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Lifter {
    boolean isSpinning, isClimbing;

    int curArrayPos, numSamples;

    double prevCurrent[];

    CANSpeedController liftMotor;

    ILimitSwitch topSensor;

    public boolean kill = false;

    private final  int NUM_SAMPLES = 10;
    private int cSample = 0;
    private double[] samples = new double[NUM_SAMPLES];

	private double avg = 00.00;
	
    
    public Lifter(CANSpeedController liftMotor, ILimitSwitch topSensor) {
        isSpinning = false;
        isClimbing = false;

        curArrayPos = 0;

        prevCurrent = new double[100];

        this.liftMotor = liftMotor;
        this.topSensor = topSensor;

        if (liftMotor instanceof LiveWindowSendable) {
            LiveWindow.addActuator("Lifter", "Lift Motor", liftMotor);
        }
    }

    public void startSpin() {
        isSpinning = true;
    }

    public void stopSpin() {
        isSpinning = false;
        isClimbing = false;
    }

    public void update() {
    	SmartDashboard.putBoolean("atRopeTop", kill);
    	
        if (isSpinning) {

            double c = liftMotor.getOutputCurrent();
            samples[cSample] = c;
            if(cSample == NUM_SAMPLES-1) {
            	double total = 0;
            	for(double d : samples) total += d;
            	
            	avg = total / samples.length;
            	
            }
            
            cSample = (cSample+1) %NUM_SAMPLES;
            
            if(c > 30){
            	stopSpin();
            	liftMotor.set(0.0);
            	liftMotor.stopMotor();
            	//liftMotor.disable();
            } else if (isClimbing) {
                liftMotor.set(Vars.LIFT_CLIMB_SPEED);
            } else {
                liftMotor.set(Vars.LIFT_IDLE_SPEED);
            }
            
        } else {
            liftMotor.set(0.0);
        }

        if (topSensor.isAtLimit()) {
            stopSpin();
        }
    }
    
    public void climb(EnhancedXBoxController joystick) {
    	double speed = 0;
    	
    	double c = Vars.CLASSMATE_TEST ? 0 : liftMotor.getOutputCurrent();
    	samples[cSample] = c;
        if(cSample == NUM_SAMPLES-1) {
        	double total = 0;
        	for(double d : samples) total += d;
        	
        	avg = total / samples.length;
        	
        }
        
        cSample = (cSample+1) %NUM_SAMPLES;
        
        
        if(joystick.getRawValue(Vars.LIFT_RELEASE)){
        	kill = false;
        }
        
        if(avg > 25){
        	
        	kill = true;
        	stopSpin();
        	speed = 0;
        	liftMotor.stopMotor();
        	//liftMotor.disable();
        }else if(!kill){
        	speed = joystick.getValue(XBoxAxes.TRIGGER);
        }
        
        liftMotor.set(speed);
    }

    public boolean isAtTop() {
        return topSensor.isAtLimit();
    }
    
    public void printCurrent(){
    	if(!Vars.CLASSMATE_TEST){
	    	double c = liftMotor.getOutputCurrent();
	        SmartDashboard.putNumber("liftCurr", c);
    	}
	}

}

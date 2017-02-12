package org.usfirst.frc.team871.robot;

import org.usfirst.frc.team871.tools.PIDOutputWrapper;
import org.usfirst.frc.team871.tools.controller.EnhancedXBoxController;
import org.usfirst.frc.team871.tools.controller.XBoxAxes;

import com.kauailabs.navx.frc.AHRS;

import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.livewindow.LiveWindowSendable;

public class DriveTrain {
    final RobotDrive mechDrive;
    final AHRS gyro;
    final PIDController headingPID;
    final PIDOutputWrapper headingWrapper;

    double targetHeading = 0.0;
    boolean headingHold = false;
    
    public DriveTrain(SpeedController frontLeft, SpeedController frontRight, SpeedController backLeft, SpeedController backRight, AHRS gyro) {
        frontRight.setInverted(true);
        backRight.setInverted(true);
        mechDrive = new RobotDrive(frontLeft, backLeft, frontRight, backRight);
        headingWrapper = new PIDOutputWrapper();
        headingPID = new PIDController(.01, 0, 0, gyro, headingWrapper);//new PIDControl(0.01, 0, 0, 0, .1, 3);
        
        this.gyro = gyro;
        if (frontRight instanceof LiveWindowSendable) {
            LiveWindow.addActuator("Drive Train", "Front Right Motor", (LiveWindowSendable) frontRight);
        }

        if (frontLeft instanceof LiveWindowSendable) {
            LiveWindow.addActuator("Drive Train", "Front Left Motor", (LiveWindowSendable) frontLeft);
        }

        if (backLeft instanceof LiveWindowSendable) {
            LiveWindow.addActuator("Drive Train", "Rear Left Motor", (LiveWindowSendable) backLeft);
        }

        if (backRight instanceof LiveWindowSendable) {
            LiveWindow.addActuator("Drive Train", "Rear Right Motor", (LiveWindowSendable) backRight);
        }

        LiveWindow.addSensor("Drive Train", "GYRO", gyro);
        LiveWindow.addActuator("Drive Train", "Heading Hold PID", headingPID);
    }
    
    /**
     * Set the target heading and enable heading hold mode.
     * In this mode the drive train will ignore rotation commands and instead
     * use an internal PID controller to maintain the desired heading
     * 
     * @param targetHeading
     */
    public void setHeadingHold(double targetHeading) {
    	headingHold = true;
    	this.targetHeading = targetHeading;
    	headingPID.setSetpoint(targetHeading);
    	headingPID.enable();
    }
    
    /**
     * Disable the heading hold feature
     */
    public void disableHeadingHold() {
    	headingHold = false;
    	headingPID.disable();
    }
    
    /**
     * Return if the drive train is in heading hold mode
     * @return
     */
    public boolean isHeadingHold() {
    	return headingHold;
    }
    
    /**
     * If heading hold mode is enabled, return true if the robot is at the
     * desired heading.  If NOT in heading hold mode, return true always
     * @return
     */
    public boolean onTargetHeading() {
    	if(headingHold) {
    		return headingPID.onTarget();
    	}
    	else return true;
    }
    
    protected void drive(double x, double y, double rotation, boolean fieldOriented) {
    	if(headingHold) {
    		rotation = headingWrapper.getOutput();
    	}
    	
    	mechDrive.mecanumDrive_Cartesian(x, y, rotation, fieldOriented ? gyro.getFusedHeading() : 0);
    }

    public void driveFieldOriented(EnhancedXBoxController j) {
        drive(-j.getValue(XBoxAxes.LEFTY), j.getValue(XBoxAxes.LEFTX), j.getValue(XBoxAxes.RIGHTX),true);
    }
    
    public void driveFieldOriented(double x, double y, double rotation) {
    	drive(x,y,rotation,true);
    }

    public void driveRobotOriented(EnhancedXBoxController j) {
    	drive(-j.getValue(XBoxAxes.LEFTY), j.getValue(XBoxAxes.LEFTX), j.getValue(XBoxAxes.RIGHTX),false);
    }
    
    public void driveRobotOriented(double x, double y, double rotation) {
    	drive(x,y,rotation,false);
    }
    
    /**
     * Unconditionally stop all drive motion.
     */
    public void stop(){
        mechDrive.mecanumDrive_Cartesian(0, 0, 0, 0);
    }
}
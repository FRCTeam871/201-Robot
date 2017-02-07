package org.usfirst.frc.team871.robot;

import org.usfirst.frc.team871.tools.EnhancedXBoxController;
import org.usfirst.frc.team871.tools.XBoxAxes;

import com.kauailabs.navx.frc.AHRS;

import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.livewindow.LiveWindowSendable;

public class DriveTrain {
	
	final RobotDrive mechDrive;
	final AHRS gyro;
	 
	public DriveTrain(SpeedController frontLeft, SpeedController frontRight, SpeedController backLeft, SpeedController backRight, AHRS gyro) {
		frontRight.setInverted(true);
		backRight.setInverted(true);
		mechDrive = new RobotDrive(frontLeft, backLeft, frontRight, backRight);
		
		this.gyro = gyro;
		if(frontRight instanceof LiveWindowSendable){
			LiveWindow.addActuator("Drive Train", "Front Right Motor", (LiveWindowSendable)frontRight);
		}
		
		if(frontLeft instanceof LiveWindowSendable){
			LiveWindow.addActuator("Drive Train", "Front Left Motor", (LiveWindowSendable)frontLeft);
		}
		
		if(backLeft instanceof LiveWindowSendable){
			LiveWindow.addActuator("Drive Train", "Rear Left Motor", (LiveWindowSendable)backLeft);
		}
		
		if(backRight instanceof LiveWindowSendable){
			LiveWindow.addActuator("Drive Train", "Rear Right Motor", (LiveWindowSendable)backRight);
		}
		
		//gyro.initTable(((LiveWindowSendable)frontRight).getTable());
		LiveWindow.addSensor("Drive Train", "GYRO", gyro);
		//System.out.println("ITable instance = " + gyro.getTable().getClass().getName());
	}
	
	
	public void driveFieldOriented(EnhancedXBoxController j){
		mechDrive.mecanumDrive_Cartesian(-j.getValue(XBoxAxes.LEFTY), j.getValue(XBoxAxes.LEFTX), j.getValue(XBoxAxes.RIGHTX), gyro.getAngle());
		
	}
	
	public void driveRobotOriented(EnhancedXBoxController j){
		mechDrive.mecanumDrive_Cartesian(-j.getValue(XBoxAxes.LEFTY), j.getValue(XBoxAxes.LEFTX), j.getValue(XBoxAxes.RIGHTX), 0);
	
	}
	
	public void resetNorth() {
		gyro.reset();
		
	}
	
	
}
package org.usfirst.frc.team871.auton;

import org.usfirst.frc.team871.robot.BergDevice;
import org.usfirst.frc.team871.robot.DriveTrain;
import org.usfirst.frc.team871.target.AutoDock;
import org.usfirst.frc.team871.target.ITarget;
import org.usfirst.frc.team871.target.ITargetAcquisition;
import org.usfirst.frc.team871.tools.StopWatch;

import com.kauailabs.navx.frc.AHRS;

public class AutonGearDropper {
	final DriveTrain drive;
	final AHRS gyro;
	final BergDevice berg;
	final ITargetAcquisition finder;
	final AutoDock docker;
	
	AutonStates state = AutonStates.DRIVE;
	
    private double baseAngle;
    private double turningDirection = .3;
    private StopWatch timer;
    private ITarget target;
	
	public AutonGearDropper(DriveTrain drive, BergDevice berg, AHRS gyro, ITargetAcquisition acq) {
		this.drive = drive;
		this.berg = berg;
		this.gyro = gyro;
		this.finder = acq;
		this.docker = new AutoDock(drive, finder, gyro, true);
		timer = new StopWatch(3000);
	}
	
	public void update() {
		target = finder.getTarget();
		
		switch(state) {
	        case DRIVE: 
	            if (!timer.timeUp()){
	               drive.driveRobotOriented(.75, 0, 0);
	            } else {
	                baseAngle = gyro.getAngle();
	                drive.driveRobotOriented(0, 0, 1);
	                state = AutonStates.SEARCH;
	            }
            break;
	        
	        case SEARCH: 
	            if (finder.isTargetAvailable()){ 
	            	state = AutonStates.DOCKING;
	            } else {
	                if (Math.abs(gyro.getAngle() - baseAngle) >= 45){
	                   turningDirection *= -1;
	                } 
	                drive.driveRobotOriented(0, 0, turningDirection);
	            }
	            
            break;
	            
	        case DOCKING:
	            docker.dock();
	            
	            if (docker.isDocked()){
	            	state = AutonStates.STOP;
	            }
            break;
            
	        case STOP:
	            drive.stop();
            break;
            
	        case PULL_OUT:
	            
            break;
	    }
	}
}

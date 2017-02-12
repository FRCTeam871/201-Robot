package org.usfirst.frc.team871.auton;

import org.usfirst.frc.team871.robot.BergDevice;
import org.usfirst.frc.team871.robot.DriveTrain;
import org.usfirst.frc.team871.target.AutoDock;
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
	TargetID whichTarget = TargetID.LEFT;
	
    private StopWatch timer;
	
    /**
     * Enum that encapsulates the target as well as the
     * desired heading to place the robot perpendicular to that target (assumes berg device)
     * 
     * @author Andrew
     */
    public enum TargetID {
    	LEFT(45),
    	CENTER(0),
    	RIGHT(315);
    	
    	final double heading;
    	private TargetID(double heading) {
    		this.heading = heading;
    	}
    	
    	public double getHeading() {
    		return heading;
    	}
    }
    
	public AutonGearDropper(DriveTrain drive, BergDevice berg, AHRS gyro, ITargetAcquisition acq) {
		this.drive = drive;
		this.berg = berg;
		this.gyro = gyro;
		this.finder = acq;
		this.docker = new AutoDock(drive, true);
		timer = new StopWatch(3000);
	}
	
	/**
	 * Set which target auton is aiming at
	 * 
	 * @param target
	 */
	public void setTarget(TargetID target) {
		whichTarget = target;
	}
	
	/**
	 * Do any primary initialization stuff
	 */
	public void init() {
		drive.setHeadingHold(0.0);
	}
	
	/**
	 * Main State machine for dropping the gear
	 * This code does the following:
	 * 
	 * <ol>
	 *   <li>Moves forward a bunch to cover distance quickly</li>
	 *   <li>Sets the drive into heading hold mode for the desired target</li>
	 *   <li>Searches for the target if it was'nt found</li>
	 *   <li>Docks with the target</li>
	 *   <li>TODO: Drops the gear</li>
	 * </ol>
	 * 
	 */
	public void update() {
		switch(state) {
	        case DRIVE: 
	        	//Drive forward for some set time
	            if (!timer.timeUp()){
	               drive.driveRobotOriented(.75, 0, 0);
	            } else {
	            	//When we're done driving forward, stop,
	            	//and set the heading hold for the desired target 
	            	//then begin searching
	                drive.stop();
	                drive.setHeadingHold(whichTarget.getHeading());
	                state = AutonStates.SEARCH;
	            }
            break;
	        
	        case SEARCH:
	        	//If we found the target, go ahead and begin docking
	            if (finder.isTargetAvailable()){ 
	            	state = AutonStates.DOCKING;
	            } else {
	                //TODO: Implement a better search pattern
	            	//      Maybe side to side w/o rotation
	                drive.driveRobotOriented(0, 0, 0);
	            }
	            
            break;
	            
	        case DOCKING:
	        	if(finder.isTargetAvailable()) {
		        	//Dock with the target using the specified docking algorithm
		            docker.dock(finder.getTarget());
		            
		            //Once we're done drop the gear and back off
		            if (docker.isDocked()){
		            	//TODO:  Don't want to stop,  want to release
		            	//       then back up
		            	state = AutonStates.STOP;
		            }
	        	}
	        	else {
	        		state = AutonStates.SEARCH;
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

package org.usfirst.frc.team871.target;

import org.usfirst.frc.team871.robot.DriveTrain;
import org.usfirst.frc.team871.tools.PIDControl;

public class AutoDock {
    private final int CENTER_X = 160;
    private final double DISTANCE = 30 /*inches*/;

    private boolean isDocked;
    private PIDControl pidX;
    private DriveTrain drive;

    private boolean isBergDevice;

    public AutoDock(DriveTrain drive, boolean dockType) {
        this.drive = drive;
        this.isBergDevice = dockType;
        
        isDocked = false;
        pidX = new PIDControl(.0045, 0, 0, CENTER_X, 0, 0);
    }

    private double findVectorX(ITarget target) {
        return pidX.getMotorPID(target.getCenterX());
    }

    private double findVectorY(ITarget target) {
        return target.getDistance() <= DISTANCE ? 0.0 : 0.4;
    }

    public void dock(ITarget target) {
    	//If no target is available,  don't freak out
    	double vectorX = 0.0;
		double vectorY = 0.0;
		
        if (target != null) {
            //Don't bother with crabbing unless heading is correct
            if(drive.onTargetHeading()) {
	            //Transpose the target vector 90 degrees if we're using the berg device.
	            vectorX = (isBergDevice) ? findVectorX(target) : findVectorY(target);
	            vectorY = (isBergDevice) ? findVectorY(target) : findVectorX(target);
            }
            
            if (target.getDistance() <= DISTANCE) {
                isDocked = true;
            }
        }
        
        drive.driveRobotOriented(vectorX, vectorY, 0);
    }
    
    public boolean isDocked() {
        return isDocked;
    }
}

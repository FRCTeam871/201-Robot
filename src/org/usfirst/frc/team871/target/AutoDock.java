package org.usfirst.frc.team871.target;

import org.usfirst.frc.team871.robot.DriveTrain;
import org.usfirst.frc.team871.tools.PIDControl;
import org.usfirst.frc.team871.tools.StopWatch;

import com.kauailabs.navx.frc.AHRS;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class AutoDock {
    
    private boolean isDocked;
    private final int CENTER_X = 160;
    private final double DISTANCE = 30 /*inches*/;

    private PIDControl pidX;
//    private PIDControl pidY;
    private PIDControl pidRot;

    private DriveTrain drive;
    
    AHRS gyro;

    private boolean isBergDevice;

    ITargetAcquisition targetFinder;
    StopWatch potato = new StopWatch(1000);
    public AutoDock(DriveTrain drive, ITargetAcquisition targetFinder, AHRS gyro, boolean dockType) {
        this.drive = drive;

        this.targetFinder = targetFinder;

        this.isBergDevice = dockType;
        
        this.gyro = gyro;
        
        isDocked = false;

        pidX = new PIDControl(.0045, 0, 0, CENTER_X, 0, 0);
        pidRot = new PIDControl(0.01, 0, 0, 0, .1, 3);
    }

    private double findVectorX(ITarget target) {
        return pidX.getMotorPID(target.getCenterX());
    }

    private double findVectorY(ITarget target) {
        return target.getDistance() <= DISTANCE ? 0.0 : 0.4;
    }

    private double findRot(ITarget target) {
        return pidRot.getPID(-gyro.getAngle());
    }

    public void dock() {
        SmartDashboard.putNumber("Gyro", gyro.getAngle());
        if (targetFinder.isTargetAvailable()) {
            ITarget target = targetFinder.getTarget();
            
            double vectorX = findVectorX(target);
            double vectorY = findVectorY(target);
            double rotVector = findRot(target);
            
            if(potato.timeUp()) {
                potato = new StopWatch(1000);
                System.out.println("Found: X: "+vectorX+ " Y: "+vectorY+" R: "+rotVector);
                System.out.println("     : D: "+target.getDistance()+" DL: "+target.getDistanceLeft()+" Dr: "+target.getDistanceRight());
            }

            if (isBergDevice == true) { // true is chute, false is Berg device
                vectorX = findVectorY(target);
                vectorY = findVectorX(target);
            }

            drive.driveRobotOriented(vectorX, vectorY, rotVector);
            //drive.driveRobotOriented(0, vectorY, rotVector);
            
            if (target.getDistance() <= DISTANCE) {
                isDocked = true;
            }
            
        } else {
            isDocked = false;
        }
    }
    
    public boolean isDocked() {
        return isDocked;
    }
}

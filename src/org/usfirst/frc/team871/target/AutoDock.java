package org.usfirst.frc.team871.target;

import org.usfirst.frc.team871.robot.DriveTrain;
import org.usfirst.frc.team871.tools.PIDControl;

public class AutoDock {

    private final int CENTER_X = 160;
    private final double DISTANCE = 5 /*inches*/; 

    private PIDControl pidX;
    private PIDControl pidY;
    private PIDControl pidRot;

    private DriveTrain drive;

    private boolean isBergDevice;

    ITargetAcquisition targetFinder;

    public AutoDock(DriveTrain drive, ITargetAcquisition targetFinder, boolean dockType) {
        this.drive = drive;

        this.targetFinder = targetFinder;

        this.isBergDevice = dockType;

        pidX = new PIDControl(.1, 0, 0, 0);
        pidY = new PIDControl(.1, 0, 0, DISTANCE);
        pidRot = new PIDControl(.1, 0, 0, CENTER_X);
    }

    private double findVectorX(ITarget target) {
        return pidX.getMotorPID(target.getDistanceLeft() - target.getDistanceRight());
    }

    private double findVectorY(ITarget target) {
        return pidY.getMotorPID(target.getDistance());
    }

    private double findRot(ITarget target) {
        return pidRot.getPID(target.getCenterX());
    }

    public void dock() {
        if (targetFinder.isTargetAvailable()) {
            ITarget target = targetFinder.getTarget();

            double vectorX = findVectorX(target);
            double vectorY = findVectorY(target);
            double rotVector = findRot(target);

            if (isBergDevice == true) { // true is chute, false is Berg device
                vectorX = findVectorY(target);
                vectorY = findVectorX(target);
            }

            drive.driveRobotOriented(vectorX, vectorY, rotVector); 
        }
    }
}

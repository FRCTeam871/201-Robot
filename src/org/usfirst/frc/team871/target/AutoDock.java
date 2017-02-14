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
    private DriveTrain drive;
    
    AHRS gyro;

    private boolean isBergDevice;

    public AutoDock(DriveTrain drive, AHRS gyro, boolean dockType) {
        this.drive = drive;

        this.isBergDevice = dockType;
        
        this.gyro = gyro;
        
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
        if (target == null) {
            return;
        }
        
        double vectorX = findVectorX(target);
        double vectorY = findVectorY(target);

        if (isBergDevice == true) { // true is chute, false is Berg device
            vectorX = findVectorY(target);
            vectorY = findVectorX(target);
        }

        drive.driveRobotOriented(vectorX, vectorY, 0);
        
        if (target.getDistance() <= DISTANCE) {
            isDocked = true;
        }
    }
    
    public boolean isDocked() {
        return isDocked;
    }
}

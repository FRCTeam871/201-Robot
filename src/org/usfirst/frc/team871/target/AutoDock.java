package org.usfirst.frc.team871.target;

import org.usfirst.frc.team871.robot.DriveTrain;
import org.usfirst.frc.team871.robot.Vars;
import org.usfirst.frc.team871.tools.PIDControl;

import com.kauailabs.navx.frc.AHRS;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class AutoDock {
    
    private boolean isDocked;
    private final int CENTER_X = 160;
    private final double DISTANCE = Vars.AUTO_DIST /*inches*/;

    private PIDControl pidX;
    private DriveTrain drive;
    
    AHRS gyro;

    private boolean isBergDevice;

    public AutoDock(DriveTrain drive, AHRS gyro, boolean dockType) {
        this.drive = drive;
        this.isBergDevice = dockType;
        this.gyro = gyro;
        isDocked = false;

        pidX = new PIDControl(.005, 0, 0.009, CENTER_X, 0, 0); /// was /0045, 0 .0045
    }

    private double findVectorX(ITarget target) {
        return pidX.getMotorPID(target.getCenterX());
    }

    private double findVectorY(ITarget target) {
        return target.getDistance() <= DISTANCE ? 0.0 : 0.60; //was .5
    }

    public void dock(ITarget target) {
        if (target == null) {
            return;
        }
        
        double vectorX = (!isBergDevice) ? findVectorX(target) : findVectorY(target);
        double vectorY = (!isBergDevice) ? findVectorY(target) : findVectorX(target);
        
        SmartDashboard.putNumber("Dock vx", vectorX);
        SmartDashboard.putNumber("Dock vy", vectorY);
        
        if (target.getDistance() <= DISTANCE) {
            isDocked = true;
            vectorX = 0;
        }
        
        drive.driveRobotOriented(vectorX, vectorY, 0);
    }
    
    public boolean isDocked() {
        return isDocked;
    }

    public void reset(){
        isDocked = false;
    }

}

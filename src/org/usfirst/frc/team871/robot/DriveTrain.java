package org.usfirst.frc.team871.robot;

import org.usfirst.frc.team871.tools.EnhancedXBoxController;
import org.usfirst.frc.team871.tools.XBoxAxes;

import com.kauailabs.navx.frc.AHRS;

import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.PIDOutput;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.livewindow.LiveWindowSendable;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class DriveTrain {

    final RobotDrive mechDrive;
    final AHRS gyro;
    double markedHeading;
    boolean isHeadingHold;
    PIDController pid;
    double pidOutput;
    
    public DriveTrain(SpeedController frontLeft, SpeedController frontRight, SpeedController backLeft, SpeedController backRight, AHRS gyro) {
        frontRight.setInverted(true);
        backRight.setInverted(true);
        mechDrive = new RobotDrive(frontLeft, backLeft, frontRight, backRight);
        
        this.gyro = gyro;
        
        pid = new PIDController(.01, 0, 0, gyro, new PIDOutput() {
            
            @Override
            public void pidWrite(double output) {
                pidOutput = output;
            }
        });
        
        pid.setAbsoluteTolerance(3);
        pid.setOutputRange(-.75, .75);
        
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
    }


    public void driveFieldOriented(EnhancedXBoxController j) {
        printSD();
        double angleOrient = (isHeadingHold)? pidOutput: j.getValue(XBoxAxes.RIGHTX);  
        mechDrive.mecanumDrive_Cartesian(-j.getValue(XBoxAxes.LEFTY), j.getValue(XBoxAxes.LEFTX), angleOrient, gyro.getAngle());
    }

    public void driveRobotOriented(EnhancedXBoxController j) {
        printSD();
        double angleOrient = (isHeadingHold)? pidOutput: j.getValue(XBoxAxes.RIGHTX);
        mechDrive.mecanumDrive_Cartesian(-j.getValue(XBoxAxes.LEFTY), j.getValue(XBoxAxes.LEFTX), angleOrient, 0);
    }
    
    public void driveRobotOriented(double vectorX, double vectorY, double rotVector) {
        printSD();
        double angleOrient = (isHeadingHold)? pidOutput: rotVector;
        mechDrive.mecanumDrive_Cartesian(vectorX, vectorY, angleOrient, 0);
    }
    
    public void stop(){
        printSD();
        mechDrive.mecanumDrive_Cartesian(0, 0, 0, 0);
    }

    public void resetNorth() {
        gyro.reset();

    }
    
    public boolean isAtHeading(){
        return pid.onTarget();
    }
    public void setHeadingHold(){
        markedHeading = gyro.getFusedHeading();
        isHeadingHold = true;
        pid.setSetpoint(markedHeading);
        pid.enable();
    }

    public void setHeadingHold(double heading){
        markedHeading = heading;
        isHeadingHold = true;
        pid.setSetpoint(markedHeading);
        pid.enable();
    }
    
    public void stopHeadingHold(){
        isHeadingHold = false;
        pid.disable();
    }
    
    void printSD() {
        SmartDashboard.putNumber("err", pid.getError());
        SmartDashboard.putBoolean("onTarget", pid.onTarget());
    }
}
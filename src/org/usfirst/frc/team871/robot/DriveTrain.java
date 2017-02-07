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

    public void driveFieldOriented(EnhancedXBoxController joy) {
        mechDrive.mecanumDrive_Cartesian(-joy.getValue(XBoxAxes.LEFTY), joy.getValue(XBoxAxes.LEFTX), joy.getValue(XBoxAxes.RIGHTX), gyro.getAngle());
    }

    public void driveRobotOriented(double x, double y, double rot) {
        mechDrive.mecanumDrive_Cartesian(x, y, rot, 0);
    }

    public void driveRobotOriented(EnhancedXBoxController joy) {
        mechDrive.mecanumDrive_Cartesian(-joy.getValue(XBoxAxes.LEFTY), joy.getValue(XBoxAxes.LEFTX), joy.getValue(XBoxAxes.RIGHTX), 0);

    }

    public void resetNorth() {
        gyro.reset();

    }

}
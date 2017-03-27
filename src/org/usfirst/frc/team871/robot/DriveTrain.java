package org.usfirst.frc.team871.robot;

import org.usfirst.frc.team871.tools.DummyPIDSource;
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

	private static final double AXIS_SCALE = Vars.AXIS_SCALEY;
	private final RobotDrive mechDrive;
	private final AHRS gyro;
	private double markedHeading;
	private boolean isHeadingHold;
	private PIDController pid;
	private double pidOutput;

	public DriveTrain(SpeedController frontLeft, SpeedController frontRight, SpeedController backLeft, SpeedController backRight, AHRS gyro) {
		frontRight.setInverted(true);
		backRight.setInverted(true);
		mechDrive = new RobotDrive(frontLeft, backLeft, frontRight, backRight);

		this.gyro = gyro;

		pid = new PIDController(.03, 0, 0, (gyro == null) ? (new DummyPIDSource()) : gyro, new PIDOutput() {

			@Override
			public void pidWrite(double output) {
				pidOutput = output;
			}
		});

		pid.setAbsoluteTolerance(6);
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

		if(gyro != null) LiveWindow.addSensor("Drive Train", "GYRO", gyro);
	}

	public void driveFieldOriented(EnhancedXBoxController j) {
		printSD();
		double angleOrient = (isHeadingHold) ? pidOutput : j.getScaledValue(XBoxAxes.RIGHTX, AXIS_SCALE);

		mechDrive.mecanumDrive_Cartesian(-j.getScaledValue(XBoxAxes.LEFTY, AXIS_SCALE), j.getScaledValue(XBoxAxes.LEFTX, AXIS_SCALE), angleOrient, (gyro == null) ? 0 : gyro.getAngle());
	}

	public void driveFieldOriented(double vectorX, double vectorY, double rotVector) {
		printSD();
		double angleOrient = (isHeadingHold) ? pidOutput : rotVector;
		mechDrive.mecanumDrive_Cartesian(vectorX, vectorY, angleOrient, (gyro == null) ? 0 : gyro.getAngle());
	}

	public void driveRobotOriented(EnhancedXBoxController j) {
		printSD();
		double angleOrient = (isHeadingHold) ? pidOutput : j.getScaledValue(XBoxAxes.RIGHTX, AXIS_SCALE);
		mechDrive.mecanumDrive_Cartesian(-j.getScaledValue(XBoxAxes.LEFTY, AXIS_SCALE), j.getScaledValue(XBoxAxes.LEFTX, AXIS_SCALE), angleOrient, 0);
	}

	public void driveRobotOriented(double vectorX, double vectorY, double rotVector) {
		printSD();
		double angleOrient = (isHeadingHold) ? pidOutput : rotVector;
		mechDrive.mecanumDrive_Cartesian(vectorX, vectorY, angleOrient, 0);
	}

	/**
	 * stop
	 */
	public void stop() {
		printSD();
		mechDrive.mecanumDrive_Cartesian(0, 0, 0, 0);
	}

	public void resetNorth() {
		if(gyro != null) gyro.reset();

	}

	public boolean isAtHeading() {
		return pid.onTarget();
	}

	public void setHeadingHold() {
		System.out.println("set heading hold");
		if(gyro != null) markedHeading = gyro.getFusedHeading();
		isHeadingHold = true;
		pid.setSetpoint(markedHeading);
		pid.enable();
	}

	public void setHeadingHold(double heading) {
		System.out.println("set heading hold");
		markedHeading = heading;
		isHeadingHold = true;
		pid.setSetpoint(markedHeading);
		pid.enable();
	}

	public void stopHeadingHold() {
		isHeadingHold = false;
		pid.disable();
	}

	public void printSD() {
		SmartDashboard.putNumber("err", pid.getError());
		SmartDashboard.putBoolean("onTarget", pid.onTarget());
	}
}
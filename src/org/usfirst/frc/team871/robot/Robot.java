package org.usfirst.frc.team871.robot;

import org.usfirst.frc.team871.tools.ButtonTypes;
import org.usfirst.frc.team871.tools.DigitalLimitSwitch;
import org.usfirst.frc.team871.tools.EnhancedXBoxController;
import org.usfirst.frc.team871.tools.ILimitSwitch;
import org.usfirst.frc.team871.tools.StopWatch;
import org.usfirst.frc.team871.tools.XBoxAxes;
import org.usfirst.frc.team871.tools.XBoxButtons;

import com.ctre.CANTalon;
import com.kauailabs.navx.frc.AHRS;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;

public class Robot extends IterativeRobot {
	final String defaultAuto = "Default";
	final String customAuto = "My Auto";
	String autoSelected;
	SendableChooser<String> chooser = new SendableChooser<>();
	
	DriveTrain drive;
	EnhancedXBoxController joystick;
	Chute chute;
	Lifter lift;
	BergDevice berg;
	StopWatch timer;
	AutonStates autoStates;
	@Override
	public void robotInit() {
		drive = new DriveTrain(	new CANTalon(Vars.FRONT_LEFT_MOTOR),
								new CANTalon(Vars.FRONT_RIGHT_MOTOR),
								new CANTalon(Vars.REAR_LEFT_MOTOR),
								new CANTalon(Vars.REAR_RIGHT_MOTOR),
								new AHRS(Vars.GYRO));
		chute = new Chute(new DigitalInput(Vars.CHUTE_LOADED_SENSOR));
		lift = new Lifter(new CANTalon(Vars.DRUM_MOTOR), (ILimitSwitch) new DigitalLimitSwitch(new DigitalInput(Vars.LIFTER_UPPER_LIMIT)));
		berg = new BergDevice((SpeedController) new CANTalon(Vars.BERG_MOTOR),
												new DigitalInput(Vars.BERG_UPPER_LIMIT),
												new DigitalInput(Vars.BERG_LOWER_LIMIT),
												new DigitalInput(Vars.BERG_DEVICE_LOADED_SENSOR),
												new DoubleSolenoid(Vars.BERG_DEVICE_PISTON_FORWARD, Vars.BERG_DEVICE_PISTON_BACKWARD));
		
		joystick = new EnhancedXBoxController(0);
		joystick.setButtonMode(XBoxButtons.A, ButtonTypes.TOGGLE);
		joystick.setButtonMode(XBoxButtons.B, ButtonTypes.RISING);
		joystick.setButtonMode(XBoxButtons.X, ButtonTypes.RISING);
		joystick.setButtonMode(XBoxButtons.Y, ButtonTypes.RISING);
		joystick.setButtonMode(XBoxButtons.START, ButtonTypes.TOGGLE);
		joystick.setButtonMode(XBoxButtons.BACK, ButtonTypes.TOGGLE);
		joystick.setAxisDeadband(XBoxAxes.LEFTX, .1);
		joystick.setAxisDeadband(XBoxAxes.LEFTY, .1);
		joystick.setAxisDeadband(XBoxAxes.RIGHTX, .1);
		joystick.setAxisDeadband(XBoxAxes.TRIGGER, .1);
		NetworkTable.initialize();
		NetworkTable.getTable("Good Table").putNumber("Gyro Stuff", 6.28d);
		
	}
	
	@Override
	public void autonomousInit() {
		autoSelected = chooser.getSelected();
		System.out.println("Auto selected: " + autoSelected);
		timer = new StopWatch(3000);
		
	}
	
	@Override
	public void autonomousPeriodic() {
		switch(autoStates){
		case DOCKING:
			break;
		case DRIVE:
			break;
		case SEARCH:
			break;
		case STOP:
			break;
		
		}
	
	
	}
	
	@Override
	public void teleopPeriodic() {
		//drive.driveFieldOriented(joystick);
		drive.driveRobotOriented(joystick);
		berg.update(joystick);
		
	}
	
	@Override
	public void testPeriodic() {
		//System.out.println(drive.gyro.getTable() + " " + drive.gyro.getYaw());
	    LiveWindow.run();
	}
}

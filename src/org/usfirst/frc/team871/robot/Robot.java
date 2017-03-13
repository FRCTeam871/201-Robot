package org.usfirst.frc.team871.robot;

import org.usfirst.frc.team871.auton.AutonStates;
import org.usfirst.frc.team871.robot.BergDevice.ControlMode;
import org.usfirst.frc.team871.target.AutoDock;
import org.usfirst.frc.team871.target.ITarget;
import org.usfirst.frc.team871.target.ITargetAcquisition;
import org.usfirst.frc.team871.target.LabViewTargetAcquisition;
import org.usfirst.frc.team871.tools.ButtonTypes;
import org.usfirst.frc.team871.tools.DigitalLimitSwitch;
import org.usfirst.frc.team871.tools.EnhancedXBoxController;
import org.usfirst.frc.team871.tools.Profiler;
import org.usfirst.frc.team871.tools.StopWatch;
import org.usfirst.frc.team871.tools.XBoxAxes;
import org.usfirst.frc.team871.tools.XBoxButtons;
import org.usfirst.frc.team871.tools.XBoxJoypads;

import com.ctre.CANTalon;
import com.kauailabs.navx.frc.AHRS;

import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.first.wpilibj.GenericHID.RumbleType;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick.ButtonType;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Robot extends IterativeRobot {
	final String defaultAuto = "Default";
	final String customAuto = "My Auto";
	String autoSelected;
	SendableChooser<String> chooser = new SendableChooser<>();

	private DriveTrain drive;
	private EnhancedXBoxController joystick;
	private Chute chute;
	private Lifter lift;
	private BergDevice berg;
	private StopWatch timer;
	private AutonStates autoState = AutonStates.DRIVE;
	private ITargetAcquisition targetFinder;
	private AHRS gyro;
	private AutoDock autoDock;
	private DoubleSolenoid andysSuperSecretPiston;

	private double startingPosition;

	private UsbCamera bergCam;
	private UsbCamera chuteCam;

	private boolean dPadDownWasPressed = false;
	private boolean dPadToggled;
	private boolean wasAutoDocking;

	private StopWatch stoppy;
	
	@Override
	public void robotInit() {
		andysSuperSecretPiston = new DoubleSolenoid(Vars.THE_SECRET_PISTON_BACKWARD, Vars.THE_SECRET_PISTON_FOREWARD);
		bergCam = CameraServer.getInstance().startAutomaticCapture(0);
		bergCam.setResolution(320, 240);
		bergCam.setFPS(15);
		chuteCam = CameraServer.getInstance().startAutomaticCapture(1);
		chuteCam.setResolution(320, 240);
		chuteCam.setFPS(15);

		targetFinder = new LabViewTargetAcquisition();

		gyro = new AHRS(Vars.GYRO);
		chute = new Chute(new DigitalInput(Vars.CHUTE_LOADED_SENSOR));
		lift = new Lifter(new CANTalon(Vars.DRUM_MOTOR),
				new DigitalLimitSwitch(new DigitalInput(Vars.LIFTER_UPPER_LIMIT)));
		berg = new BergDevice(new CANTalon(Vars.BERG_MOTOR), new DigitalInput(Vars.BERG_UPPER_LIMIT),
				new DigitalInput(Vars.BERG_LOWER_LIMIT), new DigitalInput(Vars.BERG_DEVICE_LOADED_SENSOR),
				new DoubleSolenoid(Vars.BERG_DEVICE_PISTON_FORWARD, Vars.BERG_DEVICE_PISTON_BACKWARD));

		drive = new DriveTrain(new CANTalon(Vars.FRONT_LEFT_MOTOR), new CANTalon(Vars.FRONT_RIGHT_MOTOR),
				new CANTalon(Vars.REAR_LEFT_MOTOR), new CANTalon(Vars.REAR_RIGHT_MOTOR), gyro);

		joystick = new EnhancedXBoxController(0);
		joystick.setButtonMode(XBoxButtons.A, ButtonTypes.RISING);
		joystick.setButtonMode(XBoxButtons.B, ButtonTypes.RISING);
		joystick.setButtonMode(XBoxButtons.X, ButtonTypes.MOMENTARY);
		joystick.setButtonMode(XBoxButtons.Y, ButtonTypes.RISING);
		joystick.setButtonMode(XBoxButtons.START, ButtonTypes.TOGGLE);
		joystick.setButtonMode(XBoxButtons.BACK, ButtonTypes.TOGGLE);
		joystick.setButtonMode(XBoxButtons.LBUMPER, ButtonTypes.MOMENTARY);
		joystick.setButtonMode(XBoxButtons.RBUMPER, ButtonTypes.MOMENTARY);
		joystick.setAxisDeadband(XBoxAxes.LEFTX, .1);
		joystick.setAxisDeadband(XBoxAxes.LEFTY, .1);
		joystick.setAxisDeadband(XBoxAxes.RIGHTX, .1);
		joystick.setAxisDeadband(XBoxAxes.TRIGGER, .1);
		
		joystick.setAxisDeadband(XBoxAxes.RTRIGGER, .1);
		joystick.setAxisDeadband(XBoxAxes.LTRIGGER, .1);
		// joystick.getValue(pad)

		gyro.zeroYaw();

		autoDock = new AutoDock(drive, gyro, true);
	}

	@Override
	public void robotPeriodic() {
		updateCameraParams();
		SmartDashboard.putNumber("Gyro", gyro.getFusedHeading());

		NetworkTable tab = NetworkTable.getTable("SmartDashboard");
		DriverStation ds = DriverStation.getInstance();

		tab.putString("alliance", ds.getAlliance().toString());
		tab.putNumber("gameTime", ds.getMatchTime());
		tab.putNumber("battery", ds.getBatteryVoltage());
		tab.putNumber("realStationNumber", ds.getLocation());

		SmartDashboard.putString("driveMode", joystick.getValue(XBoxButtons.BACK) ? "Robot" : "Field");
		SmartDashboard.putBoolean("chuteLoaded", chute.isLoaded());
		SmartDashboard.putString("bergMode", berg.getMode().name());
		SmartDashboard.putBoolean("atRopeTop", !lift.isAtTop());
		SmartDashboard.putBoolean("bergLoaded", !berg.isGearLoaded());

		boolean nowPressed = joystick.getValue(XBoxJoypads.LJOYPAD) == 180;

		if (!dPadDownWasPressed && nowPressed) {
			dPadToggled = !dPadToggled;
		}

		SmartDashboard.putBoolean("cameraDirection", dPadToggled);
		dPadDownWasPressed = nowPressed;

		updateSuperSmartAI();
		
	}

	@Override
	public void autonomousInit() {
		gyro.zeroYaw();
		timer = new StopWatch(1500);

		drive.setHeadingHold(0);
		startingPosition = SmartDashboard.getNumber("stationNumber", 1);
		autoState = AutonStates.DRIVE;
		berg.setModeSemi();
		berg.reset();
	}

	@Override
	public void autonomousPeriodic() {
		SmartDashboard.putString("Auton", autoState.toString());

		berg.update(joystick);
		autoDockUpdate();
	}

	@Override
	public void teleopInit() {
		drive.stopHeadingHold();
		berg.setModeAuto();
		berg.reset();
	}

	public void autoDockUpdate() {
		SmartDashboard.putString("Auton", autoState.toString());

		switch (autoState) {
		case DRIVE:
			if (!timer.timeUp()) {
				drive.driveRobotOriented(.60, 0.05, 0);
			} else {
				drive.stop();

				if (startingPosition == 0) {
					drive.setHeadingHold(45);
				} else if (startingPosition == 1) {
					drive.setHeadingHold(0);
				} else {
					drive.setHeadingHold(-45);
				}
				autoState = AutonStates.DOCKING;
				System.out.println("Going to search");
			}

			break;
		case SEARCH:
			if (targetFinder.isTargetAvailable()) {
				autoState = AutonStates.DOCKING;
			} else {
				// TODO: Make a search pattern
				drive.driveRobotOriented(0, 0, 0);
			}

			break;

		case DOCKING:
			if (!targetFinder.isTargetAvailable()) {
				drive.driveRobotOriented(0, 0, 0);
				// autoState = AutonStates.SEARCH;
			} else {
				autoDock.dock(targetFinder.getTarget());
			}

			if (autoDock.isDocked()) {
				autoState = AutonStates.BEGIN_DROP;
				timer = new StopWatch(2800);
				berg.advanceState();
			}
			break;

		case BEGIN_DROP:
			System.out.println(timer.getAppriseTime());
			// autoDock.dock(targetFinder.getTarget());
			if (timer.timeUp()) {
				autoState = AutonStates.POSITION_GEAR;
				timer = new StopWatch(500);
			}
			break;

		case POSITION_GEAR:
			drive.driveRobotOriented(.4, 0.05, 0);
			if (timer.timeUp()) {
				autoState = AutonStates.DROP_GEAR;
				timer = new StopWatch(250);
				berg.advanceState();
			}
			break;

		case DROP_GEAR:
			if (timer.timeUp()) {
				timer = new StopWatch(2000);
				autoState = AutonStates.PULL_OUT;
			}
			break;

		case PULL_OUT:
			drive.driveRobotOriented(-.6, -0.05, 0);
			if (timer.timeUp()) {
				autoState = AutonStates.STOP;
				berg.advanceState();
			}
			break;

		case STOP:
			drive.stop();
			break;
		default:
			drive.driveRobotOriented(0, 0, 0);
			break;
		}

	}

	@Override
	public void teleopPeriodic() {

		ITarget tar = targetFinder.getTarget();

		if (!wasAutoDocking) {
			autoDock.reset();
		}

		if (joystick.getDebouncedButton(Vars.AUTO_DOCK)) {
			drive.setHeadingHold();
			berg.setModeSemi(true);
			berg.reset();
			autoState = AutonStates.DOCKING;
			wasAutoDocking = true;
		}

		if (joystick.getValue(Vars.AUTO_DOCK) && (tar != null)) {
			autoDockUpdate();
		} else {
			if(wasAutoDocking) {
				drive.stopHeadingHold();
				berg.setModeAuto();
				wasAutoDocking = false;
			}
			
			if (joystick.getValue(Vars.NORTH_RESET)) {
				drive.resetNorth();
			}
			
			if (joystick.getValue(Vars.DRIVE_MODE_CHANGE)) {
				drive.driveRobotOriented(joystick);
			} else {
				drive.driveFieldOriented(joystick);
			}
			
		}
		
//		if (joystick.getDebouncedButton(Vars.BERG_ADVANCE) && berg.getMode() == ControlMode.SEMI) {
//			berg.advanceState();
//		}

		Profiler.getProfiler("TeleopPeriod").mark();
		Profiler.getProfiler("TeleopLength").reset();
		// SmartDashboard.putNumber("Gyro", gyro.getAngle());

		if (joystick.getValue(XBoxButtons.X)) { // change button type
			lift.startSpin();
		} else {
			lift.stopSpin();
		}

		// lift.update();
		lift.climb(joystick);
		berg.update(joystick);

		Profiler.getProfiler("TeleopLength").mark();
		// andysSuperSecretPiston.set(joystick.getValue(XBoxButtons.LBUMPER) ?
		// Value.kForward : Value.kOff);
		// if (berg.getMode() == ControlMode.SEMI){
		// berg.grabPiston.set(joystick.getValue(XBoxButtons.LBUMPER) ?
		// Value.kForward : Value.kOff);
		// }
	}

	@Override
	public void testPeriodic() {
		LiveWindow.run();
	}

	public void updateCameraParams() {
		if (SmartDashboard.getBoolean("Update Camera", false)) {
			
			SmartDashboard.putBoolean("Update Camera", false);
			
			double wb = SmartDashboard.getNumber("White Bal", 45);
			double exp = SmartDashboard.getNumber("Exposure", 2);

			bergCam.setWhiteBalanceManual((int) wb);
			bergCam.setExposureManual((int) exp);
			chuteCam.setWhiteBalanceManual((int) wb);
			chuteCam.setExposureManual((int) exp);
		}
	}
	
	public void updateSuperSmartAI() {

		double ltrig = joystick.getValue(XBoxAxes.LTRIGGER);
		double rtrig = joystick.getValue(XBoxAxes.RTRIGGER);

		boolean pressed = (ltrig > 0) || (rtrig > 0);

		if (pressed) {
			if (stoppy == null) {
				stoppy = new StopWatch(500);
			}

			if (stoppy.timeUp()) {
				berg.setModeManual();
			}

		} else {
			stoppy = null;
		}

		
		joystick.setButtonMode(XBoxButtons.A, ButtonTypes.MOMENTARY);
		if(joystick.getValue(XBoxButtons.A)){
			if(berg.getMode() == ControlMode.MANUAL){
				berg.setModeAuto();
			}
		}
		joystick.setButtonMode(XBoxButtons.A, ButtonTypes.RISING);
		
	}
	
}

package org.usfirst.frc.team871.robot;

import java.lang.reflect.Field;

import org.usfirst.frc.team871.auton.AutonStates;
import org.usfirst.frc.team871.robot.BergDevice.ControlMode;
import org.usfirst.frc.team871.target.AutoDock;
import org.usfirst.frc.team871.target.ITarget;
import org.usfirst.frc.team871.target.ITargetAcquisition;
import org.usfirst.frc.team871.target.LabViewTargetAcquisition;
import org.usfirst.frc.team871.tools.ButtonTypes;
import org.usfirst.frc.team871.tools.DigitalLimitSwitch;
import org.usfirst.frc.team871.tools.EnhancedXBoxController;
import org.usfirst.frc.team871.tools.Mic;
import org.usfirst.frc.team871.tools.Profiler;
import org.usfirst.frc.team871.tools.Rumble;
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
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Robot extends IterativeRobot {
	private static Robot robot;
	
	final String defaultAuto = "Default";
	final String customAuto = "My Auto";
	String autoSelected;
	SendableChooser<String> chooser = new SendableChooser<>();

	private DriveTrain drive;
	private EnhancedXBoxController joystick;
	private EnhancedXBoxController joystick2;
	private Rumble rumble;
	private Rumble rumble2;
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
	
	private boolean didEndgameRumble = false;
	
	private boolean wasSecret = false;
	private Arduino arduino;
	
	public Robot() {
		robot = this;
	}
	
	@Override
	public void robotInit() {
		andysSuperSecretPiston = new DoubleSolenoid(Vars.THE_SECRET_PISTON_BACKWARD, Vars.THE_SECRET_PISTON_FORWARD);
		if(!Vars.CLASSMATE_TEST){
			bergCam = CameraServer.getInstance().startAutomaticCapture(0);
			bergCam.setResolution(160, 120);
			bergCam.setFPS(15);
			chuteCam = CameraServer.getInstance().startAutomaticCapture(1);
			chuteCam.setResolution(160, 120);
			chuteCam.setFPS(15);
		}

		targetFinder = new LabViewTargetAcquisition();

		if(!Vars.CLASSMATE_TEST) gyro = new AHRS(Vars.GYRO_PORT);
		
//		try{
//			Field f = gyro.getClass().getField("io");
//			f.setAccessible(true);
//			Object obj = f.get(gyro);
//			System.out.println(obj.getClass().getName());
//		}catch(Exception e){
//			
//		}
		
		chute = new Chute(new DigitalInput(Vars.CHUTE_LOADED_SENSOR));
		lift = new Lifter(new CANTalon(Vars.DRUM_MOTOR),
				
				new DigitalLimitSwitch(new DigitalInput(Vars.LIFTER_UPPER_LIMIT)));
		berg = new BergDevice(new CANTalon(Vars.BERG_MOTOR), new DigitalInput(Vars.BERG_UPPER_LIMIT),
				new DigitalInput(Vars.BERG_LOWER_LIMIT), new DigitalInput(Vars.BERG_DEVICE_LOADED_SENSOR),
				new DoubleSolenoid(Vars.BERG_DEVICE_PISTON_FORWARD, Vars.BERG_DEVICE_PISTON_BACKWARD));

		drive = new DriveTrain(new CANTalon(Vars.FRONT_LEFT_MOTOR), new CANTalon(Vars.FRONT_RIGHT_MOTOR),
				new CANTalon(Vars.REAR_LEFT_MOTOR), new CANTalon(Vars.REAR_RIGHT_MOTOR), gyro);

		joystick = new EnhancedXBoxController(0);
		joystick2 = new EnhancedXBoxController(1);
		rumble = new Rumble(joystick, 0.5d);
		rumble2 = new Rumble(joystick2, 0.5d);
		
		joystick.setButtonMode(Vars.BERG_ADVANCE, ButtonTypes.RISING);
		joystick2.setButtonMode(Vars.LIFT_RELEASE, ButtonTypes.RISING);
		joystick.setButtonMode(Vars.LIFTER_TOGGLE, ButtonTypes.MOMENTARY);
		joystick.setButtonMode(Vars.BERG_AUTO_RESET, ButtonTypes.RISING);
		//joystick.setButtonMode(XBoxButtons.START, ButtonTypes.MOMENTARY);
		joystick.setButtonMode(Vars.DRIVE_MODE_CHANGE, ButtonTypes.TOGGLE);
		joystick.setButtonMode(Vars.MANUAL_BERG_CLAMP, ButtonTypes.MOMENTARY);
		joystick.setButtonMode(Vars.AUTO_DOCK, ButtonTypes.MOMENTARY);
		
		double axisDead = 0.2;
		
		joystick.setAxisDeadband(XBoxAxes.LEFTX, axisDead);
		joystick.setAxisDeadband(XBoxAxes.LEFTY, axisDead);
		joystick.setAxisDeadband(XBoxAxes.RIGHTX, axisDead);
		joystick.setAxisDeadband(XBoxAxes.TRIGGER, .1);
		
		joystick.setAxisDeadband(XBoxAxes.RTRIGGER, .1);
		joystick.setAxisDeadband(XBoxAxes.LTRIGGER, .1);
		// joystick.getValue(pad)
		
		if(gyro != null) gyro.zeroYaw();

		autoDock = new AutoDock(drive, gyro, true);
		
		arduino = new Arduino();
//		arduino.setRingColor(0, 255, 0); //gets set in disabledInit()
//		arduino.setStripsColor(0, 255, 0);
		
		//arduino.write("c/0|255|0/t");
	}

	double prevBrightness = 1d;
	
	boolean lastCb = false;
	
	@Override
	public void robotPeriodic() {
		
		boolean wasPressed = wasSecret;
		boolean nowPressed2 = joystick2.getValue(XBoxButtons.B);
		
		boolean nowCb = SmartDashboard.getBoolean("colorBlind", false);
		if(nowCb != lastCb){
			lastCb = nowCb;
			arduino.setColorBlind(nowCb);
		}
		
		double br = SmartDashboard.getNumber("ledBrightness", 1d);
		if(br != prevBrightness){
			arduino.setBrightness((float) br);
			prevBrightness = br;
		}
		
		if(nowPressed2 != wasPressed){
			if(nowPressed2){
				arduino.write("r/");
			}
			wasSecret = nowPressed2;
		}
		
		updateCameraParams();
		if(gyro != null) SmartDashboard.putNumber("Gyro", gyro.getFusedHeading());

		NetworkTable tab = NetworkTable.getTable("SmartDashboard");
		DriverStation ds = DriverStation.getInstance();
		
		
		tab.putString("alliance", ds.getAlliance().toString());
		double matchTime = ds.getMatchTime();
		
		//System.out.println(matchTime);
		
		if(isEnabled()){
			if(isAutonomous()){
				matchTime = 15 - matchTime;
			}else{
				matchTime = (150) - matchTime;
			}
		}else{
			matchTime = 0;
		}
		
		//System.out.println(matchTime);
		
		if(matchTime >= ((150-30)) && !didEndgameRumble && !isAutonomous() && isEnabled()){
			didEndgameRumble = true;
			rumble.longPulse();
			rumble2.longPulse();
			//System.out.println("rumble");
		}
		
		tab.putNumber("gameTime", matchTime);
		tab.putNumber("battery", ds.getBatteryVoltage());
		tab.putNumber("realStationNumber", ds.getLocation());

		SmartDashboard.putString("driveMode", joystick.getValue(Vars.DRIVE_MODE_CHANGE) ? "Robot" : "Field");
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
		
		try{
			lift.printCurrent();
		}catch(Exception e){}
		
		arduino.update();
		
		if(joystick2.getDebouncedButton(XBoxButtons.LEFTSTICK)){
			rumble.longPulse();
			rumble2.longPulse();
			//System.out.println("rumble");
		}
		
	}

	@Override
	public void autonomousInit() {
		if(gyro != null) gyro.zeroYaw();
		timer = new StopWatch(1500);

		drive.setHeadingHold(0);
		startingPosition = SmartDashboard.getNumber("stationNumber", 1);
		autoState = AutonStates.DRIVE;
		berg.setModeSemi();
		berg.reset();
	}

	@Override
	public void autonomousPeriodic() {
		boolean doAuton = SmartDashboard.getBoolean("doAuton", true);
		SmartDashboard.putString("Auton", doAuton ? autoState.toString() : "DISABLED");

		if(doAuton){
			berg.update(joystick, joystick2);
			autoDockUpdate();
		}
	}

	@Override
	public void teleopInit() {
		didEndgameRumble = false;
		arduino.setStripsColor(255, 80, 0); // cs/255|80|0/t
		drive.stopHeadingHold();
		berg.setModeAuto();
		berg.reset();
	}

	boolean sentFailColor = false;
	
	public void autoDockUpdate() {
		SmartDashboard.putString("Auton", autoState.toString());

		switch (autoState) {
		case DRIVE:
			if (!timer.timeUp()) {
				drive.driveRobotOriented(.60, -0.05, 0);
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
				sentFailColor = false;
				System.out.println("Going to search");
			}

			break;
		case SEARCH:
			if (targetFinder.isTargetAvailable() || Vars.CLASSMATE_TEST) {
				autoState = AutonStates.DOCKING;
				Robot.getArduino().pulseStrips(0, 255, 0, 500);
			} else {
				// TODO: Make a search pattern
				drive.driveRobotOriented(0, 0, 0);
				if(!sentFailColor){
					Robot.getArduino().pulseStrips(255, 0, 0, 1000);
					sentFailColor = true;
				}
			}

			break;

		case DOCKING:
			if (!targetFinder.isTargetAvailable() && !Vars.CLASSMATE_TEST) {
				drive.driveRobotOriented(0, 0, 0);
				// autoState = AutonStates.SEARCH;
				if(!sentFailColor){
					Robot.getArduino().pulseStrips(255, 0, 0, 1000);
					sentFailColor = true;
				}
			} else {
				ITarget targ = targetFinder.getTarget();
				double dist = targ != null ? targ.getDistance() : Vars.AUTO_DIST;
				
//				int pulseSpeed = 1000;
//				
//				if(dist < Vars.AUTO_DIST){
//					pulseSpeed = 400;
//				}else{
//					dist -= Vars.AUTO_DIST;
//					dist *= 10;
//					
//					pulseSpeed = (int) dist + 400;
//				}
//				
//				Robot.getArduino().pulseStrips(0, 255, 0, pulseSpeed);
				
				
				autoDock.dock(targ);
			}

			if (autoDock.isDocked() || (joystick2.getDebouncedButton(XBoxButtons.START) && Vars.CLASSMATE_TEST)) {
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
				Robot.getArduino().theaterChaseStrips(0, 50, 0, 0, 255, 0, -100, 50); // tcs/0|50|0|0|255|0|-100|50/t
			}
			break;

		case DROP_GEAR:
			if (timer.timeUp()) {
				Robot.getArduino().rainbowStrips(); // rs/
				timer = new StopWatch(2000);
				autoState = AutonStates.PULL_OUT;
			}
			break;

		case PULL_OUT:
			drive.driveRobotOriented(-.6, -0.05, 0);
			if (timer.timeUp()) {
				autoState = AutonStates.STOP;
				berg.advanceState();
				Robot.getArduino().setStripsColor(255, 0, 0);
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

		if(joystick2.getDebouncedButton(XBoxButtons.BACK) && Vars.CLASSMATE_TEST){
			berg.overrideSensors();
		}
		
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
			
			if (joystick.getValue(Vars.DPAD) == 0) {
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

		if (joystick.getValue(Vars.LIFTER_TOGGLE)) { // change button type
			lift.startSpin();
		} else {
			lift.stopSpin();
		}

		// lift.update();
		lift.climb(joystick2);
		berg.update(joystick, joystick2);

		Profiler.getProfiler("TeleopLength").mark();
		andysSuperSecretPiston.set(joystick2.getValue(Vars.ANDYS_SUPER_SECRET_PISTON) ? Value.kForward : Value.kOff);
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
		if(!Vars.CLASSMATE_TEST){
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

		
		joystick.setButtonMode(Vars.SWITCH_FROM_MANUAL_TO_AUTO, ButtonTypes.MOMENTARY);
		if(joystick.getValue(Vars.SWITCH_FROM_MANUAL_TO_AUTO)){
			if(berg.getMode() == ControlMode.MANUAL){
				berg.setModeAuto();
			}
		}
		joystick.setButtonMode(Vars.SWITCH_FROM_MANUAL_TO_AUTO, ButtonTypes.RISING);
		
	}
	
	@Override
	public void disabledInit() {
		arduino.setRingColor(0, 255, 0);
		arduino.setStripsColor(127, 0, 0);
		Mic.drop();
	}
	
	public static Arduino getArduino(){
		return robot.arduino;
	}
	
}

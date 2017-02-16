package org.usfirst.frc.team871.robot;

import org.usfirst.frc.team871.auton.AutonStates;
import org.usfirst.frc.team871.target.AutoDock;
import org.usfirst.frc.team871.target.ITargetAcquisition;
import org.usfirst.frc.team871.target.LabViewTargetAcquisition;
import org.usfirst.frc.team871.tools.ButtonTypes;
import org.usfirst.frc.team871.tools.DigitalLimitSwitch;
import org.usfirst.frc.team871.tools.EnhancedXBoxController;
import org.usfirst.frc.team871.tools.Profiler;
import org.usfirst.frc.team871.tools.StopWatch;
import org.usfirst.frc.team871.tools.XBoxAxes;
import org.usfirst.frc.team871.tools.XBoxButtons;

import com.ctre.CANTalon;
import com.kauailabs.navx.frc.AHRS;

import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
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
    
    
    private double startingPosition;
    
    UsbCamera cam;
    
    @Override
    public void robotInit() {        
        cam = CameraServer.getInstance().startAutomaticCapture();
        cam.setResolution(320, 240);
        targetFinder = new LabViewTargetAcquisition();
        
        gyro = new AHRS(Vars.GYRO);
        chute = new Chute(new DigitalInput(Vars.CHUTE_LOADED_SENSOR));
        lift = new Lifter(new CANTalon(Vars.DRUM_MOTOR), new DigitalLimitSwitch(new DigitalInput(Vars.LIFTER_UPPER_LIMIT)));
        berg = new BergDevice(  new CANTalon(Vars.BERG_MOTOR),
                                new DigitalInput(Vars.BERG_UPPER_LIMIT),
                                new DigitalInput(Vars.BERG_LOWER_LIMIT),
                                new DigitalInput(Vars.BERG_DEVICE_LOADED_SENSOR),
                                new DoubleSolenoid(Vars.BERG_DEVICE_PISTON_FORWARD, Vars.BERG_DEVICE_PISTON_BACKWARD));
        
        drive = new DriveTrain( new CANTalon(Vars.FRONT_LEFT_MOTOR),
                new CANTalon(Vars.FRONT_RIGHT_MOTOR),
                new CANTalon(Vars.REAR_LEFT_MOTOR),
                new CANTalon(Vars.REAR_RIGHT_MOTOR),
                gyro);
        
        joystick = new EnhancedXBoxController(0);
        joystick.setButtonMode(XBoxButtons.A, ButtonTypes.TOGGLE);
        joystick.setButtonMode(XBoxButtons.B, ButtonTypes.RISING);
        joystick.setButtonMode(XBoxButtons.X, ButtonTypes.MOMENTARY);
        joystick.setButtonMode(XBoxButtons.Y, ButtonTypes.RISING);
        joystick.setButtonMode(XBoxButtons.START, ButtonTypes.TOGGLE);
        joystick.setButtonMode(XBoxButtons.BACK, ButtonTypes.TOGGLE);
        joystick.setAxisDeadband(XBoxAxes.LEFTX, .1);
        joystick.setAxisDeadband(XBoxAxes.LEFTY, .1);
        joystick.setAxisDeadband(XBoxAxes.RIGHTX, .1);
        joystick.setAxisDeadband(XBoxAxes.TRIGGER, .1);
        
        updateCameraParams();
        
        gyro.zeroYaw();
    }

    @Override
    public void autonomousInit() {
        updateCameraParams();
        gyro.zeroYaw();
        timer = new StopWatch(2000);
        autoDock = new AutoDock(drive, gyro, true);
        drive.setHeadingHold(0);
        startingPosition = SmartDashboard.getNumber("stationNumber", 1);
        drive.setHeadingHold(0);
        autoState = AutonStates.DRIVE;
        berg.setModeSemi();
        berg.reset();
    }

    @Override
    public void autonomousPeriodic() {
        SmartDashboard.putNumber("Gyro", gyro.getFusedHeading());
        SmartDashboard.putString("Auton", autoState.toString());
        berg.update(joystick);
        switch(autoState){
            case DRIVE: 
                if (!timer.timeUp()){
                   drive.mechDrive.mecanumDrive_Cartesian(.30, 0, 0, 0);
                } else {
                    drive.stop();
                    
                    if (startingPosition == 0){
                        drive.setHeadingHold(45);
                    }else if (startingPosition == 1){
                        drive.setHeadingHold(0);
                    }else{
                        drive.setHeadingHold(-45);
                    }
                    autoState = AutonStates.SEARCH;
                    System.out.println("Going to search");
                }
                
                break;
            case SEARCH: 
                if (targetFinder.isTargetAvailable()){ 
                    autoState = AutonStates.DOCKING;
                    System.out.println("Going to Docking");
                } else {
                    //TODO: Make a search pattern
                    drive.driveRobotOriented(0,0,0);
                }
                
                break;
                
            case DOCKING:
                SmartDashboard.putBoolean("atHeading", drive.isAtHeading());
                if (targetFinder.isTargetAvailable() && drive.isAtHeading()){
                    System.out.println("DOCKING");
                        autoDock.dock(targetFinder.getTarget());
                }else{
                    System.out.println("NO TARGET");
                    drive.driveRobotOriented(0, 0, 0);
                   //baseAngle = gyro.getAngle();
                   //autoState = AutonStates.SEARCH;
                }
                
                if (autoDock.isDocked()){
                    System.out.println("Going to STOP");
                    autoState = AutonStates.STOP;
                }
                break;
            case STOP:
                drive.stop();
                break;
            default:
                break;
        }

    }
    
    @Override
    public void teleopInit() {
        drive.stopHeadingHold();
        berg.setModeAuto();
        berg.reset();
    }

    @Override
    public void teleopPeriodic() {
        
        if(joystick.getDebouncedButton(XBoxButtons.START)){
            berg.advanceState();
        }
        
        Profiler.getProfiler("TeleopPeriod").mark();
        Profiler.getProfiler("TeleopLength").reset();
        SmartDashboard.putNumber("Gyro", gyro.getAngle());
        
        if (joystick.getValue(XBoxButtons.BACK)) {
            drive.driveRobotOriented(joystick);
        } else {
            drive.driveFieldOriented(joystick);
        }
        
        if (joystick.getValue(XBoxButtons.X)) { //change button type
            lift.startSpin();
        }else{
            lift.stopSpin();
        }

        if(joystick.getValue(XBoxButtons.B)) {
            gyro.zeroYaw();
        }
        
//        lift.update();
        lift.climb(joystick);
        berg.update(joystick);
        
        Profiler.getProfiler("TeleopLength").mark();
    }

    @Override
    public void testPeriodic() {
        LiveWindow.run();
    }
    
    public void updateCameraParams() {
        //if(SmartDashboard.getBoolean("Update Camera",false)) {
            double wb = SmartDashboard.getNumber("White Bal",2);
            double exp = SmartDashboard.getNumber("Exposure", 2);
        
            cam.setExposureManual((int)exp);
            cam.setWhiteBalanceManual((int)wb);
        //}
    }
}

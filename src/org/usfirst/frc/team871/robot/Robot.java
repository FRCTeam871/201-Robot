package org.usfirst.frc.team871.robot;

import org.usfirst.frc.team871.auton.AutonStates;
import org.usfirst.frc.team871.target.AutoDock;
import org.usfirst.frc.team871.target.ITargetAcquisition;
import org.usfirst.frc.team871.target.LabViewTargetAcquisition;
import org.usfirst.frc.team871.tools.ButtonTypes;
import org.usfirst.frc.team871.tools.DigitalLimitSwitch;
import org.usfirst.frc.team871.tools.EnhancedXBoxController;
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
    private AutoDock dock;
    private StopWatch timer;
    private AutonStates autoState;
    private ITargetAcquisition targetFinder;
    private AHRS gyro;
    private AutoDock autoDock;
    
    private double baseAngle;
    private int turningDirection = 1;
    private boolean isBergDevice;
    UsbCamera cam;
    
    @Override
    public void robotInit() {        
        //dock = new AutoDock(drive, null);// TODO: Pass in new target acquisition
                                         // object
        
        cam = CameraServer.getInstance().startAutomaticCapture();
        cam.setResolution(320, 240);
        
        gyro = new AHRS(Vars.GYRO);
        chute = new Chute(new DigitalInput(Vars.CHUTE_LOADED_SENSOR));
        lift = new Lifter(new CANTalon(Vars.DRUM_MOTOR), new DigitalLimitSwitch(new DigitalInput(Vars.LIFTER_UPPER_LIMIT)));
        berg = new BergDevice(  new CANTalon(Vars.BERG_MOTOR),
                                new DigitalInput(Vars.BERG_UPPER_LIMIT),
                                new DigitalInput(Vars.BERG_LOWER_LIMIT),
                                new DigitalInput(Vars.BERG_DEVICE_LOADED_SENSOR),
                                new DoubleSolenoid(Vars.BERG_DEVICE_PISTON_FORWARD, Vars.BERG_DEVICE_PISTON_BACKWARD));
        autoDock = new AutoDock(drive, new LabViewTargetAcquisition(), isBergDevice);
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
        
        isBergDevice = false;
        
    }

    @Override
    public void autonomousInit() {
        updateCameraParams();
        autoSelected = chooser.getSelected();
        System.out.println("Auto selected: " + autoSelected);
        timer = new StopWatch(3000);
    }

    @Override
    public void autonomousPeriodic() {
        switch(autoState){
            case DRIVE: 
                if (!timer.timeUp()){
                    drive.mechDrive.mecanumDrive_Cartesian(.75, 0, 0, 0);
                } else {
                    baseAngle = gyro.getAngle();
                    drive.driveRobotOriented(0, 0, 1);
                    autoState = AutonStates.SEARCH;
                }
                
                break;
            case SEARCH: 
                if (targetFinder.isTargetAvailable()){ 
                    autoState = AutonStates.DOCKING;
                } else {
                    if (Math.abs(gyro.getAngle() - baseAngle) >= 45){
                        
                       turningDirection *= -1;
                        
                    } 
                    drive.driveRobotOriented(0, 0, turningDirection);
                }
                
                break;
                
            case DOCKING:
                if (targetFinder.isTargetAvailable()){
                    dock.dock();
                } else {
                    baseAngle = gyro.getAngle();
                    autoState = AutonStates.SEARCH;
                }
                
                if (targetFinder.getTarget().getDistance() /**find thresholded value**/ == 5){
                    timer = new StopWatch(5000);
                    autoState = AutonStates.STOP;
                }
                
                break;
            case STOP:
                drive.stop();
                break;
            case PULL_OUT:
                //TODO optional retry state
                break;
        }

    }

    @Override
    public void teleopPeriodic() {
        updateCameraParams();
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

        lift.update();
        berg.update(joystick);
    }

    @Override
    public void testPeriodic() {
        LiveWindow.run();
    }
    
    public void updateCameraParams() {
        double wb = SmartDashboard.getNumber("White Bal",2);
        double exp = SmartDashboard.getNumber("Expoosure", 2);
        
        cam.setExposureManual((int)exp);
        cam.setWhiteBalanceManual((int)wb);
    }
}

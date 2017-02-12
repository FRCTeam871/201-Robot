package org.usfirst.frc.team871.robot;

import org.usfirst.frc.team871.auton.AutonGearDropper;
import org.usfirst.frc.team871.target.ITargetAcquisition;
import org.usfirst.frc.team871.target.LabViewTargetAcquisition;
import org.usfirst.frc.team871.tools.Profiler;
import org.usfirst.frc.team871.tools.controller.ButtonTypes;
import org.usfirst.frc.team871.tools.controller.EnhancedXBoxController;
import org.usfirst.frc.team871.tools.controller.XBoxAxes;
import org.usfirst.frc.team871.tools.controller.XBoxButtons;
import org.usfirst.frc.team871.tools.sensors.DigitalLimitSwitch;

import com.ctre.CANTalon;
import com.kauailabs.navx.frc.AHRS;

import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Robot extends IterativeRobot {
    private DriveTrain drive;
    private EnhancedXBoxController joystick;
    //private Chute chute;
    private Lifter lift;
    private BergDevice berg;
    private ITargetAcquisition targetFinder;
    private AHRS gyro;
    AutonGearDropper auton;
    
    UsbCamera cam;
    
    @Override
    public void robotInit() {        
        cam = CameraServer.getInstance().startAutomaticCapture();
        cam.setResolution(320, 240);
        targetFinder = new LabViewTargetAcquisition();
        
        gyro = new AHRS(Vars.GYRO);
        //chute = new Chute(new DigitalInput(Vars.CHUTE_LOADED_SENSOR));
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
    	auton = new AutonGearDropper(drive, berg, gyro, targetFinder);
    }

    @Override
    public void autonomousPeriodic() {
    	auton.update();
    }

    @Override
    public void teleopPeriodic() {
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
        
        lift.update();
        berg.update(joystick);
        
        Profiler.getProfiler("TeleopLength").mark();
    }

    @Override
    public void testPeriodic() {
        LiveWindow.run();
    }
    
    public void updateCameraParams() {
        double wb = SmartDashboard.getNumber("White Bal",2);
        double exp = SmartDashboard.getNumber("Exposure", 2);
    
        cam.setExposureManual((int)exp);
        cam.setWhiteBalanceManual((int)wb);
    }
}

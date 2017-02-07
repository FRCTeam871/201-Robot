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
    AutoDock dock;
    StopWatch timer;

    @Override
    public void robotInit() {
        drive = new DriveTrain(new CANTalon(Vars.FRONT_LEFT_MOTOR), new CANTalon(Vars.FRONT_RIGHT_MOTOR), new CANTalon(Vars.REAR_LEFT_MOTOR), new CANTalon(Vars.REAR_RIGHT_MOTOR), new AHRS(Vars.GYRO));
        dock = new AutoDock(drive, null);// TODO: Pass in new target acquisition
                                         // object
        chute = new Chute(new DigitalInput(Vars.CHUTE_LOADED_SENSOR));
        lift = new Lifter(new CANTalon(Vars.DRUM_MOTOR), new DigitalLimitSwitch(new DigitalInput(Vars.LIFTER_UPPER_LIMIT)));
        berg = new BergDevice((SpeedController) new CANTalon(Vars.BERG_MOTOR), new DigitalInput(Vars.BERG_UPPER_LIMIT), new DigitalInput(Vars.BERG_LOWER_LIMIT), new DigitalInput(Vars.BERG_DEVICE_LOADED_SENSOR), new DoubleSolenoid(Vars.BERG_DEVICE_PISTON_FORWARD, Vars.BERG_DEVICE_PISTON_BACKWARD));
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
    }

    boolean isBergDevice = false;
    AutoDock autoDock = new AutoDock(drive, null, isBergDevice);

    @Override
    public void autonomousInit() {
        autoSelected = chooser.getSelected();
        System.out.println("Auto selected: " + autoSelected);
        timer = new StopWatch(3000);
    }

    @Override
    public void autonomousPeriodic() {
        if (!timer.timeUp()) {
            //FIXME:  DriveTrain needs method to drive a specific direction
            //drive.driveRobotOriented(1, 0, 0);
        } else {
            //drive.driveRobotOriented(0, 0, 0);
            dock.dock();
        }

    }

    @Override
    public void teleopPeriodic() {

        if (joystick.getValue(XBoxButtons.BACK)) {
            drive.driveRobotOriented(joystick);
        } else {
            drive.driveFieldOriented(joystick);
        }
        if (joystick.getValue(XBoxButtons.X)) {
            lift.startSpin();
        }

        lift.update();
        berg.update(joystick);

        if (joystick.getValue(XBoxButtons.RBUMPER)) {

        }
    }

    @Override
    public void testPeriodic() {
        LiveWindow.run();
    }
}

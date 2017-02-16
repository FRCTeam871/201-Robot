package org.usfirst.frc.team871.robot;

import org.usfirst.frc.team871.tools.XBoxButtons;

import edu.wpi.first.wpilibj.SPI;

public class Vars {

    // talon motors
    final static int FRONT_LEFT_MOTOR = 1;
    final static int FRONT_RIGHT_MOTOR = 2;
    final static int REAR_LEFT_MOTOR = 3;
    final static int REAR_RIGHT_MOTOR = 0;
    final static int BERG_MOTOR = 4;
    final static int DRUM_MOTOR = 5;// TODO: Make actual number when available
    final static int FRONT_LOADER_MOTOR = 10;// TODO: Make actual number when
                                             // available

    // solenoids
    // final static int TILT_PISTON = -50;
    // final static int BALL_DOOR_PISTON = -50;
    final static int BERG_DEVICE_PISTON_FORWARD = 6;
    final static int BERG_DEVICE_PISTON_BACKWARD = 7;

    // sensors
    final static int LIFTER_UPPER_LIMIT = 3;
    final static int BERG_UPPER_LIMIT = 0;
    final static int BERG_LOWER_LIMIT = 1;
    final static int CHUTE_LOADED_SENSOR = 2;
    final static int BERG_DEVICE_LOADED_SENSOR = 4;

    // gyro
    final static SPI.Port GYRO = SPI.Port.kMXP;
    
    //joystick
    final static XBoxButtons BERG_PIST_GRAB = XBoxButtons.A;
    final static XBoxButtons LIFTER_TOGGLE = XBoxButtons.X;
    final static XBoxButtons BERG_AUTO_RESET = XBoxButtons.Y;
    final static XBoxButtons BERG_MODE_CHANGE = XBoxButtons.START;
    final static XBoxButtons DRIVE_MODE_CHANGE = XBoxButtons.BACK;
   // final static XBoxButtons TRIGGER_AXES = XBoxAxes

}
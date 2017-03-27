package org.usfirst.frc.team871.robot;

import org.usfirst.frc.team871.tools.XBoxAxes;
import org.usfirst.frc.team871.tools.XBoxButtons;
import org.usfirst.frc.team871.tools.XBoxJoypads;

import edu.wpi.first.wpilibj.SPI;

public class Vars {

	// talon motors
	public static final int	FRONT_LEFT_MOTOR	= 1;
	public static final int	FRONT_RIGHT_MOTOR	= 2;
	public static final int	REAR_LEFT_MOTOR		= 3;
	public static final int	REAR_RIGHT_MOTOR	= 0;
	public static final int	BERG_MOTOR			= 4;
	public static final int	DRUM_MOTOR			= 5;  // TODO: Make actual number when available
	public static final int	FRONT_LOADER_MOTOR	= 10; // TODO: Make actual number when available

	// solenoids
	// static final int TILT_PISTON = -50;
	// static final int BALL_DOOR_PISTON = -50;
	public static final int	BERG_DEVICE_PISTON_FORWARD	= 6;
	public static final int	BERG_DEVICE_PISTON_BACKWARD	= 7;
	public static final int	THE_SECRET_PISTON_BACKWARD	= 1; // TODO get actual channel ID
	public static final int	THE_SECRET_PISTON_FORWARD	= 2;

	// sensors
	public static final int	LIFTER_UPPER_LIMIT			= 3;
	public static final int	BERG_UPPER_LIMIT			= 0;
	public static final int	BERG_LOWER_LIMIT			= 1;
	public static final int	CHUTE_LOADED_SENSOR			= 2;
	public static final int	BERG_DEVICE_LOADED_SENSOR	= 4;

	// gyro
	public static final SPI.Port GYRO_PORT = SPI.Port.kMXP;

	// joystick
	public static final XBoxButtons	LIFTER_TOGGLE				= XBoxButtons.X;
	public static final XBoxButtons	BERG_AUTO_RESET				= XBoxButtons.Y;
	public static final XBoxButtons	BERG_ADVANCE				= XBoxButtons.A;
	public static final XBoxButtons	DRIVE_MODE_CHANGE			= XBoxButtons.BACK;
	public static final XBoxButtons	AUTO_DOCK					= XBoxButtons.RBUMPER;
	public static final XBoxButtons	MANUAL_BERG_CLAMP			= XBoxButtons.LBUMPER;
	public static final XBoxButtons	ANDYS_SUPER_SECRET_PISTON	= XBoxButtons.Y;
	public static final XBoxButtons	SWITCH_FROM_MANUAL_TO_AUTO	= XBoxButtons.A;
	public static final XBoxButtons	LIFT_RELEASE				= XBoxButtons.X;

	// Sticks
	public static final XBoxJoypads	DPAD	= XBoxJoypads.LJOYPAD;
	public static final XBoxAxes	TRIGGER	= XBoxAxes.TRIGGER;

	// number values
	public static final double	BERG_UP_SPEED		= -1;
	public static final double	BERG_DOWN_SPEED		= .7;
	public static final double	AUTO_DIST			= 34;
	public static final double	LIFT_IDLE_SPEED		= .3;
	public static final double	LIFT_CLIMB_SPEED	= 1;
	public static final double	AXIS_SCALEY			= 1;

	// Misc
	public static final boolean CLASSMATE_TEST = true;

}
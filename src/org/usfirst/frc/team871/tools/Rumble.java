package org.usfirst.frc.team871.tools;

import edu.wpi.first.wpilibj.GenericHID.RumbleType;
import edu.wpi.first.wpilibj.Joystick;

public class Rumble {

	public static final String PULSE = "10";
	public static final String LONG_PULSE = "1-0";
	public static final String DOUBLE_PULSE = "1010";
	public static final String LONG_DOUBLE_PULSE = "1-0-1-0";
	public static final String TOTALLY_NORMAL_PATTERN = "10-1010--1--------------0";
	
	private Joystick stick;
	
	private Runnable on;
	private Runnable off;
	
	public Rumble(Joystick stick, double strength) {
		this.stick = stick;
		
		on = () -> {
			stick.setRumble(RumbleType.kLeftRumble, strength);
			stick.setRumble(RumbleType.kRightRumble, strength);
		};
		
		off = () -> {
			stick.setRumble(RumbleType.kLeftRumble, 0);
			stick.setRumble(RumbleType.kRightRumble, 0);
		};
		
	}

	public Joystick getStick(){
		return stick;
	}
	
	public void setRumble(boolean rumble){
		(rumble ? on : off).run(); //wow this looks really bad
	}
	
	/**
	 * Rumble the controller with a custom pattern (As documented by the Pattern class).
	 */
	public  void rumble(String pattern){
		rumble(pattern, 100);
	}
	
	/**
	 * Rumble the controller with a custom pattern and delay (As documented by the Pattern class).
	 */
	public  void rumble(String pattern, int delay){
		new Pattern(on, off, pattern, delay).start();
	}
	
	public void pulse(int lengthInMs){
		pulse(lengthInMs, 1);
	}
	
	public void pulse(int lengthInMs, int num){
		String pattern = "";
		
		for(int i = 0; i < num; i++){
			pattern += "10";
		}
		
		rumble(pattern, lengthInMs);
	}
	
	public void shortPulse(){
		pulse(100, 1);
	}
	
	public void longPulse(){
		pulse(500, 1);
	}
	
	public void shortDoublePulse(){
		pulse(100, 2);
	}
	
	public void longDoublePulse(){
		pulse(500, 2);
	}
	
}

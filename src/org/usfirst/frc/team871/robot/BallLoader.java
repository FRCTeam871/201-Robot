package org.usfirst.frc.team871.robot;

import edu.wpi.first.wpilibj.SpeedController;

public class BallLoader {

	private SpeedController motor;
	
	public BallLoader(SpeedController motor) {
		this(motor, false);
	}
	
	public BallLoader(SpeedController motor, boolean enable) {
		this.motor = motor;
		if(enable) enable();
	}
	
	public void enable(){
		setSpeed(0.5d);
	}
	
	public void disable(){
		motor.stopMotor();
	}
	
	public void setSpeed(double speed){
		motor.set(speed);
	}
	
	public double getSpeed(){
		return motor.get();
	}
	
}

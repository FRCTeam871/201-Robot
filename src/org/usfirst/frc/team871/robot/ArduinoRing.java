package org.usfirst.frc.team871.robot;

import edu.wpi.first.wpilibj.SerialPort;
import edu.wpi.first.wpilibj.SerialPort.Port;

public class ArduinoRing {

	private SerialPort port;
	
	public ArduinoRing() {
		this.port = new SerialPort(9600, Port.kMXP);
	}
	
	public void setColor(int r, int g, int b){
		write("c/" + r + "|" + b + "|" + b + "/t");
	}
	
	public void write(String str){
		port.writeString(str);
		port.flush();
	}
	
	public void rainbow(){
		write("r/");
	}
	
	public void off(){
		write("o/");
	}
	
}

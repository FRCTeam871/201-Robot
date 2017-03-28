package org.usfirst.frc.team871.robot;

import java.util.ArrayList;
import java.util.List;

import edu.wpi.first.wpilibj.SerialPort;
import edu.wpi.first.wpilibj.SerialPort.Port;

/* LED OVERVIEW
 * no gear  : setStripsColor(255, 80, 0); // cs/255|80|0/t
 * raising  : theaterChaseStrips(0, 50, 0, 0, 255, 0, 100, 50); // tcs/0|50|0|0|255|0|100|50/t
 * lowering : theaterChaseStrips(0, 50, 0, 0, 255, 0, -100, 50); // tcs/0|50|0|0|255|0|-100|50/t
 * mid stop : setStripsColor(0, 255, 0); // cs/0|255|0/t
 * at top   : pulseStrips(0, 255, 0, (distance -> 400-1000)); // ps/0|255|0|1000/t
 * 
 * AUTON
 * target   : setStripsColor(0, 255, 0); // ps/0|255|0|1000/t
 * no target: setStripsColor(255, 0, 0); // ps/255|0|0|1000/t
 */

/**
 * @author Team871 - Dave
 * <br>
 * A class used for controlling the LED ring and strips connected to the arduino.
 */
public class Arduino {

	private SerialPort port;
	
	/**
	 * Instantiate the Arduino. Connects via a <code>SerialPort</code> at 9600 baud on <code>Port.kMXP</code>.
	 */
	public Arduino() {
		this.port = new SerialPort(9600, Port.kMXP);
	}
	
	/**
	 * Sets the color of all LEDs on the arduino.
	 * @param r - Red component of the color. [0-255]
	 * @param g - Green component of the color. [0-255]
	 * @param b - Blue component of the color. [0-255]
	 */
	public void setColor(int r, int g, int b){
		setRingColor(r, g, b);
		setStripsColor(r, g, b);
	}
	
	/**
	 * Sets the LED ring to a color.
	 * @param r - Red component of the color. [0-255]
	 * @param g - Green component of the color. [0-255]
	 * @param b - Blue component of the color. [0-255]
	 */
	public void setRingColor(int r, int g, int b){
		write("c/" + r + "|" + g + "|" + b + "/t");
	}
	
	/**
	 * Sets the LED strips to a color.
	 * @param r - Red component of the color. [0-255]
	 * @param g - Green component of the color. [0-255]
	 * @param b - Blue component of the color. [0-255]
	 */
	public void setStripsColor(int r, int g, int b){
		write("cs/" + r + "|" + g + "|" + b + "/t");
	}
	
	/**
	 * Sets the LED strips to a gradient from (r1,g1,b1) to (r2,g2,b2)
	 * @param r1 - Red component of the first color. [0-255]
	 * @param g1 - Green component of the first color. [0-255]
	 * @param b1 - Blue component of the first color. [0-255]
	 * @param r2 - Red component of the second color. [0-255]
	 * @param g2 - Green component of the second color. [0-255]
	 * @param b2 - Blue component of the second color. [0-255]
	 */
	public void setStripsGradientColor(int r1, int g1, int b1, int r2, int g2, int b2){
		write("gs/" + r1 + "|" + g1 + "|" + b1 + "|" + r2 + "|" + g2 + "|" + b2 + "/t");
	}
	
	private List<String> queue = new ArrayList<String>();
	
	/**
	 * Writes <b>str</b> directly to the serial connection.
	 * @param str - The <code>String</code> to write.
	 */
	public void write(String str){
		//System.out.println("add to queue |" + str + "|");
		queue.add(str);
	}
	
	private long lastFlush = 0;
	public void update(){
		long now = System.currentTimeMillis();
		if(now - lastFlush >= 100){
			String str = null;
			
			if(!queue.isEmpty()){
			
				str = queue.get(0);
				queue.remove(0);
				//System.out.println("writing |" + str + "|");
				
				port.writeString(str);
				port.flush();
				
				lastFlush = now;
			}
		}
	}
	
	/**
	 * Puts all LEDs on the arduino into rainbow mode.
	 */
	public void rainbow(){
		write("r/");
	}
	
	/**
	 * Turns off the LED ring on the arduino.
	 */
	public void ringOff(){
		setRingColor(0, 0, 0);
	}
	
	/**
	 * Turns off the LED strips on the arduino.
	 */
	public void stripsOff(){
		setStripsColor(0, 0, 0);
	}
	
	/**
	 * Turns off all LEDs on the arduino.
	 */
	public void allOff(){
		write("o/");
	}
	
	/**
	 * Sets the brightness of all LEDs on the arduino.
	 * @param fl - The brightness. [0f-1f] where 1f is full brightness and 0f is off.
	 */
	public void setBrightness(float fl){
		if(fl > 1f) fl = 1f;
		if(fl < 0f) fl = 0f;
		write("br/" + Math.round(fl * 100) + "/t");
	}
	
	/**
	 * Puts the LED strips on the arduino into theater chase mode.
	 * @param r1 - Red component of the first("on") color. [0-255]
	 * @param g1 - Green component of the first("on") color. [0-255]
	 * @param b1 - Blue component of the first("on") color. [0-255]
	 * @param r2 - Red component of the second("off") color. [0-255]
	 * @param g2 - Green component of the second("off") color. [0-255]
	 * @param b2 - Blue component of the second("off") color. [0-255]
	 * @param pxOfs - How offset each pixel is. Increasing <b>pxOfs</b> makes the "on" parts longer and faster. If negative, the "on" parts will go the opposite direction.<br>
	 * @param speed - How long each cycle is. Increasing <b>speed</b> makes the "on" parts farther apart.
	 */
	public void theaterChaseStrips(int r1, int g1, int b1, int r2, int g2, int b2, int pxOfs, int speed){
		write("tcs/" + r1 + "|" + g1 + "|" + b1 + "|" + r2 + "|" + g2 + "|" + b2 + "|" + pxOfs + "|" + speed + "/t");
	}
	
	/**
	 * Puts the LED strips on the arduino into pulse mode.
	 * @param r - Red component of the color. [0-255]
	 * @param g - Green component of the color. [0-255]
	 * @param b - Blue component of the color. [0-255]
	 * @param time - How long each pulse is.
	 */
	public void pulseStrips(int r, int g, int b, int time){
		write("ps/" + r + "|" + g + "|" + b + "|" + time + "/t");
	}
	
	/**
	 * Puts the LED strips on the arduino into blink mode.
	 * @param r - Red component of the color. [0-255]
	 * @param g - Green component of the color. [0-255]
	 * @param b - Blue component of the color. [0-255]
	 * @param time - How long each blink is.
	 */
	public void blinkStrips(int r, int g, int b, int time){
		write("bs/" + r + "|" + g + "|" + b + "|" + time + "/t");
	}
	

	public void rainbowStrips() {
		write("rs/");
	}
	
}

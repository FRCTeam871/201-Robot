package org.usfirst.frc.team871.tools;

public class Pattern {

	protected Runnable on;
	protected Runnable off;
	
	protected String pattern;
	protected int index = 0;
	
	protected int delay;
	
	protected Thread thread;
	
	/**
	 * Plays a pattern using on/off Runnables. <br>
	 * When formatting the pattern, 1 is on, 0 is off, and any other character is wait.<br>
	 * There is a 500ms delay for each character in the pattern.<br>
	 * The pattern will turn off automatically if 0 is not the ending character. (Eg. '101' will function the same as '1010'.)
	 * 
	 * @param pattern The pattern to play.
	 * @param on Executes for every '1'.
	 * @param off Executes for every '0'.
	 */
	public Pattern(Runnable on, Runnable off, String pattern) {
		this(on, off, pattern, 500);
	}
	
	/**
	 * Plays a pattern using on/off Runnables. <br>
	 * When formatting the pattern, 1 is on, 0 is off, and any other character is wait.<br>
	 * There is a delay (determined by the 'delay' parameter) for each character in the pattern.<br>
	 * The pattern will turn off automatically if 0 is not the ending character. (Eg. '101' will function the same as '1010'.)
	 * 
	 * @param pattern The pattern to play.
	 * @param on Executes for every '1'.
	 * @param off Executes for every '0'.
	 * @param delay The delay between characters.
	 */
	public Pattern(Runnable on, Runnable off, String pattern, int delay) {
		this.on = on;
		this.off = off;
		this.pattern = pattern;
		if(!pattern.endsWith("0")) pattern = pattern + "0";
		this.delay = delay;
	}

	public void start(){
		if(isDone()){
			index = 0;
    		thread = new Thread(() -> {
    			while(index < pattern.length()){
    				next();
    				try {
    					Thread.sleep(delay);
    				} catch (InterruptedException e) {}
    			}
    			
    			thread = null; //is this bad?
    		});
    		
    		thread.start();
		}else{
			System.err.println("You can't start a Pattern while it is running.");
		}
	}
	
	public boolean isDone(){
		return thread == null;
	}
	
	protected void next() {
		char nextChar = pattern.charAt(index);
		
		switch(nextChar){
			case '0':
				off.run();
				break;
			case '1':
				on.run();
				break;
			default:
				//do nothing
				break;
		}
		
		index++;
	}
}

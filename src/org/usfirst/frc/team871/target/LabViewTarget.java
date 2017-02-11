package org.usfirst.frc.team871.target;

public class LabViewTarget implements ITarget{

	double distL, distR, distC, centerX;
	
	/**
	 * An implementation of {@link ITarget} for storing target info.<br>
	 * Any instance will not update its information after its creation.
	 */
	public LabViewTarget(double distL, double distR, double distC, double centerX) {
		this.distL = distL;
		this.distR = distR;
		this.distC = distC;
		this.centerX = centerX;
	}
	
	/**
	 * Gets the distance to the left target in inches.
	 */
	@Override
	public double getDistanceLeft() {
		return distL;
	}

	/**
	 * Gets the distance to the right target in inches.
	 */
	@Override
	public double getDistanceRight() {
		return distR;
	}

	/**
	 * Gets the distance to the overall(combined) target in inches.
	 */
	@Override
	public double getDistance() {
		return distC;
	}

	/**
	 * Gets the x coordinate of the center of the target, where x=0 is the left of the camera.
	 */
	@Override
	public double getCenterX() {
		return centerX;
	}

}

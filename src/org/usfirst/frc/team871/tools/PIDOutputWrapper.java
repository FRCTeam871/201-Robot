package org.usfirst.frc.team871.tools;

import edu.wpi.first.wpilibj.PIDOutput;

public class PIDOutputWrapper implements PIDOutput {
	double cOutput = 0.0;
	
	@Override
	public void pidWrite(double output) {
		output = cOutput;
	}
	
	public double getOutput() {
		return cOutput;
	}
}

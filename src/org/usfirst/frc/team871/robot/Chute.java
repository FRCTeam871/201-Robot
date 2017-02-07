package org.usfirst.frc.team871.robot;

import edu.wpi.first.wpilibj.DigitalInput;

public class Chute {
    DigitalInput loadSensor;

    public Chute(DigitalInput loadSensor) {
        this.loadSensor = loadSensor;
    }

    public boolean isLoaded() {
        return !loadSensor.get();
    }
}

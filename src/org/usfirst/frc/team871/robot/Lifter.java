package org.usfirst.frc.team871.robot;

import org.usfirst.frc.team871.tools.sensors.ILimitSwitch;

import edu.wpi.first.wpilibj.CANSpeedController;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.livewindow.LiveWindowSendable;

public class Lifter {
    boolean isSpinning, isClimbing;

    int curArrayPos, numSamples;

    double prevCurrent[];

    CANSpeedController liftMotor;

    ILimitSwitch topSensor;

    public Lifter(CANSpeedController liftMotor, ILimitSwitch topSensor) {
        isSpinning = false;
        isClimbing = false;

        curArrayPos = 0;

        prevCurrent = new double[100];

        this.liftMotor = liftMotor;
        this.topSensor = topSensor;

        if (liftMotor instanceof LiveWindowSendable) {
            LiveWindow.addActuator("Lifter", "Lift Motor", liftMotor);
        }
    }

    public void startSpin() {
        isSpinning = true;
    }

    public void stopSpin() {
        isSpinning = false;
        isClimbing = false;
    }

    public void update() {
        if (isSpinning) {
            if (isClimbing) {
                liftMotor.set(1.0);
            } else {
                liftMotor.set(0.3);
            }

            double c = liftMotor.getOutputCurrent();
            prevCurrent[curArrayPos] = c;

            curArrayPos = ++curArrayPos % 99;
            numSamples++;

            if (numSamples > 100) {
                double avg = 0;
                for (double d : prevCurrent)
                    avg += d;

                avg /= 100;

                if (c > (1.5 * avg))
                    isClimbing = true;
            }
        } else {
            liftMotor.set(0.0);
        }

        if (topSensor.isAtLimit()) {
            stopSpin();
        }
    }

}

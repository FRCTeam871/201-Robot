package org.usfirst.frc.team871.tools;

public class PIDControl {

    private double setpoint;
    private double prevError;
    private double integral;
    private double kp;
    private double ki;
    private double kd;
    private double minOut;
    private double threshhold;

    public PIDControl(double Kp, double Ki, double Kd, double setpoint, double minOut, double threshhold) {
        this.kp = Kp;
        this.ki = Ki;
        this.kd = Kd;

        this.setpoint = setpoint;
        
        this.minOut = minOut;
        
        this.threshhold = threshhold;

        prevError = 0;
        integral = 0;
    }

    public void setSetpoing(double setpoint) {
        this.setpoint = setpoint;
    }

    public void setKp(double kp) {
        this.kp = kp;
    }

    public void setKi(double ki) {
        this.ki = ki;
    }

    public void setKd(double kd) {
        this.kd = kd;
    }
    
    public void setMinimumOutput(double minOut) {
        this.minOut = minOut;
    }
    
    public void setThreshhold(double threshhold) {
        this.threshhold = threshhold;
    }

    private double CentralPID(double reading) {
        double error = reading - setpoint;
        integral += error;

        double output = (kp * error) + (ki * integral) + (kd * (error - prevError));

        prevError = error;
        
        if (Math.abs(error) >= threshhold) {
            if (Math.abs(output) <= minOut) {
                output = output < 0 ? -minOut : minOut;
            }
        } else {
            output = 0.0;
        }

        return output;
    }

    public double getPID(double error) {
        return this.CentralPID(error);
    }

    public double getMotorPID(double error) {
        double output = this.CentralPID(error);

        if (output > 0.99) {
            output = 1;
        }
        if (output < -0.99) {
            output = -1;
        }

        return output;
    }
}

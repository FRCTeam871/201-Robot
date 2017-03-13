package org.usfirst.frc.team871.tools;

/**
 * Basic PID controller class.
 * 
 * @author Team871
 */
public class PIDControl {

    private double setpoint;
    private double prevError;
    private double integral;
    private double kp;
    private double ki;
    private double kd;
    private double minOut;
    private double threshold;

/**
 * @param Kp - Double
 * @param Ki - Double
 * @param Kd - Double
 * @param setpoint - Double
 * @param minOut - Double representing the minimum output that the controller
 * should output if not at the limit.
 * @param threshold - Double representing the maximum acceptable deviation
 * from the setpoint.
 */
    public PIDControl(double Kp, double Ki, double Kd, double setpoint, double minOut, double threshold) {
        this.kp = Kp;
        this.ki = Ki;
        this.kd = Kd;

        this.setpoint = setpoint;
        
        this.minOut = minOut;
        
        this.threshold = threshold;

        prevError = 0;
        integral = 0;
    }

/**
 * @param setpoint - Double
 */
    public void setSetpoing(double setpoint) {
        this.setpoint = setpoint;
    }

/**
 * @param kp - Double
 */
    public void setKp(double kp) {
        this.kp = kp;
    }
    
/**
 * @param ki - Double
 */
    public void setKi(double ki) {
        this.ki = ki;
    }

/**
 * @param kd - Double
 */
    public void setKd(double kd) {
        this.kd = kd;
    }
 
/**
 * @param minOut - Double representing the minimum output
 */
    public void setMinimumOutput(double minOut) {
        this.minOut = minOut;
    }
    
/**
 * @param threshhold - Double
 */
    public void setThreshhold(double threshhold) {
        this.threshold = threshhold;
    }

    private double CentralPID(double reading) {
        double error = reading - setpoint;
        integral += error;

        double output = (kp * error) + (ki * integral) + (kd * (error - prevError));

        prevError = error;
        
        if (Math.abs(error) >= threshold) {
            if (Math.abs(output) <= minOut) {
                output = output < 0 ? -minOut : minOut;
            }
        } else {
            output = 0.0;
        }

        return output;
    }

/**
 * Takes in the error value and returns the PID correction
 * 
 * @param error - Double
 * @return Double representing the correction
 */
    public double getPID(double error) {
        return this.CentralPID(error);
    }

/**
 * Takes in the error value and returns the PID correction.  The correction
 * returned will be truncated to between -1.0 and 1.0, exclusive.
 * 
 * @param error - Double
 * @return Double representing the correction value
 */
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

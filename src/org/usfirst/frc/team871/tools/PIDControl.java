package org.usfirst.frc.team871.tools;

public class PIDControl {

    private double setpoint;
    private double prevError;
    private double integral;
    private double kp;
    private double ki;
    private double kd;

    public PIDControl(double Kp, double Ki, double Kd, double setpoint) {
        this.kp = Kp;
        this.ki = Ki;
        this.kd = Kd;

        this.setpoint = setpoint;

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

    private double CentralPID(double reading) {
        double error = reading - setpoint;
        integral += error;

        double output = (kp * error) + (ki * integral) + (kd * (error - prevError));

        prevError = error;

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

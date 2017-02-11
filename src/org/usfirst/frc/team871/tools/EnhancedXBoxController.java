package org.usfirst.frc.team871.tools;

import edu.wpi.first.wpilibj.Joystick;

/**
 * An extension of Joystick taylored to the Xbox 360 controller which provides
 * additional functionality including individual axis deadbanding, mapped
 * deadbands, and button type emulation for all buttons.
 * 
 * @author Jack Langhorn
 * 
 * @see XBoxAxes
 * @see XBoxButtons
 * @see XBoxJoypads
 * @see ButtonTypes
 *
 */
public class EnhancedXBoxController extends Joystick {
    boolean[][] prevState; // Each element is an array containing the emulated
                           // and actual previous states of the button.

    int[] POVs; // Each element is a POV (joypad) on the controller.
    private static final int PREV_RAW = 1;
    private static final int PREV_EMULATED = 0;

    double[] axisDeadband; // Each element is the deadband value of the
                           // corresponding joystick axis.

    ButtonTypes[] buttonMode;// Each element is the emulated type of the
                             // corresponding button.

    /**
     * Creates a new Enhanced Controller on the specified port.
     * 
     * @param port
     *            Int containing the port number of the controller
     */
    public EnhancedXBoxController(int port) {
        // Constructor; initializes everything to zero or false and creates a
        // new Joystick on the specified port
        super(port);

        prevState = new boolean[XBoxButtons.values().length][2];
        buttonMode = new ButtonTypes[XBoxButtons.values().length];
        for (int i = 0; i < XBoxButtons.values().length; i++) {
            buttonMode[i] = ButtonTypes.MOMENTARY;
        }
        axisDeadband = new double[XBoxAxes.values().length];
        for (int i = 0; i < axisDeadband.length; i++) {
            axisDeadband[i] = 0.0;
        }
    }

    /*
     * Setters
     */

    /**
     * Sets the emulated type of the corresponding button
     * 
     * @param button
     *            The affected XBoxButtons button
     * @param mode
     *            The new ButtonTypes type
     */
    public void setButtonMode(XBoxButtons button, ButtonTypes mode) {
        buttonMode[button.ordinal()] = mode;
    }

    /**
     * Sets the deadband of the corresponding button
     * 
     * @param axis
     *            The affected axis
     * @param dead
     *            Double containing the new deadband value. Must be greater than
     *            0 and less than 1.
     */
    public void setAxisDeadband(XBoxAxes axis, double dead) {
        axisDeadband[axis.getValue()] = ((dead >= 0) && (dead < 1)) ? dead : axisDeadband[axis.getValue()];
    }

    /*
     * Getters (Overloaded)
     */

    /**
     * Returns the value of the specified button. All buttons are active high,
     * and there is no way to change this. This method respects the emulated
     * type of the buttons.
     * 
     * @param button
     *            The button whose value should be read
     * @return Boolean containing the state of the button
     * 
     * @see ButtonTypes
     */
    public boolean getValue(XBoxButtons button) {
        // Gets the value of the button, taking into account its emulated type.
        int buttonNum = button.ordinal();
        boolean curVal = super.getRawButton(button.getValue());
        boolean retVal = false;

        ButtonTypes mode = buttonMode[buttonNum];

        switch (mode) {
            case MOMENTARY: // Momentary button, returns raw input.
                retVal = curVal;
                break;

            case TOGGLE: // Toggle button emulator.
                if (curVal && (curVal != prevState[buttonNum][PREV_RAW])) {
                    prevState[buttonNum][PREV_EMULATED] = !prevState[buttonNum][PREV_EMULATED];
                    retVal = prevState[buttonNum][PREV_EMULATED];
                } else {
                    retVal = prevState[buttonNum][PREV_EMULATED];
                }
                break;

            case RISING: // Rising edge detector.
                if (curVal && (curVal != prevState[buttonNum][PREV_RAW])) {
                    prevState[buttonNum][PREV_RAW] = curVal;
                    retVal = true;
                } else {
                    prevState[buttonNum][PREV_RAW] = curVal;
                    retVal = false;
                }
                break;

            case FALLING: // Falling edge detector.
                if (!curVal && (curVal != prevState[buttonNum][PREV_RAW])) {
                    prevState[buttonNum][PREV_RAW] = curVal;
                    retVal = true;
                } else {
                    prevState[buttonNum][PREV_RAW] = curVal;
                    retVal = false;
                }
                break;

            default:
                retVal = false;
                break;
        }

        prevState[buttonNum][PREV_RAW] = curVal;

        return retVal;
    }

    /**
     * Reads the value of the specified axis after deadbanding. The deadbanded
     * values are linearly mapped to new values such that at the deadband, the
     * value is 0, but at full power the value is 1. This prevents jerky
     * readings when the raw value hovers around the deadband.
     * <p>
     * If Trigger is read, the value returned will be the result from
     * subtracting the value of the left trigger from the right trigger. This
     * emulates both triggers affecting the same axis.
     * 
     * @param axis
     *            The axis to read
     * @return Double containing the value of the axis.
     */
    public double getValue(XBoxAxes axis) {
        /*
         * Gets the value of the joystick axis after deadbanding. Uses the
         * following equation to create a linear graph mapping deadbanded
         * joystick data to output: y=((x-1)/(1-d))+1. This means that if the
         * axis is at or below the deadband, it will return 0.0, but after that
         * it begins increasing from 0.0, but with a steeper sloping line so
         * that at the deadband it is 0.0, but at full forward it is 1.0. This
         * prevents the robot from jerking and gives the driver more control.
         * The following may be used for parabolic mapping:
         * y=(((x-d)^2)/((1-d)^2)).
         */
        double raw = 0.0;
        if (axis == XBoxAxes.TRIGGER) { // Emulates previous years in which both
                                        // triggers were one one axis
            raw = this.getRawValue(XBoxAxes.RTRIGGER) - this.getRawValue(XBoxAxes.LTRIGGER);
        } else {
            raw = super.getRawAxis(axis.getValue());
        }

        double dead = axisDeadband[axis.ordinal()];
        double adjustedValue = 0.0;

        if (dead == 0.0) {
            return raw;
        }

        if ((raw > -dead) && (raw < dead)) {
            return 0.0;
        }

        if (raw <= 0) {
            adjustedValue = (1 / (1 - dead)) * raw + (dead / (1 - dead));// -(((Math.abs(raw)
                                                                         // - 1)
                                                                         // / (1
                                                                         // -
                                                                         // dead))
                                                                         // +
                                                                         // 1);
        } else {
            adjustedValue = (1 / (1 - dead)) * raw - (dead / (1 - dead));// ((raw
                                                                         // - 1)
                                                                         // / (1
                                                                         // -
                                                                         // dead))
                                                                         // + 1;
        }

        return adjustedValue;
    }

    /**
     * Reads the value of the specified joypad.
     * 
     * @param pad
     *            The joypad to read
     * @return Int containing the angle of the angle of the joypad. It will be a
     *         multiple of 45 degrees representing the position that was
     *         pressed. -1 will be returned if nothing was pressed.
     */
    public int getValue(XBoxJoypads pad) {
        /*
         * Gets the joypad value. Returns an int representing the angle in
         * degrees about the top of the pad of the position of the direction
         * pressed. Will only ever be multiples of 45 or -1 if nothing is being
         * pressed.
         */
        return super.getPOV(pad.getValue());
    }

    /**
     * Reads the raw value of the specified button. This method does not respect
     * emulated types.
     * 
     * @param button
     *            The button to read
     * @return Boolean containing the value of the button
     */
    public boolean getRawValue(XBoxButtons button) {
        // Same as calling joystick.getRawButton(number), but is overloaded and
        // uses the Button enum
        return super.getRawButton(button.getValue());
    }

    /**
     * Reads the raw value of the specified axis. This method does not deadband
     * values.
     * 
     * @param axis
     *            The axis to read
     * @return Double between 0 and 1 containing the value of the axis
     */
    public double getRawValue(XBoxAxes axis) {
        // Same as calling joystick.getRawAxis(number), but is overloaded and
        // used the XBoxAxes enum
        return super.getRawAxis(axis.getValue());
    }

    /**
     * Reads and debounces the specified button. When read with this method, the
     * button will read high only on the rising edge, regardless of the emulated
     * type of the button.
     * 
     * @param button
     *            The button to read
     * @return Boolean containing the debounced value of the button
     */
    public boolean getDebouncedButton(XBoxButtons button) {
        // Returns true when the button is rising, regardless of emulated type.
        boolean value = super.getRawButton(button.getValue());
        int buttonNum = button.ordinal();

        if (value && (value != prevState[buttonNum][PREV_RAW])) {
            prevState[buttonNum][PREV_RAW] = value;
            return true;
        } else {
            prevState[buttonNum][PREV_RAW] = value;
            return false;
        }
    }

}

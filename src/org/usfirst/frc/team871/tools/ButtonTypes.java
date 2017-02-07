package org.usfirst.frc.team871.tools;

/**
 * All the possible button types for any digital input.
 * 
 * @author Jack Langhorn
 */
public enum ButtonTypes {
    /**
     * Momentary buttons are read as true whenever they are pressed. This is
     * exactly the same as reading the raw value of the button.
     */
    MOMENTARY,
    /**
     * Toggle buttons switch states when they are pressed. If one reads low and
     * it is pressed, it will change state to high and continue to read high
     * until it is pressed again.
     */
    TOGGLE,
    /**
     * Buttons set to Rising will only read high once, on their rising edges.
     * This means that the button will only read high when its raw value changes
     * state from low to high.
     */
    RISING,
    /**
     * Buttons set to Falling will only read high once, on their falling edges.
     * This means that the button will only read high when its raw value changes
     * state from high to low.
     */
    FALLING;
}
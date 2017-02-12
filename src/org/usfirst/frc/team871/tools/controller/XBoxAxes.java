package org.usfirst.frc.team871.tools.controller;

/**
 * Maps all the axes on the XBox 360 controller to their axis numbers in
 * software.
 * 
 * @author Jack Langhorn
 *
 */
public enum XBoxAxes {
    /**
     * The horizontal axis of the left stick.
     */
    LEFTX(0),
    /**
     * The vertical axis of the left stick.
     */
    LEFTY(1),
    /**
     * The value of the left trigger only. This will only ever be positive and
     * does not take into account the right trigger.
     * 
     * @see XBoxAxes#TRIGGER
     */
    LTRIGGER(2),
    /**
     * The value of the right trigger only. This will only ever be positive and
     * does not take into account the left trigger.
     * 
     * @see XBoxAxes#TRIGGER
     */
    RTRIGGER(3),
    /**
     * The horizontal axis of the right stick.
     */
    RIGHTX(4),
    /**
     * The vertical axis of the right stick.
     */
    RIGHTY(5),
    /**
     * Virtual axis which takes into account both the left and right triggers.
     * The value of this axis will be equal to the left trigger subtracted from
     * the right trigger.
     */
    TRIGGER(6); // Emulates previous years in which both triggers were on one
                // axis

    private int value;

    XBoxAxes(int num) {
        value = num;
    }

    /**
     * Returns the mapping to the axis number in software.
     * 
     * @return Int containing the axis number
     */
    int getValue() {
        return value;
    }
}

package org.usfirst.frc.team871.tools;

/**
 * Maps the physical joypads to their POV numbers in software.
 * <p>
 * Xbox 360 controllers only have one joypad.
 * 
 * @author Jack Langhorn
 *
 */
public enum XBoxJoypads {
    /**
     * The only joypad on the controller, located under the left stick.
     */
    LJOYPAD(0);

    private int value;

    XBoxJoypads(int num) {
        value = num;
    }

    /**
     * Returns the mapping the the POV number in software.
     * 
     * @return Int containing the POV number
     */
    int getValue() {
        return value;
    }
}

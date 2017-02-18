package org.usfirst.frc.team871.tools;

import edu.wpi.first.wpilibj.DigitalInput;


/**
 * Implementation of the ILimitSwitch interface for digital inputs. 
 * 
 * @author Team871
 */
public class DigitalLimitSwitch implements ILimitSwitch {
    boolean activeLow;

    DigitalInput input;

/**
 * Constructor which specifies if the input is active low or not.
 * 
 * @param input - DigitalInput
 * @param activeLow - Boolean indicating if the input is to be interpreted as
 * active low
 */
    public DigitalLimitSwitch(DigitalInput input, boolean activeLow) {
        this.input = input;
        this.activeLow = activeLow;
    }

/**
 * Basic constructor.  Assumes the input is to be interpreted as active high.
 * 
 * @param input - DigitalInput
 */
    public DigitalLimitSwitch(DigitalInput input) {
        this.input = input;
        this.activeLow = false;
    }

/**
 * Sets if the input is to be interpreted as active low.
 * 
 * @param activeLow - Boolean
 */
    public void setActiveLow(boolean activeLow) {
        this.activeLow = activeLow;
    }

/**
 * @return The status of the sensor
*/
    @Override
    public boolean isAtLimit() {
        return (activeLow) ? !input.get() : input.get();
    }
}

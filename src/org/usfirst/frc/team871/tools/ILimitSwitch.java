package org.usfirst.frc.team871.tools;

/**
 * An interface for interacting with limit switches.
 * 
 * @author Team871
 */
public interface ILimitSwitch {

    /**
     * @return Boolean - the status of the limit switch
     */
    public boolean isAtLimit();
}

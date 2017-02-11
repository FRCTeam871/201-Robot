package org.usfirst.frc.team871.target;

/**
 * A generic interface to specify a way of acquiring targets from sources.
 */
public interface ITargetAcquisition {
    /**
     * Gets an ITarget object containing the target.
     */
   public ITarget getTarget();

    /**
     * Checks to see if a target is available.
     */
   public boolean isTargetAvailable();
    
    
}

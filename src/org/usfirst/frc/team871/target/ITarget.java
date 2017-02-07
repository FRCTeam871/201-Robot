package org.usfirst.frc.team871.target;

/**
 * Defines a generic interface for a vision target.
 */
public interface ITarget {
    /**
     * Reads the distance to the left tape of the target.
     */
    double getDistanceLeft();

    /**
     * Reads the distance to the right tape of the target.
     */
    double getDistanceRight();

    /**
     * Reads the distance to the center of the target.
     */
    double getDistance();

    /**
     * Reads the X coordinate of the center of the target.
     */
    double getCenterX();
}

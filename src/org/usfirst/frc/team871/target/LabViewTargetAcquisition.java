package org.usfirst.frc.team871.target;

import edu.wpi.first.wpilibj.networktables.NetworkTable;

public class LabViewTargetAcquisition implements ITargetAcquisition{

	private NetworkTable table;
	
	/**
	 * An implementation of {@link ITargetAcquisition} for retrieving target data from the LabView dashboard.
	 */
	public LabViewTargetAcquisition() {
		this.table = NetworkTable.getTable("SmartDashboard");
	}
	
	/**
	 * Gets the best target, if available.
	 * @returns A new <b>{@link LabViewTarget}</b> if there is target data available.<br>
	 * <b>null</b> if there is no target available.
	 */
	@Override
	public ITarget getTarget() {
		return isTargetAvailable() ? new LabViewTarget(table.getNumber("distA", 0f), table.getNumber("distB", 0f), table.getNumber("distComb", 0f), table.getNumber("centerX", 0f)) : null;
	}

	/**
	 * @return <b>true</b> if a target data is available.<br>
	 * <b>false</b> if no target data is available.
	 */
	@Override
	public boolean isTargetAvailable() {
		return table.getBoolean("hasTarget", false);
	}
	
	/**
	 * @return The {@link NetworkTable} being read for target information.
	 */
	public NetworkTable getTable(){
		return table;
	}

}

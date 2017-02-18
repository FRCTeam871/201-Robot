package org.usfirst.frc.team871.tools;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import edu.wpi.first.wpilibj.Utility;
import edu.wpi.first.wpilibj.networktables.NetworkTable;

/**
 * Profiler which records and prints the time delta between two arbitrary
 * points in the program.  Profiles can be created with arbitrary names.
 * Mark points can be inserted using the <code>mark</code> method which will be
 * printed out to the <code>Profiler</code> network table every 500 milliseconds.
 * 
 * @author Team871
 */
public class Profiler {
    private static final long TABLE_UPDATE_PERIOD = 500;
    private static final Map<String,Profiler> profilers = new HashMap<>();
    private static final Thread profileUpdater;
    private static NetworkTable table;
    
    long lastTime = 0;
    long curTime = 0;
    
    static {
        table = NetworkTable.getTable("Profiler");
        profileUpdater = new Thread(() -> {
            while(true) {
                for(Entry<String,Profiler> ent : profilers.entrySet()) {
                    table.putNumber(ent.getKey(), ent.getValue().getDelta());
                }
                try {
                    Thread.sleep(TABLE_UPDATE_PERIOD);
                } catch (InterruptedException e) { }
            }
        });
        profileUpdater.start();
    }
    
    protected Profiler() {
        
    }
    
/**
 * Gets the profiler with the specified name.  If the specified profiler does
 * not exist, it will be created automatically.
 * 
 * @param name - String
 * @return Profiler with the specified name
 */
    public static Profiler getProfiler(String name) {
        if(!profilers.containsKey(name)) {
            profilers.put(name, new Profiler());
        }
        
        return profilers.get(name);
    }

/**
 * Records the time when called.
 */
    public void mark() {
        lastTime = curTime;
        curTime = Utility.getFPGATime();
    }
    
/**
 * Gets the delta in microseconds between when the previous two marks.  This
 * method is called every 500 milliseconds for every profiler and the results
 * are posted into the <code>Profiler</code> network table.
 * 
 * @return Long representing the time delta
 */
    public long getDelta() {
        return (curTime-lastTime);
    }

/**
 * Resets the delta to zero.
 */
    public void reset() {
        curTime = lastTime = Utility.getFPGATime();
    }
}

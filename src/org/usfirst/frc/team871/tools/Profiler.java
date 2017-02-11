package org.usfirst.frc.team871.tools;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import edu.wpi.first.wpilibj.Utility;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

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
    
    public static Profiler getProfiler(String name) {
        if(!profilers.containsKey(name)) {
            profilers.put(name, new Profiler());
        }
        
        return profilers.get(name);
    }
    
    public void mark() {
        lastTime = curTime;
        curTime = Utility.getFPGATime();
    }
    
    public double getDelta() {
        return (curTime-lastTime);
    }
    
    public void reset() {
        curTime = lastTime = Utility.getFPGATime();
    }
}

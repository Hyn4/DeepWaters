package org.example.level;

public class XPTable {

    private static final long[] LEVEL_THRESHOLDS = {
        0,
        100,
        200,
        300,
        400,
        500,
        600,
        700,
        800,
        900,
        1000
    };

    public static final int MAX_LEVEL = LEVEL_THRESHOLDS.length;
    public static final int START_LEVEL = 1;

    private XPTable() {}

    public static int getLevelFromXP(long totalXP){
        if (totalXP < 0) return START_LEVEL;

        for(int level = MAX_LEVEL; level >= START_LEVEL; level --){
            if (totalXP >= LEVEL_THRESHOLDS[level - 1]){
                return level;
            }
        }

        return START_LEVEL;
    }

    public static long getXPFromLevel(int level){
        if (level < START_LEVEL) return 0L;
        if (level > MAX_LEVEL) return LEVEL_THRESHOLDS[MAX_LEVEL-1];
        return LEVEL_THRESHOLDS[level-1];
    }

    public static long getXPInCurrentLevel(long totalXP){
        var level = getLevelFromXP(totalXP);
        return totalXP - getXPFromLevel(level);
    }

    public static long getXPToNextLevel(long totalXP){
        var level = getLevelFromXP(totalXP);
        if(level >=MAX_LEVEL) return 0L;
        return LEVEL_THRESHOLDS[level] - totalXP;
    }

    public static float getProgressToNextLevel(long totalXP){
        var level = getLevelFromXP(totalXP);
        if(level >= MAX_LEVEL) return 1.0f;

        var currentThreshold = LEVEL_THRESHOLDS[level - 1];
        var nextThreshold = LEVEL_THRESHOLDS[level];
        var xpInLevel = totalXP - currentThreshold;
        var xpNeeded = nextThreshold - currentThreshold;

        return xpNeeded == 0 ? 1.0f : (float) xpInLevel/xpNeeded;
    }
}

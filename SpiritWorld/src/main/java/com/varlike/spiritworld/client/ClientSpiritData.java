package com.varlike.spiritworld.client;

public class ClientSpiritData {

    private static long spirit = 0L;
    private static long maxSpirit = 100L;
    private static int visionLevel = 0;

    public static long getSpirit() { return spirit; }
    public static long getMaxSpirit() { return maxSpirit; }
    public static int getVisionLevel() { return visionLevel; }

    public static void update(long newSpirit, long newMaxSpirit, int newVisionLevel) {
        spirit = newSpirit;
        maxSpirit = newMaxSpirit;
        visionLevel = newVisionLevel;
    }
}
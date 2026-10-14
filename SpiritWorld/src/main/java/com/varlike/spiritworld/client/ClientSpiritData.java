package com.varlike.spiritworld.client;

import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ClientSpiritData {

    private static long spirit = 0L;
    private static long maxSpirit = 100L;
    private static long availableVisionPoints = 0L;
    private static long totalSpentVisionPoints = 0L;
    private static final Map<ResourceLocation, Integer> learnedSkills = new HashMap<>();

    public static long getSpirit() { return spirit; }
    public static long getMaxSpirit() { return maxSpirit; }
    public static long getAvailableVisionPoints() { return availableVisionPoints; }
    public static long getTotalSpentVisionPoints() { return totalSpentVisionPoints; }
    public static Map<ResourceLocation, Integer> getLearnedSkills() {
        return Collections.unmodifiableMap(learnedSkills);
    }

    public static int getSkillLevel(ResourceLocation id) {
        return learnedSkills.getOrDefault(id, 0);
    }

    public static int getVisionLevel() {
        int level = 0;
        while ((long)(level + 1) * (level + 2) / 2 * 10 <= totalSpentVisionPoints) {
            level++;
        }
        return level;
    }

    public static void update(long newSpirit, long newMaxSpirit,
                              long newAvailableVisionPoints, long newTotalSpent,
                              Map<ResourceLocation, Integer> newSkills) {
        spirit = newSpirit;
        maxSpirit = newMaxSpirit;
        availableVisionPoints = newAvailableVisionPoints;
        totalSpentVisionPoints = newTotalSpent;
        learnedSkills.clear();
        learnedSkills.putAll(newSkills);
    }
}
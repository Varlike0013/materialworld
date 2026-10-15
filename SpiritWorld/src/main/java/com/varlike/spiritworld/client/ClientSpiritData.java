package com.varlike.spiritworld.client;

import com.varlike.spiritworld.skill.SkillNode;
import com.varlike.spiritworld.skill.SkillTree;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class ClientSpiritData {

    private static long spirit = 0L;
    private static long maxSpirit = 100L;
    private static long availableVisionPoints = 0L;
    private static long totalSpentVisionPoints = 0L;
    private static long visionFragments = 0L;
    private static final Map<ResourceLocation, Integer> learnedSkills = new HashMap<>();

    public static long getSpirit() { return spirit; }
    public static long getMaxSpirit() { return maxSpirit; }
    public static long getAvailableVisionPoints() { return availableVisionPoints; }
    public static long getTotalSpentVisionPoints() { return totalSpentVisionPoints; }
    public static long getVisionFragments(){ return visionFragments; }
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

    private static final ResourceLocation[] loadout = new ResourceLocation[6];
    private static int selectedSlot = 0;
    private static final Set<ResourceLocation> activePassives = new HashSet<>();

    public static ResourceLocation getLoadoutSlot(int slot) {
        if (slot < 0 || slot >= 6) return null;
        return loadout[slot];
    }

    public static ResourceLocation[] getLoadout() { return loadout.clone(); }
    public static int getSelectedSlot() { return selectedSlot; }
    public static Set<ResourceLocation> getActivePassives() {
        return Collections.unmodifiableSet(activePassives);
    }

    public static ResourceLocation getEquippedSkill() {
        return getLoadoutSlot(selectedSlot);
    }

    public static boolean isPassiveActive(ResourceLocation id) {
        SkillNode node = SkillTree.get(id);
        if (node == null) return false;
        if (getSkillLevel(id) <= 0) return false;
        if (node.isAlwaysOn()) return true;
        return activePassives.contains(id);
    }

    public static void update(long newSpirit, long newMaxSpirit,
                              long newAvail, long newTotalSpent,
                              long newVisionFragments,
                              Map<ResourceLocation, Integer> newSkills,
                              ResourceLocation[] newLoadout,
                              int newSelectedSlot,
                              Set<ResourceLocation> newPassives) {
        spirit = newSpirit;
        maxSpirit = newMaxSpirit;
        availableVisionPoints = newAvail;
        totalSpentVisionPoints = newTotalSpent;
        visionFragments = newVisionFragments;
        learnedSkills.clear();
        learnedSkills.putAll(newSkills);
        for (int i = 0; i < 6; i++) {
            loadout[i] = (newLoadout != null && i < newLoadout.length) ? newLoadout[i] : null;
        }
        selectedSlot = newSelectedSlot;
        activePassives.clear();
        activePassives.addAll(newPassives);
    }
}
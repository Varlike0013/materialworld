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
    private static long baseMaxSpirit = 0L;
    private static long visionFragments = 0L;
    private static final Map<ResourceLocation, Integer> learnedSkills = new HashMap<>();
    private static int scanRange = 8;
    private static Set<ResourceLocation> scanOres = new HashSet<>();

    public static long getSpirit() { return spirit; }
    public static long getMaxSpirit() { return maxSpirit; }
    public static long getAvailableVisionPoints() { return availableVisionPoints; }
    public static long getTotalSpentVisionPoints() { return totalSpentVisionPoints; }
    public static long getBaseMaxSpirit() { return baseMaxSpirit; }
    public static long getVisionFragments(){ return visionFragments; }
    public static Map<ResourceLocation, Integer> getLearnedSkills() {
        return Collections.unmodifiableMap(learnedSkills);
    }
    public static int getScanRange() { return scanRange; }
    public static Set<ResourceLocation> getScanOres() { return scanOres; }

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

    /**
     * 退出世界 / 断开服务器时清空缓存，避免切换到另一个存档后
     * HUD 和技能树短暂显示上一个世界的进度。
     */
    public static void reset() {
        spirit = 0L;
        maxSpirit = 100L;
        availableVisionPoints = 0L;
        totalSpentVisionPoints = 0L;
        visionFragments = 0L;
        learnedSkills.clear();
        Arrays.fill(loadout, null);
        selectedSlot = 0;
        activePassives.clear();
    }

    public static void update(long newSpirit, long newMaxSpirit,long newBaseMaxSpirit,
                              long newAvail, long newTotalSpent,
                              long newVisionFragments,
                              Map<ResourceLocation, Integer> newSkills,
                              ResourceLocation[] newLoadout,
                              int newSelectedSlot,
                              Set<ResourceLocation> newPassives,
                              int newScanRange, Set<ResourceLocation> newScanOres) {
        maxSpirit = Math.max(0L, newMaxSpirit);
        spirit = Math.clamp(newSpirit, 0L, maxSpirit);
        availableVisionPoints = newAvail;
        totalSpentVisionPoints = newTotalSpent;
        visionFragments = newVisionFragments;
        baseMaxSpirit = Math.max(0L, newBaseMaxSpirit);
        learnedSkills.clear();
        learnedSkills.putAll(newSkills);
        for (int i = 0; i < 6; i++) {
            loadout[i] = (newLoadout != null && i < newLoadout.length) ? newLoadout[i] : null;
        }
        selectedSlot = newSelectedSlot;
        activePassives.clear();
        activePassives.addAll(newPassives);
        scanRange = Math.max(8, newScanRange);
        scanOres = newScanOres == null ? new HashSet<>() : new HashSet<>(newScanOres);
    }
}
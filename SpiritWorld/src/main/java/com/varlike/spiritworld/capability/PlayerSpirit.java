package com.varlike.spiritworld.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 玩家精神力与灵视数据。
 * Player spirit and spirit-vision data.
 */
public class PlayerSpirit {

    /** 精神力上限默认值 / Default max spirit */
    public static final long DEFAULT_MAX_SPIRIT = 100L;

    /** 灵视等级上限 / Max vision level */
    public static final int MAX_VISION_LEVEL = 10;

    private long spirit = DEFAULT_MAX_SPIRIT;
    private long maxSpirit = DEFAULT_MAX_SPIRIT;

    /** 临时上限加成，key 为来源 ID，value 为加成量（可正可负） */
    private final Map<ResourceLocation, Long> maxSpiritModifiers = new HashMap<>();

    /** 是否已初始化 / Whether the data has been initialized */
    private transient boolean initialized = false;

    /** 脏标记，用于节流同步 / Dirty flag for throttled sync */
    private transient boolean dirty = false;

    // ==================== 精神力 / Spirit ====================

    public long getSpirit() {
        return spirit;
    }

    public void setSpirit(long value) {
        long old = this.spirit;
        this.spirit = clamp(value, 0, getEffectiveMaxSpirit());
        if (this.spirit != old) markDirty();
    }

    public long getMaxSpirit() {
        return maxSpirit;
    }

    public void setMaxSpirit(long value) {
        this.maxSpirit = Math.max(0, value);
        if (spirit > getEffectiveMaxSpirit()) spirit = getEffectiveMaxSpirit();
        markDirty();
    }

    /**
     * 有效上限 = 基础上限 + 所有临时加成之和。
     * Effective max = base max + sum of all temporary modifiers.
     */
    public long getEffectiveMaxSpirit() {
        long total = maxSpirit;
        for (long v : maxSpiritModifiers.values()) total += v;
        return Math.max(0, total);
    }

    public void addSpirit(long amount) {
        setSpirit(spirit + amount);
    }

    public boolean consumeSpirit(long amount) {
        if (amount < 0 || spirit < amount) return false;
        spirit -= amount;
        markDirty();
        return true;
    }

    // ==================== 临时上限加成 / Temp Max Modifiers ====================

    /**
     * 添加或覆盖一个临时上限加成。
     * Add or overwrite a temporary max-spirit modifier.
     *
     * @param id     来源唯一标识（例如 buff 的注册名）/ Source identifier
     * @param amount 加成量，可为负 / Modifier amount, can be negative
     */
    public void addMaxSpiritModifier(ResourceLocation id, long amount) {
        maxSpiritModifiers.put(id, amount);
        if (spirit > getEffectiveMaxSpirit()) spirit = getEffectiveMaxSpirit();
        markDirty();
    }

    /** 移除指定来源的临时加成 / Remove a modifier by id */
    public void removeMaxSpiritModifier(ResourceLocation id) {
        if (maxSpiritModifiers.remove(id) != null) {
            if (spirit > getEffectiveMaxSpirit()) spirit = getEffectiveMaxSpirit();
            markDirty();
        }
    }

    /** 清空所有临时加成 / Clear all temporary modifiers */
    public void clearMaxSpiritModifiers() {
        if (!maxSpiritModifiers.isEmpty()) {
            maxSpiritModifiers.clear();
            if (spirit > getEffectiveMaxSpirit()) spirit = getEffectiveMaxSpirit();
            markDirty();
        }
    }

    public Map<ResourceLocation, Long> getMaxSpiritModifiers() {
        return Collections.unmodifiableMap(maxSpiritModifiers);
    }

    // ===== 新增字段 =====
    private final Map<ResourceLocation, Integer> learnedSkills = new HashMap<>();
    private long availableVisionPoints = 0L;
    private long totalSpentVisionPoints = 0L;

    // ===== 技能相关 =====
    public int getSkillLevel(ResourceLocation id) {
        return learnedSkills.getOrDefault(id, 0);
    }

    public void setSkillLevel(ResourceLocation id, int level) {
        if (level <= 0) learnedSkills.remove(id);
        else learnedSkills.put(id, level);
        markDirty();
    }

    public Map<ResourceLocation, Integer> getLearnedSkills() {
        return Collections.unmodifiableMap(learnedSkills);
    }

    public void clearLearnedSkills() {
        learnedSkills.clear();
        markDirty();
    }

    // ===== 灵视点 =====
    public long getAvailableVisionPoints() { return availableVisionPoints; }

    public void setAvailableVisionPoints(long v) {
        this.availableVisionPoints = Math.max(0, v);
        markDirty();
    }

    public void addVisionPoints(long v) {
        this.availableVisionPoints += v;
        markDirty();
    }

    public boolean consumeVisionPoints(long v) {
        if (v < 0 || availableVisionPoints < v) return false;
        availableVisionPoints -= v;
        markDirty();
        return true;
    }

    public long getTotalSpentVisionPoints() { return totalSpentVisionPoints; }

    public void setTotalSpentVisionPoints(long v) {
        this.totalSpentVisionPoints = Math.max(0, v);
        markDirty();
    }

    public void addSpentVisionPoints(long v) {
        this.totalSpentVisionPoints += v;
        markDirty();
    }

    // ===== 灵视等级：根据已消耗点数动态计算 =====
    // ==================== 灵视 / Spirit Vision ====================

    public int getVisionLevel() {
        int level = 0;
        while ((long)(level + 1) * (level + 2) / 2 * 10 <= totalSpentVisionPoints) {
            level++;
        }
        return level;
    }

    public boolean isVisionAtLeast(int level) {
        return getVisionLevel() >= level;
    }

    // ==================== 初始化 / Initialization ====================

    public boolean isInitialized() {
        return initialized;
    }

    public void markInitialized() {
        this.initialized = true;
    }

    // ==================== 复制与序列化 / Copy & Serialization ====================

    public void copyFrom(PlayerSpirit other) {
        this.spirit = other.spirit;
        this.maxSpirit = other.maxSpirit;
        this.maxSpiritModifiers.clear();
        this.maxSpiritModifiers.putAll(other.maxSpiritModifiers);
        this.initialized = other.initialized;
        markDirty();
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("spirit", spirit);
        tag.putLong("maxSpirit", maxSpirit);
        tag.putLong("availableVisionPoints", availableVisionPoints);
        tag.putLong("totalSpentVisionPoints", totalSpentVisionPoints);

        CompoundTag skillsTag = new CompoundTag();
        learnedSkills.forEach((id, lvl) -> skillsTag.putInt(id.toString(), lvl));
        tag.put("learnedSkills", skillsTag);

        return tag;
    }

    public void load(CompoundTag tag) {
        this.spirit = tag.getLong("spirit");
        this.maxSpirit = tag.contains("maxSpirit") ? tag.getLong("maxSpirit") : DEFAULT_MAX_SPIRIT;
        this.availableVisionPoints = tag.getLong("availableVisionPoints");
        this.totalSpentVisionPoints = tag.getLong("totalSpentVisionPoints");

        learnedSkills.clear();
        CompoundTag skillsTag = tag.getCompound("learnedSkills");
        for (String key : skillsTag.getAllKeys()) {
            ResourceLocation id = ResourceLocation.tryParse(key);
            if (id != null) {
                int lvl = skillsTag.getInt(key);
                if (lvl > 0) learnedSkills.put(id, lvl);
            }
        }

        if (spirit > getEffectiveMaxSpirit()) spirit = getEffectiveMaxSpirit();
        initialized = true;
    }

    // ==================== Dirty ====================

    public void markDirty() { this.dirty = true; }
    public boolean isDirty() { return dirty; }
    public void clearDirty() { this.dirty = false; }

    // ==================== 工具 / Utils ====================

    private static long clamp(long v, long min, long max) {
        return Math.max(min, Math.min(max, v));
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }


}
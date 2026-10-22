package com.varlike.spiritworld.capability;

import com.varlike.spiritworld.skill.ModSkills;
import com.varlike.spiritworld.skill.SkillNode;
import com.varlike.spiritworld.skill.SkillTree;
import com.varlike.spiritworld.skill.SkillValues;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

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

    /**
     * 是否已经和存档对接过（load 成功或登录流程走完）。
     * 登录握手阶段玩家可能先被 tick，此时若把默认值写回 NBT 会覆盖真实存档。
     */
    private transient boolean persistenceReady = false;
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
        long base = maxSpirit;
        for (long v : maxSpiritModifiers.values()) base += v;
        base += getSkillMaxSpiritFlatBonus();
        base = (long) (base * (1.0 + getSkillMaxSpiritPercentBonus()));
        return Math.max(0, base);
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
        if (level <= 0) {
            learnedSkills.remove(id);
            // 清理技能栏引用
            for (int i = 0; i < LOADOUT_SIZE; i++) {
                if (id.equals(loadout[i])) loadout[i] = null;
            }
            // 清理被动激活状态
            activePassives.remove(id);
        } else {
            learnedSkills.put(id, level);
        }
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
    //
    private long visionFragments = 0;
    private transient double regenAccumulator = 0;
    public long getVisionFragments() { return visionFragments; }

    public void addVisionFragments(long amount) {
        if (amount <= 0) return;
        this.visionFragments += amount;
        while (this.visionFragments >= 100) {
            this.visionFragments -= 100;
            this.availableVisionPoints += 1;
        }
        markDirty();
    }

    /** 带小数的精神力恢复，累积到整数再加 */
    public void addSpiritWithFraction(double amount) {
        if (amount <= 0) return;
        this.regenAccumulator += amount;
        long whole = (long) this.regenAccumulator;
        if (whole > 0) {
            this.regenAccumulator -= whole;
            addSpirit(whole);
        }
    }

    // ===== 技能加成 =====
    public long getSkillMaxSpiritFlatBonus() {
        return getSkillLevel(ModSkills.SPIRIT_ENDURANCE_ID) * SkillValues.SPIRIT_ENDURANCE_MAX_PER_LEVEL;
    }
    public double getSkillMaxSpiritPercentBonus() {
        double bonus = getSkillLevel(ModSkills.SPIRIT_STRENGTH_ID)
                * SkillValues.SPIRIT_STRENGTH_MAX_PERCENT_PER_LEVEL;
        if (getSkillLevel(ModSkills.SPIRIT_SOURCE_ID) > 0) {
            bonus += SkillValues.SPIRIT_SOURCE_MAX_PERCENT;
        }
        return bonus;
    }
    public double getSkillRegenFlatBonus() {
        return getSkillLevel(ModSkills.SPIRIT_TOUGHNESS_ID)
                * SkillValues.SPIRIT_TOUGHNESS_REGEN_PER_LEVEL;
    }
    public double getSkillRegenPercentBonus() {
        double bonus = getSkillLevel(ModSkills.SPIRIT_SURGE_ID)
                * SkillValues.SPIRIT_SURGE_REGEN_PERCENT_PER_LEVEL;
        if (getSkillLevel(ModSkills.SPIRIT_SOURCE_ID) > 0) {
            bonus += SkillValues.SPIRIT_SOURCE_REGEN_PERCENT;
        }
        return bonus;
    }
    public double getSpiritFlowRegenPerSecond() {
        return getSkillLevel(ModSkills.SPIRIT_FLOW_ID)
                * SkillValues.SPIRIT_FLOW_REGEN_PER_SECOND_PER_LEVEL;
    }

    public long getVisionFragmentFlatBonus() {
        return getSkillLevel(ModSkills.VISION_SHARPNESS_ID)
                * SkillValues.VISION_SHARPNESS_FRAGMENTS_PER_LEVEL;
    }

    public double getVisionFragmentPercentBonus() {
        return getSkillLevel(ModSkills.VISION_MASTERY_ID)
                * SkillValues.VISION_MASTERY_FRAGMENT_PERCENT_PER_LEVEL;
    }

    public double getEmcCostMultiplier() {
        double reduction = getSkillLevel(ModSkills.EMC_AFFINITY_ID)
                * SkillValues.EMC_AFFINITY_COST_REDUCTION_PER_LEVEL;
        return Math.max(0.0, 1.0 - reduction);
    }
    // ===== 技能栏 / 装配 =====
    public static final int LOADOUT_SIZE = 6;

    private final ResourceLocation[] loadout = new ResourceLocation[LOADOUT_SIZE];
    private int selectedSlot = 0;
    private final Set<ResourceLocation> activePassives = new HashSet<>();
    // ===== 技能栏 =====
    public ResourceLocation getLoadoutSlot(int slot) {
        if (slot < 0 || slot >= LOADOUT_SIZE) return null;
        return loadout[slot];
    }

    public void setLoadoutSlot(int slot, ResourceLocation id) {
        if (slot < 0 || slot >= LOADOUT_SIZE) return;
        loadout[slot] = id;
        markDirty();
    }

    public ResourceLocation[] getLoadout() {
        return loadout.clone();
    }

    public int getSelectedSlot() { return selectedSlot; }

    public void setSelectedSlot(int slot) {
        if (slot < 0 || slot >= LOADOUT_SIZE) return;
        this.selectedSlot = slot;
        markDirty();
    }

    public ResourceLocation getEquippedSkill() {
        return getLoadoutSlot(selectedSlot);
    }

    // ===== 被动 =====
    public Set<ResourceLocation> getActivePassives() {
        return Collections.unmodifiableSet(activePassives);
    }

    public void addPassive(ResourceLocation id) {
        activePassives.add(id);
        markDirty();
    }

    public void removePassive(ResourceLocation id) {
        activePassives.remove(id);
        markDirty();
    }

    /**
     * 判断某被动技能当前是否生效。
     * 常驻被动只要学了就生效；其他被动需要 activePassives 中包含。
     */
    public boolean isPassiveActive(ResourceLocation id) {
        SkillNode node = SkillTree.get(id);
        if (node == null) return false;
        if (getSkillLevel(id) <= 0) return false;
        if (node.isAlwaysOn()) return true;
        return activePassives.contains(id);
    }
    // ===== 精神工具状态 =====
    private boolean spiritToolActive = false;

    public boolean isSpiritToolActive() { return spiritToolActive; }

    public void setSpiritToolActive(boolean active) {
        this.spiritToolActive = active;
        markDirty();
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
        this.learnedSkills.clear();
        this.learnedSkills.putAll(other.learnedSkills);
        this.availableVisionPoints = other.availableVisionPoints;
        this.totalSpentVisionPoints = other.totalSpentVisionPoints;
        System.arraycopy(other.loadout, 0, this.loadout, 0, LOADOUT_SIZE);
        this.selectedSlot = other.selectedSlot;
        this.activePassives.clear();
        this.activePassives.addAll(other.activePassives);
        this.spiritToolActive = other.spiritToolActive;
        this.visionFragments = other.visionFragments;
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

        // 技能栏
        CompoundTag loadoutTag = new CompoundTag();
        for (int i = 0; i < LOADOUT_SIZE; i++) {
            if (loadout[i] != null) loadoutTag.putString("slot" + i, loadout[i].toString());
        }
        tag.put("loadout", loadoutTag);
        tag.putInt("selectedSlot", selectedSlot);

        // 激活的被动
        ListTag passiveList = new ListTag();
        for (ResourceLocation pid : activePassives) {
            passiveList.add(StringTag.valueOf(pid.toString()));
        }
        tag.put("activePassives", passiveList);
        tag.putBoolean("spiritToolActive", spiritToolActive);
        tag.putLong("visionFragments", visionFragments);

        return tag;
    }

    public void load(CompoundTag tag) {
        // 覆盖前先清空旧状态，避免存档缺字段时残留上一次（或默认值）的数据
        this.maxSpiritModifiers.clear();
        this.learnedSkills.clear();
        Arrays.fill(this.loadout, null);
        this.activePassives.clear();

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

        CompoundTag loadoutTag = tag.getCompound("loadout");
        for (int i = 0; i < LOADOUT_SIZE; i++) {
            String key = "slot" + i;
            if (loadoutTag.contains(key)) {
                loadout[i] = ResourceLocation.tryParse(loadoutTag.getString(key));
            }
        }
        selectedSlot = tag.getInt("selectedSlot");
        if (selectedSlot < 0 || selectedSlot >= LOADOUT_SIZE) selectedSlot = 0;

        activePassives.clear();
        ListTag passiveList = tag.getList("activePassives", Tag.TAG_STRING);
        for (int i = 0; i < passiveList.size(); i++) {
            ResourceLocation pid = ResourceLocation.tryParse(passiveList.getString(i));
            if (pid != null) activePassives.add(pid);
        }
        this.spiritToolActive = tag.getBoolean("spiritToolActive");
        this.visionFragments = tag.getLong("visionFragments");
        if (spirit > getEffectiveMaxSpirit()) spirit = getEffectiveMaxSpirit();
        initialized = true;
        persistenceReady = true;
        markDirty();
    }

    // ==================== Dirty ====================

    public void markDirty() { this.dirty = true; }
    public boolean isDirty() { return dirty; }
    public void clearDirty() { this.dirty = false; }

    /** 存档是否已经对接完成 */
    public boolean isPersistenceReady() { return persistenceReady; }

    /** 标记存档已对接（登录载入完成 / 新玩家建立存档后调用） */
    public void markPersistenceReady() { this.persistenceReady = true; }

    // ==================== 工具 / Utils ====================

    private static long clamp(long v, long min, long max) {
        return Math.max(min, Math.min(max, v));
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }


}
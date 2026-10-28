package com.varlike.spiritworld.capability;

import com.varlike.spiritworld.skill.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

/**
 * 玩家精神力与灵视数据。
 * <p>
 * 数据域：
 * <ol>
 *   <li>精神力核心（spirit / maxSpirit / 临时 modifier）</li>
 *   <li>技能进度（learnedSkills / 灵视点 / 灵视碎片 / 灵视等级）</li>
 *   <li>技能栏（loadout / selectedSlot / activePassives）</li>
 *   <li>冷却（transient，不序列化）</li>
 *   <li>临时状态（工具 / 飞行 / 扫描）</li>
 *   <li>flags（initialized / dirty / persistenceReady）</li>
 * </ol>
 * 计算逻辑委托：{@link SpiritMaxCalculator} / {@link SkillBonusCalculator} / {@link SkillCooldownCalculator}。
 */
public class PlayerSpirit {

    // ==================== 常量 ====================

    public static final long DEFAULT_MAX_SPIRIT = 100L;
    public static final int MAX_VISION_LEVEL = 10;
    public static final int LOADOUT_SIZE = 6;

    // ==================== 字段区 ====================

    // ----- 1. 精神力核心 -----
    private long spirit = DEFAULT_MAX_SPIRIT;
    private long maxSpirit = DEFAULT_MAX_SPIRIT;
    /** 临时上限加成（固定值 + 百分比），transient，不序列化 */
    private final Map<ResourceLocation, MaxSpiritModifier> maxSpiritModifiers = new HashMap<>();
    /** 带小数的精神力恢复累积器 */
    private transient double regenAccumulator = 0;

    // ----- 2. 技能进度 -----
    private final Map<ResourceLocation, Integer> learnedSkills = new HashMap<>();
    private long availableVisionPoints = 0L;
    private long totalSpentVisionPoints = 0L;
    private long visionFragments = 0L;

    // ----- 3. 技能栏 -----
    private final ResourceLocation[] loadout = new ResourceLocation[LOADOUT_SIZE];
    private int selectedSlot = 0;
    private final Set<ResourceLocation> activePassives = new HashSet<>();

    // ----- 4. 冷却（transient，不序列化、不跨死亡复制）-----
    private final transient Map<ResourceLocation, Long> skillCooldownEnds = new HashMap<>();

    // ----- 5. 临时状态 -----
    private boolean spiritToolActive = false;
    private transient double flightCostAccum = 0.0;
    private transient boolean flightGliding = false;
    private int scanRange = SkillValues.SCAN_RANGE_MIN;
    private Set<ResourceLocation> scanOres = new HashSet<>();

    // ----- 6. flags -----
    private transient boolean initialized = false;
    private transient boolean dirty = false;
    /** 是否已经和存档对接过（登录握手阶段禁止把默认值写回 NBT） */
    private transient boolean persistenceReady = false;

    // ==================== 1. 精神力核心 ====================

    public long getSpirit() {
        return spirit;
    }

    public void setSpirit(long value) {
        long old = this.spirit;
        this.spirit = Math.clamp(value, 0, getEffectiveMaxSpirit());
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

    /** 基础上限：maxSpirit + 临时 modifier + 技能加成，不含夜视/饱食惩罚 */
    public long getBaseMaxSpirit() {
        return SpiritMaxCalculator.baseMax(this);
    }

    /** 最终上限：基础值叠加所有激活惩罚 */
    public long getEffectiveMaxSpirit() {
        return SpiritMaxCalculator.effectiveMax(this);
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

    // ==================== 2. 临时上限加成 ====================

    /**
     * 单个临时上限加成条目。固定值和百分比可独立为正负。
     */
    public record MaxSpiritModifier(long fixed, double percent) {
        public static MaxSpiritModifier fixed(long v) {
            return new MaxSpiritModifier(v, 0.0);
        }

        public static MaxSpiritModifier percent(double p) {
            return new MaxSpiritModifier(0L, p);
        }

        public static MaxSpiritModifier of(long fixed, double percent) {
            return new MaxSpiritModifier(fixed, percent);
        }

        public boolean isEmpty() {
            return fixed == 0L && Math.abs(percent) < 1.0E-9;
        }
    }

    public void addMaxSpiritModifier(ResourceLocation id, long amount) {
        addMaxSpiritModifier(id, amount, 0.0);
    }

    /**
     * 添加或覆盖临时上限加成。
     *
     * @param fixed   固定值，可负；0 表示无
     * @param percent 百分比，0.05 = +5%，-0.2 = -20%；0 表示无
     */
    public void addMaxSpiritModifier(ResourceLocation id, long fixed, double percent) {
        MaxSpiritModifier m = new MaxSpiritModifier(fixed, percent);
        if (m.isEmpty()) {
            removeMaxSpiritModifier(id);
            return;
        }
        maxSpiritModifiers.put(id, m);
        clampSpiritToMax();
        markDirty();
    }

    public void addMaxSpiritPercentModifier(ResourceLocation id, double percent) {
        addMaxSpiritModifier(id, 0L, percent);
    }

    public void removeMaxSpiritModifier(ResourceLocation id) {
        if (maxSpiritModifiers.remove(id) != null) {
            clampSpiritToMax();
            markDirty();
        }
    }

    public void clearMaxSpiritModifiers() {
        if (!maxSpiritModifiers.isEmpty()) {
            maxSpiritModifiers.clear();
            clampSpiritToMax();
            markDirty();
        }
    }

    public Map<ResourceLocation, MaxSpiritModifier> getMaxSpiritModifiers() {
        return Collections.unmodifiableMap(maxSpiritModifiers);
    }

    public MaxSpiritModifier getMaxSpiritModifier(ResourceLocation id) {
        return maxSpiritModifiers.get(id);
    }

    /** 将精神力夹到有效上限内（含下限 0） */
    private void clampSpiritToMax() {
        long eff = getEffectiveMaxSpirit();
        if (spirit > eff) spirit = eff;
        if (spirit < 0L) spirit = 0L;
    }

    // ==================== 3. 技能进度 ====================

    // ----- 3.1 已学技能 -----

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

    // ----- 3.2 灵视点 -----

    public long getAvailableVisionPoints() {
        return availableVisionPoints;
    }

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

    public long getTotalSpentVisionPoints() {
        return totalSpentVisionPoints;
    }

    public void setTotalSpentVisionPoints(long v) {
        this.totalSpentVisionPoints = Math.max(0, v);
        markDirty();
    }

    public void addSpentVisionPoints(long v) {
        this.totalSpentVisionPoints += v;
        markDirty();
    }

    // ----- 3.3 灵视等级（由已消耗点数动态计算）-----

    public int getVisionLevel() {
        int level = 0;
        while ((long) (level + 1) * (level + 2) / 2 * 10 <= totalSpentVisionPoints) {
            level++;
        }
        return level;
    }

    public boolean isVisionAtLeast(int level) {
        return getVisionLevel() >= level;
    }

    // ----- 3.4 灵视碎片 -----

    public long getVisionFragments() {
        return visionFragments;
    }

    public void addVisionFragments(long amount) {
        if (amount <= 0) return;
        this.visionFragments += amount;
        while (this.visionFragments >= 100) {
            this.visionFragments -= 100;
            this.availableVisionPoints += 1;
        }
        markDirty();
    }

    // ==================== 4. 技能加成（委托 Calculator）====================

    public long getSkillMaxSpiritFlatBonus() {
        return SkillBonusCalculator.maxSpiritFlat(this);
    }

    public double getSkillMaxSpiritPercentBonus() {
        return SkillBonusCalculator.maxSpiritPercent(this);
    }

    public double getSkillRegenFlatBonus() {
        return SkillBonusCalculator.regenFlat(this);
    }

    public double getSkillRegenPercentBonus() {
        return SkillBonusCalculator.regenPercent(this);
    }

    public double getSpiritFlowRegenPerSecond() {
        return SkillBonusCalculator.flowRegenPerSecond(this);
    }

    public long getVisionFragmentFlatBonus() {
        return SkillBonusCalculator.visionFragmentFlat(this);
    }

    public double getVisionFragmentPercentBonus() {
        return SkillBonusCalculator.visionFragmentPercent(this);
    }

    public double getEmcCostMultiplier() {
        return SkillBonusCalculator.emcCostMultiplier(this);
    }

    // ==================== 5. 冷却 ====================

    public double getCooldownMultiplier() {
        return SkillCooldownCalculator.multiplier(this);
    }

    public long getEffectiveCooldown(ResourceLocation skillId) {
        return SkillCooldownCalculator.effectiveTicks(this, skillId);
    }

    public boolean isOnCooldown(ResourceLocation skillId, long gameTime) {
        Long end = skillCooldownEnds.get(skillId);
        return end != null && gameTime < end;
    }

    public long getCooldownRemaining(ResourceLocation skillId, long gameTime) {
        Long end = skillCooldownEnds.get(skillId);
        return end == null ? 0L : Math.max(0L, end - gameTime);
    }

    public void setCooldown(ResourceLocation skillId, long endGameTime) {
        if (endGameTime <= 0L) {
            skillCooldownEnds.remove(skillId);
        } else {
            skillCooldownEnds.put(skillId, endGameTime);
        }
    }

    public void clearCooldown(ResourceLocation skillId) {
        skillCooldownEnds.remove(skillId);
    }

    public void clearAllCooldowns() {
        skillCooldownEnds.clear();
    }

    // ==================== 6. 技能栏 ====================

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

    public int getSelectedSlot() {
        return selectedSlot;
    }

    public void setSelectedSlot(int slot) {
        if (slot < 0 || slot >= LOADOUT_SIZE) return;
        this.selectedSlot = slot;
        markDirty();
    }

    public ResourceLocation getEquippedSkill() {
        return getLoadoutSlot(selectedSlot);
    }

    // ----- 6.1 被动 -----

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

    /** 常驻被动只要学了就生效；其他被动需要 activePassives 中包含。 */
    public boolean isPassiveActive(ResourceLocation id) {
        if (getSkillLevel(id) <= 0) return false;
        SkillNode node = SkillTree.get(id);
        if (node == null) return false;
        if (node.isAlwaysOn()) return true;
        return activePassives.contains(id);
    }

    // ==================== 7. 精神工具状态 ====================

    public boolean isSpiritToolActive() {
        return spiritToolActive;
    }

    public void setSpiritToolActive(boolean active) {
        this.spiritToolActive = active;
        markDirty();
    }

    // ==================== 8. 飞行状态（transient）====================

    public double getFlightCostAccum() {
        return flightCostAccum;
    }

    public void setFlightCostAccum(double v) {
        flightCostAccum = v;
    }

    public boolean isFlightGliding() {
        return flightGliding;
    }

    public void setFlightGliding(boolean v) {
        flightGliding = v;
    }

    // ==================== 9. 扫描配置 ====================

    public int getScanRange() {
        return scanRange;
    }

    public void setScanRange(int range) {
        scanRange = Math.max(SkillValues.SCAN_RANGE_MIN, range);
    }

    public Set<ResourceLocation> getScanOres() {
        return scanOres;
    }

    public void setScanOres(Set<ResourceLocation> ores) {
        this.scanOres = ores == null ? new HashSet<>() : new HashSet<>(ores);
    }

    public void toggleScanOre(ResourceLocation oreId) {
        if (!scanOres.remove(oreId)) scanOres.add(oreId);
    }

    // ==================== 10. 初始化 / 持久化 ====================

    public boolean isInitialized() {
        return initialized;
    }

    public void markInitialized() {
        this.initialized = true;
    }

    // ----- 10.1 复制 -----

    public void copyFrom(PlayerSpirit other) {
        // 精神力核心
        this.spirit = other.spirit;
        this.maxSpirit = other.maxSpirit;
        this.maxSpiritModifiers.clear();
        this.maxSpiritModifiers.putAll(other.maxSpiritModifiers);

        // 技能进度
        this.learnedSkills.clear();
        this.learnedSkills.putAll(other.learnedSkills);
        this.availableVisionPoints = other.availableVisionPoints;
        this.totalSpentVisionPoints = other.totalSpentVisionPoints;
        this.visionFragments = other.visionFragments;

        // 技能栏
        System.arraycopy(other.loadout, 0, this.loadout, 0, LOADOUT_SIZE);
        this.selectedSlot = other.selectedSlot;
        this.activePassives.clear();
        this.activePassives.addAll(other.activePassives);

        // 临时状态
        this.spiritToolActive = other.spiritToolActive;
        this.scanRange = other.scanRange;
        this.scanOres = new HashSet<>(other.scanOres);

        // flags（flightCostAccum / flightGliding / skillCooldownEnds 是 transient，不复制）
        this.initialized = other.initialized;

        markDirty();
    }

    // ----- 10.2 序列化 -----

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();

        // 精神力核心
        tag.putLong("spirit", spirit);
        tag.putLong("maxSpirit", maxSpirit);

        // 技能进度
        tag.putLong("availableVisionPoints", availableVisionPoints);
        tag.putLong("totalSpentVisionPoints", totalSpentVisionPoints);
        tag.putLong("visionFragments", visionFragments);
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
        ListTag passiveList = new ListTag();
        for (ResourceLocation pid : activePassives) {
            passiveList.add(StringTag.valueOf(pid.toString()));
        }
        tag.put("activePassives", passiveList);

        // 临时状态
        tag.putBoolean("spiritToolActive", spiritToolActive);
        tag.putInt("scanRange", scanRange);
        ListTag scanList = new ListTag();
        for (ResourceLocation id : scanOres) {
            scanList.add(StringTag.valueOf(id.toString()));
        }
        tag.put("scanOres", scanList);

        return tag;
    }

    public void load(CompoundTag tag) {
        // 覆盖前先清空旧状态，避免存档缺字段时残留上一次（或默认值）的数据
        this.maxSpiritModifiers.clear();
        this.learnedSkills.clear();
        Arrays.fill(this.loadout, null);
        this.activePassives.clear();

        // 精神力核心
        this.spirit = tag.getLong("spirit");
        this.maxSpirit = tag.contains("maxSpirit") ? tag.getLong("maxSpirit") : DEFAULT_MAX_SPIRIT;

        // 技能进度
        this.availableVisionPoints = tag.getLong("availableVisionPoints");
        this.totalSpentVisionPoints = tag.getLong("totalSpentVisionPoints");
        this.visionFragments = tag.getLong("visionFragments");
        CompoundTag skillsTag = tag.getCompound("learnedSkills");
        for (String key : skillsTag.getAllKeys()) {
            ResourceLocation id = ResourceLocation.tryParse(key);
            if (id != null) {
                int lvl = skillsTag.getInt(key);
                if (lvl > 0) learnedSkills.put(id, lvl);
            }
        }

        // 技能栏
        CompoundTag loadoutTag = tag.getCompound("loadout");
        for (int i = 0; i < LOADOUT_SIZE; i++) {
            String key = "slot" + i;
            if (loadoutTag.contains(key)) {
                loadout[i] = ResourceLocation.tryParse(loadoutTag.getString(key));
            }
        }
        selectedSlot = tag.getInt("selectedSlot");
        if (selectedSlot < 0 || selectedSlot >= LOADOUT_SIZE) selectedSlot = 0;
        ListTag passiveList = tag.getList("activePassives", Tag.TAG_STRING);
        for (int i = 0; i < passiveList.size(); i++) {
            ResourceLocation pid = ResourceLocation.tryParse(passiveList.getString(i));
            if (pid != null) activePassives.add(pid);
        }

        // 临时状态
        this.spiritToolActive = tag.getBoolean("spiritToolActive");
        if (tag.contains("scanRange")) {
            scanRange = tag.getInt("scanRange");
        }
        scanOres.clear();
        if (tag.contains("scanOres")) {
            ListTag scanList = tag.getList("scanOres", Tag.TAG_STRING);
            for (int i = 0; i < scanList.size(); i++) {
                ResourceLocation id = ResourceLocation.tryParse(scanList.getString(i));
                if (id != null) scanOres.add(id);
            }
        }

        // 上限缩小时 clamp
        if (spirit > getEffectiveMaxSpirit()) spirit = getEffectiveMaxSpirit();

        initialized = true;
        persistenceReady = true;
        markDirty();
    }

    // ----- 10.3 dirty / persistenceReady -----

    public void markDirty() {
        this.dirty = true;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void clearDirty() {
        this.dirty = false;
    }

    public boolean isPersistenceReady() {
        return persistenceReady;
    }

    public void markPersistenceReady() {
        this.persistenceReady = true;
    }
}
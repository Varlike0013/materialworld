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
     * 基础有效上限：只含临时 modifier + 技能固定/百分比加成，
     * 不含夜视/饱食等「激活时惩罚」。
     */
    public long getBaseMaxSpirit() {
        long flat = maxSpirit
                + sumModifiers()
                + getSkillMaxSpiritFlatBonus();
        double percent = sumPercentModifiers()
                + getSkillMaxSpiritPercentBonus();
        return Math.max(0L, (long) (flat * (1.0 + percent)));
    }

    /** 最终有效上限：基础值叠加所有激活惩罚 */
    public long getEffectiveMaxSpirit() {
        long total = getBaseMaxSpirit();

        // 夜视惩罚
        if (isPassiveActive(ModSkills.SPIRIT_NIGHT_VISION_ID)) {
            int lv = getSkillLevel(ModSkills.SPIRIT_NIGHT_VISION_ID);
            double penalty = SkillValues.nightVisionMaxPenalty(lv);
            if (penalty > 0.0) total = (long) (total * (1.0 - penalty));
        }

        // 饱食惩罚
        if (isPassiveActive(ModSkills.SPIRIT_SATIETY_ID)) {
            int lv = getSkillLevel(ModSkills.SPIRIT_SATIETY_ID);
            double penalty = SkillValues.satietyMaxPenalty(lv);
            if (penalty > 0.0) total = (long) (total * (1.0 - penalty));
        }

        return Math.max(0L, total);
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
     * 单个临时上限加成条目。
     * 同时支持固定值和百分比，二者可独立为正负。
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

    /** 临时上限加成（固定值 + 百分比），transient，不序列化 */
    private final Map<ResourceLocation, MaxSpiritModifier> maxSpiritModifiers = new HashMap<>();

    /**
     * 添加或覆盖一个固定值临时加成。
     * 等价于 {@code addMaxSpiritModifier(id, amount, 0.0)}。
     */
    public void addMaxSpiritModifier(ResourceLocation id, long amount) {
        addMaxSpiritModifier(id, amount, 0.0);
    }

    /**
     * 添加或覆盖一个临时上限加成，同时支持固定值和百分比。
     *
     * @param fixed   固定值加成，可负；0 表示无
     * @param percent 百分比加成，0.05 = +5%，-0.2 = -20%；0 表示无
     */
    public void addMaxSpiritModifier(ResourceLocation id, long fixed, double percent) {
        MaxSpiritModifier m = new MaxSpiritModifier(fixed, percent);
        if (m.isEmpty()) {
            // 全零视作移除，避免残留空条目
            removeMaxSpiritModifier(id);
            return;
        }
        maxSpiritModifiers.put(id, m);
        clampSpiritToMax();
        markDirty();
    }

    /** 快捷：仅百分比加成，0.05 = +5%，-0.2 = -20% */
    public void addMaxSpiritPercentModifier(ResourceLocation id, double percent) {
        addMaxSpiritModifier(id, 0L, percent);
    }

    /** 移除指定来源的临时加成 */
    public void removeMaxSpiritModifier(ResourceLocation id) {
        if (maxSpiritModifiers.remove(id) != null) {
            clampSpiritToMax();
            markDirty();
        }
    }

    /** 清空所有临时加成 */
    public void clearMaxSpiritModifiers() {
        if (!maxSpiritModifiers.isEmpty()) {
            maxSpiritModifiers.clear();
            clampSpiritToMax();
            markDirty();
        }
    }

    /** 固定值总和 */
    private long sumModifiers() {
        long sum = 0L;
        for (MaxSpiritModifier m : maxSpiritModifiers.values()) {
            sum += m.fixed();
        }
        return sum;
    }

    /** 百分比总和（0.05 + 0.05 = 0.10） */
    private double sumPercentModifiers() {
        double sum = 0.0;
        for (MaxSpiritModifier m : maxSpiritModifiers.values()) {
            sum += m.percent();
        }
        return sum;
    }

    /** 返回不可修改的完整加成映射 */
    public Map<ResourceLocation, MaxSpiritModifier> getMaxSpiritModifiers() {
        return Collections.unmodifiableMap(maxSpiritModifiers);
    }

    /** 按来源查询单个加成，未设置返回 null */
    public MaxSpiritModifier getMaxSpiritModifier(ResourceLocation id) {
        return maxSpiritModifiers.get(id);
    }

    /** 将当前精神力夹到有效上限内（含下限 0） */
    private void clampSpiritToMax() {
        long eff = getEffectiveMaxSpirit();
        if (spirit > eff) spirit = eff;
        if (spirit < 0L) spirit = 0L;
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
    /** 技能冷却结束的 gameTime；transient，不序列化、不跨死亡复制 */
    private final transient java.util.Map<ResourceLocation, Long> skillCooldownEnds = new java.util.HashMap<>();
    /**
     * 冷却倍率：1.0 表示无减少。天赋/装备可通过叠加降低此值。
     * 最低 0.2，防止无限接近 0。
     */
    public double getCooldownMultiplier() {
        int lv = getSkillLevel(ModSkills.COOLDOWN_REDUCTION_ID);
        return SkillValues.cooldownMultiplier(lv);
    }
    /** 技能实际冷却 tick；0 表示无冷却 */
    public long getEffectiveCooldown(ResourceLocation skillId) {
        SkillNode node = SkillTree.get(skillId);
        if (node == null) return 0L;
        long base = node.getBaseCooldownTicks();
        if (base <= 0) return 0L;
        return (long) Math.ceil(base * getCooldownMultiplier());
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

    /** 清理某技能的冷却（重置技能、换装备时调用） */
    public void clearCooldown(ResourceLocation skillId) {
        skillCooldownEnds.remove(skillId);
    }

    /** 清理全部冷却（死亡重生、重置全部技能时调用） */
    public void clearAllCooldowns() {
        skillCooldownEnds.clear();
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

    /** 飞行消耗累积器，transient，不序列化 */
    private transient double flightCostAccum = 0.0;
    public double getFlightCostAccum() { return flightCostAccum; }
    public void setFlightCostAccum(double v) { flightCostAccum = v; }

    /** 缓降状态标记，transient */
    private transient boolean flightGliding = false;
    public boolean isFlightGliding() { return flightGliding; }
    public void setFlightGliding(boolean v) { flightGliding = v; }
    /** 玩家配置的扫描范围（格），步长 2，范围 8~等级上限 */
    private int scanRange = SkillValues.SCAN_RANGE_MIN;

    /** 玩家勾选的矿物标识集合 */
    private Set<ResourceLocation> scanOres = new HashSet<>();

    public int getScanRange() { return scanRange; }
    public void setScanRange(int range) { scanRange = Math.max(SkillValues.SCAN_RANGE_MIN, range); }

    public Set<ResourceLocation> getScanOres() { return scanOres; }
    public void setScanOres(Set<ResourceLocation> ores) {
        this.scanOres = ores == null ? new HashSet<>() : new HashSet<>(ores);
    }
    public void toggleScanOre(ResourceLocation oreId) {
        if (!scanOres.remove(oreId)) scanOres.add(oreId);
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
        this.scanRange = other.scanRange;
        this.scanOres = new HashSet<>(other.scanOres);
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
        tag.putInt("scanRange", scanRange);
        {
            ListTag list = new ListTag();
            for (ResourceLocation id : scanOres) {
                list.add(StringTag.valueOf(id.toString()));
            }
            tag.put("scanOres", list);
        }

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
        if (tag.contains("scanRange")) {
            scanRange = tag.getInt("scanRange");
        }
        scanOres.clear();
        if (tag.contains("scanOres")) {
            ListTag list = tag.getList("scanOres", Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                ResourceLocation id = ResourceLocation.tryParse(list.getString(i));
                if (id != null) scanOres.add(id);
            }
        }
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
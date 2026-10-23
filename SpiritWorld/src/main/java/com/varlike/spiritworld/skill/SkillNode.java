package com.varlike.spiritworld.skill;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class SkillNode {

    public record Prerequisite(ResourceLocation id, int minLevel) {}

    private final ResourceLocation id;
    private final SkillType type;
    private final int gridX;
    private final int gridY;
    private final int maxLevel;
    private final long baseCost;
    private final long costIncrement;
    private final int requiredVisionLevel;
    private final List<Prerequisite> prerequisites;
    private final String icon;
    private final String nameKey;
    private final String descKey;

    private final boolean equippable;
    private final boolean toggleable;
    private final boolean alwaysOn;
    private final boolean hudVisible;   // 默认 true
    /** 动态额外 tooltip 行；null 表示无。仅在客户端渲染时调用。 */
    private final Function<SkillNode, List<Component>> tooltipExtra;
    /** 未声明冷却时的哨兵值 */
    public static final long COOLDOWN_UNSET = -1L;
    /** 开关类默认冷却：1 秒 */
    public static final long DEFAULT_COOLDOWN_TOGGLE = 20L;
    /** 其他默认冷却：5 秒 */
    public static final long DEFAULT_COOLDOWN_NORMAL = 100L;
    /** 显式声明的冷却 tick；COOLDOWN_UNSET 表示按默认规则推导。0 表示无冷却。 */
    private final long cooldownTicks;

    private SkillNode(Builder b) {
        this.id = b.id;
        this.type = b.type;
        this.gridX = b.gridX;
        this.gridY = b.gridY;
        this.maxLevel = b.maxLevel;
        this.baseCost = b.baseCost;
        this.costIncrement = b.costIncrement;
        this.requiredVisionLevel = b.requiredVisionLevel;
        this.prerequisites = Collections.unmodifiableList(new ArrayList<>(b.prerequisites));
        this.icon = b.icon;
        this.nameKey = b.nameKey;
        this.descKey = b.descKey;
        this.equippable = b.equippable;
        this.toggleable = b.toggleable;
        this.alwaysOn = b.alwaysOn;
        this.hudVisible = b.hudVisible;
        this.tooltipExtra = b.tooltipExtra;
        this.cooldownTicks = b.cooldownTicks;
    }

    // ===== getters =====
    public ResourceLocation getId() { return id; }
    public SkillType getType() { return type; }
    public int getGridX() { return gridX; }
    public int getGridY() { return gridY; }
    public int getMaxLevel() { return maxLevel; }
    public long getBaseCost() { return baseCost; }
    public long getCostIncrement() { return costIncrement; }
    public int getRequiredVisionLevel() { return requiredVisionLevel; }
    public List<Prerequisite> getPrerequisites() { return prerequisites; }
    public String getIcon() { return icon; }
    public String getNameKey() { return nameKey; }
    public String getDescKey() { return descKey; }
    public boolean isEquippable() { return equippable; }
    public boolean isToggleable() { return toggleable; }
    public boolean isAlwaysOn() { return alwaysOn; }
    public boolean isHudVisible() { return hudVisible; }
    public List<Component> getExtraTooltip() {
        if (tooltipExtra == null) return List.of();
        List<Component> out = tooltipExtra.apply(this);
        return out == null ? List.of() : out;
    }
    /** 显式声明的冷却 tick；返回 COOLDOWN_UNSET 表示未声明。 */
    public long getDeclaredCooldown() { return cooldownTicks; }
    /**
     * 实际基础冷却（未考虑天赋乘数）。
     * 显式声明优先；否则开关类 1 秒，其他 5 秒。
     */
    public long getBaseCooldownTicks() {
        if (cooldownTicks != COOLDOWN_UNSET) return Math.max(0L, cooldownTicks);
        return toggleable ? DEFAULT_COOLDOWN_TOGGLE : DEFAULT_COOLDOWN_NORMAL;
    }

    /** 从当前等级升到下一级所需的灵视点 */
    public long getCostForNextLevel(int currentLevel) {
        return baseCost + currentLevel * costIncrement;
    }

    public static Builder builder(ResourceLocation id) {
        return new Builder(id);
    }

    public static class Builder {
        private final ResourceLocation id;
        private SkillType type = SkillType.PASSIVE;
        private int gridX = 0;
        private int gridY = 0;
        private int maxLevel = 1;
        private long baseCost = 1;
        private long costIncrement = 0;
        private int requiredVisionLevel = 0;
        private final List<Prerequisite> prerequisites = new ArrayList<>();
        private String icon = "★";
        private String nameKey = "";
        private String descKey = "";

        private boolean equippable = false;
        private boolean toggleable = false;
        private boolean alwaysOn = false;
        private boolean hudVisible = true;
        private long cooldownTicks = COOLDOWN_UNSET;
        private Function<SkillNode, List<Component>> tooltipExtra = null;

        private Builder(ResourceLocation id) { this.id = id; }

        public Builder grid(int x, int y) { this.gridX = x; this.gridY = y; return this; }
        public Builder maxLevel(int n) { this.maxLevel = n; return this; }
        public Builder cost(long c) { this.baseCost = c; return this; }
        public Builder costIncrement(long c) { this.costIncrement = c; return this; }
        public Builder requiredVisionLevel(int v) { this.requiredVisionLevel = v; return this; }
        public Builder prereq(ResourceLocation p) { this.prerequisites.add(new Prerequisite(p, 1)); return this; }
        public Builder prereq(ResourceLocation p, int minLevel) { this.prerequisites.add(new Prerequisite(p, minLevel)); return this; }
        public Builder icon(String s) { this.icon = s; return this; }
        public Builder name(String key) { this.nameKey = key; return this; }
        public Builder desc(String key) { this.descKey = key; return this; }
        public Builder hideInHud() { this.hudVisible = false;return this;} //使用时要在SkillType后
        public Builder showInHud() { this.hudVisible = true;return this;}

        /**
         * 设置动态额外 tooltip 行。
         * 传入的函数在客户端渲染 tooltip 时被调用，参数为技能自身，
         * 可根据等级、客户端数据等返回任意行。返回 null 或空列表视为无额外行。
         */
        public Builder tooltipExtra(Function<SkillNode, List<Component>> fn) {
            this.tooltipExtra = fn;
            return this;
        }
        /** 显式设置冷却 tick；0 表示无冷却。 */
        public Builder cooldown(long ticks) {
            this.cooldownTicks = Math.max(0L, ticks);
            return this;
        }
        public Builder passiveAlways() {
            this.type = SkillType.PASSIVE; this.equippable = false; this.toggleable = false;
            this.alwaysOn = true;this.hudVisible = false;
            return this;
        }
        public Builder passiveToggle() {
            this.type = SkillType.PASSIVE; this.equippable = false; this.toggleable = true;
            this.alwaysOn = false; this.hudVisible = true;
            return this;
        }
        public Builder passiveEquippable() {
            this.type = SkillType.PASSIVE; this.equippable = true; this.toggleable = true;
            this.alwaysOn = false; this.hudVisible = true;this.cooldownTicks = 1L;
            return this;
        }
        public Builder active() {
            this.type = SkillType.ACTIVE; this.equippable = true; this.toggleable = false;
            this.alwaysOn = false; this.hudVisible = true;this.cooldownTicks = 5L;
            return this;
        }
        public Builder specialEquippable() {
            this.type = SkillType.SPECIAL; this.equippable = true; this.toggleable = false;
            this.alwaysOn = false; this.hudVisible = true;
            return this;
        }
        public Builder specialDisplay() {
            this.type = SkillType.SPECIAL; this.equippable = false; this.toggleable = false;
            this.alwaysOn = false; this.hudVisible = true;
            return this;
        }

        public SkillNode build() {
            if (nameKey.isEmpty()) nameKey = "skill." + id.getNamespace() + "." + id.getPath();
            if (descKey.isEmpty()) descKey = nameKey + ".desc";
            return new SkillNode(this);
        }
    }
}
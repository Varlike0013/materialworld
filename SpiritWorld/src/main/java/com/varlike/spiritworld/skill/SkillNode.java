package com.varlike.spiritworld.skill;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
            this.alwaysOn = false; this.hudVisible = true;
            return this;
        }
        public Builder active() {
            this.type = SkillType.ACTIVE; this.equippable = true; this.toggleable = false;
            this.alwaysOn = false; this.hudVisible = true;
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
package com.varlike.spiritworld.skill;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SkillNode {

    private final ResourceLocation id;
    private final SkillType type;
    private final int gridX;
    private final int gridY;
    private final int maxLevel;
    private final long costPerLevel;
    private final int requiredVisionLevel;
    private final List<ResourceLocation> prerequisites;
    private final String icon;
    private final String nameKey;
    private final String descKey;

    // ===== 行为标记 =====
    private final boolean equippable;
    private final boolean toggleable;
    private final boolean alwaysOn;

    private SkillNode(Builder b) {
        this.id = b.id;
        this.type = b.type;
        this.gridX = b.gridX;
        this.gridY = b.gridY;
        this.maxLevel = b.maxLevel;
        this.costPerLevel = b.costPerLevel;
        this.requiredVisionLevel = b.requiredVisionLevel;
        this.prerequisites = Collections.unmodifiableList(new ArrayList<>(b.prerequisites));
        this.icon = b.icon;
        this.nameKey = b.nameKey;
        this.descKey = b.descKey;
        this.equippable = b.equippable;
        this.toggleable = b.toggleable;
        this.alwaysOn = b.alwaysOn;
    }

    // getters
    public ResourceLocation getId() { return id; }
    public SkillType getType() { return type; }
    public int getGridX() { return gridX; }
    public int getGridY() { return gridY; }
    public int getMaxLevel() { return maxLevel; }
    public long getCostPerLevel() { return costPerLevel; }
    public int getRequiredVisionLevel() { return requiredVisionLevel; }
    public List<ResourceLocation> getPrerequisites() { return prerequisites; }
    public String getIcon() { return icon; }
    public String getNameKey() { return nameKey; }
    public String getDescKey() { return descKey; }
    public boolean isEquippable() { return equippable; }
    public boolean isToggleable() { return toggleable; }
    public boolean isAlwaysOn() { return alwaysOn; }

    public static Builder builder(ResourceLocation id) {
        return new Builder(id);
    }

    public static class Builder {
        private final ResourceLocation id;
        private SkillType type = SkillType.PASSIVE;
        private int gridX = 0;
        private int gridY = 0;
        private int maxLevel = 1;
        private long costPerLevel = 1;
        private int requiredVisionLevel = 0;
        private final List<ResourceLocation> prerequisites = new ArrayList<>();
        private String icon = "★";
        private String nameKey = "";
        private String descKey = "";

        private boolean equippable = false;
        private boolean toggleable = false;
        private boolean alwaysOn = false;

        private Builder(ResourceLocation id) { this.id = id; }

        public Builder grid(int x, int y) { this.gridX = x; this.gridY = y; return this; }
        public Builder maxLevel(int n) { this.maxLevel = n; return this; }
        public Builder cost(long c) { this.costPerLevel = c; return this; }
        public Builder requiredVisionLevel(int v) { this.requiredVisionLevel = v; return this; }
        public Builder prereq(ResourceLocation p) { this.prerequisites.add(p); return this; }
        public Builder icon(String s) { this.icon = s; return this; }
        public Builder name(String key) { this.nameKey = key; return this; }
        public Builder desc(String key) { this.descKey = key; return this; }

        // ===== 行为快捷方法 =====

        /** 常驻被动：一定激活，不可关闭 */
        public Builder passiveAlways() {
            this.type = SkillType.PASSIVE;
            this.equippable = false;
            this.toggleable = false;
            this.alwaysOn = true;
            return this;
        }

        /** 开关被动：只在装配页面开关 */
        public Builder passiveToggle() {
            this.type = SkillType.PASSIVE;
            this.equippable = false;
            this.toggleable = true;
            this.alwaysOn = false;
            return this;
        }

        /** 可装备被动：装配页开关，可放入技能栏，使用键切换 */
        public Builder passiveEquippable() {
            this.type = SkillType.PASSIVE;
            this.equippable = true;
            this.toggleable = true;
            this.alwaysOn = false;
            return this;
        }

        /** 主动技能：装配到槽位，使用键触发 */
        public Builder active() {
            this.type = SkillType.ACTIVE;
            this.equippable = true;
            this.toggleable = false;
            this.alwaysOn = false;
            return this;
        }

        /** 特殊技能（可装备） */
        public Builder specialEquippable() {
            this.type = SkillType.SPECIAL;
            this.equippable = true;
            this.toggleable = false;
            this.alwaysOn = false;
            return this;
        }

        /** 特殊技能（仅展示） */
        public Builder specialDisplay() {
            this.type = SkillType.SPECIAL;
            this.equippable = false;
            this.toggleable = false;
            this.alwaysOn = false;
            return this;
        }

        public SkillNode build() {
            if (nameKey.isEmpty()) nameKey = "skill." + id.getNamespace() + "." + id.getPath();
            if (descKey.isEmpty()) descKey = nameKey + ".desc";
            return new SkillNode(this);
        }
    }
}
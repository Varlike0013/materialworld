package com.varlike.spiritworld.skill;

/** 技能分支：用于技能树中的分类展示与配色。 */
public enum SkillBranch {
    SPIRIT("skill.branch.spirit", 0x4CAF50),
    VISION("skill.branch.vision", 0x42A5F5),
    TOOL("skill.branch.tool", 0xFFFFD54F),
    SPACE("skill.branch.space", 0xBA68C8);

    private final String labelKey;
    private final int color;

    SkillBranch(String labelKey, int color) {
        this.labelKey = labelKey;
        this.color = color;
    }

    public String labelKey() {
        return labelKey;
    }

    public int color() {
        return color;
    }
}

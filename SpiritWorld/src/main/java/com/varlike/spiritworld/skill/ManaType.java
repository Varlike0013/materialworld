package com.varlike.spiritworld.skill;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

/**
 * 魔力属性。环境决定当前聚集哪种属性，默认为 {@link #NONE}。
 */
public enum ManaType {

    NONE  ("none",   0xAAAAAA),
    FIRE  ("fire",   0xFF6633),
    WATER ("water",  0x3399FF),
    WIND  ("wind",   0x88FFCC),
    EARTH ("earth",  0x996633),
    SPIRIT("spirit", 0xCC66FF);

    private final String id;
    private final int color;

    ManaType(String id, int color) {
        this.id = id;
        this.color = color;
    }

    public String id()        { return id; }
    public int color()        { return color; }

    public Component displayName() {
        return Component.translatable("gui.spiritworld.mana_type." + id);
    }
    // 类内追加
    /** 给 Component 应用本属性的颜色 */
    public Component applyStyle(Component c) {
        return c.copy().withStyle(Style.EMPTY.withColor(color));
    }
    /** 按 id 查找，找不到返回 {@link #NONE} */
    public static ManaType byId(String id) {
        for (ManaType t : values()) {
            if (t.id.equals(id)) return t;
        }
        return NONE;
    }
}
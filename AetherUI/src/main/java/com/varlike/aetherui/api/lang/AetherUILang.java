package com.varlike.aetherui.api.lang;

/** 所有 HUD 文案的翻译键，供数据生成与运行时共用。 */
public final class AetherUILang {

    private AetherUILang() {}

    private static String key(String path) {
        return "aetherui." + path;
    }

    // 状态条
    public static final String HP          = key("bar.hp");
    public static final String FOOD        = key("bar.food");
    public static final String LEVEL       = key("bar.level");
    public static final String ARMOR       = key("bar.armor");

    // 瞄准信息
    public static final String ENTITY      = key("target.entity");
    public static final String NON_LIVING  = key("target.non_living");
    public static final String TOOL        = key("target.tool");
    public static final String LEVEL_REQ   = key("target.level_req");
    public static final String CAN_MINE    = key("target.can_mine");
    public static final String CANNOT_MINE = key("target.cannot_mine");

    // 工具类型
    public static final String TOOL_PICKAXE = key("tool.pickaxe");
    public static final String TOOL_SHOVEL  = key("tool.shovel");
    public static final String TOOL_AXE     = key("tool.axe");
    public static final String TOOL_HOE     = key("tool.hoe");
    public static final String TOOL_HAND    = key("tool.hand");

    // 挖掘等级
    public static final String TIER_NONE    = key("tier.none");
    public static final String TIER_STONE   = key("tier.stone");
    public static final String TIER_IRON    = key("tier.iron");
    public static final String TIER_DIAMOND = key("tier.diamond");
}

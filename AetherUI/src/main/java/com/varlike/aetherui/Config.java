package com.varlike.aetherui;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

/** 客户端配置：控制自定义 HUD 的启用与原版状态条的隐藏。 */
public final class Config {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLE_CUSTOM_HUD = BUILDER
            .comment("启用 AetherUI 自定义 HUD（装备与手持物品的图标+耐久，创造和旁观者模式自动隐藏）")
            .define("enableCustomHud", true);

    public static final ModConfigSpec.BooleanValue ENABLE_TARGET_INFO = BUILDER
            .comment("启用右下角瞄准信息（方块/实体名称等关键信息），所有游戏模式均可显示")
            .define("enableTargetInfo", true);

    public static final ModConfigSpec.BooleanValue SHOW_ARMOR_DURABILITY = BUILDER
            .comment("显示 4 件盔甲（头盔/胸甲/护腿/靴子）的图标+耐久")
            .define("showArmorDurability", true);

    public static final ModConfigSpec.BooleanValue SHOW_HELD_DURABILITY = BUILDER
            .comment("显示主手/副手物品的图标+耐久")
            .define("showHeldDurability", true);

    public static final ModConfigSpec.BooleanValue ENABLE_WORLD_HEALTH_BAR = BUILDER
            .comment("启用 3D 世界空间血条（怪物与动物头顶显示，始终面向玩家）")
            .define("enableWorldHealthBar", true);

    public static final ModConfigSpec.BooleanValue SHOW_NEUTRAL_HEALTH_BAR = BUILDER
            .comment("是否显示中立生物的 3D 血条（敌对怪物与 passive 动物不受此项影响）")
            .define("showNeutralHealthBar", true);

    public static final ModConfigSpec.BooleanValue HIDE_VANILLA_BARS = BUILDER
            .comment("隐藏原版状态条（玩家血量、护甲、饥饿、空气、经验、坐骑血量、跳跃条）。物品栏不受影响。")
            .define("hideVanillaBars", true);

    public static final ModConfigSpec.BooleanValue SHOW_DEMO = BUILDER
            .comment("在屏幕左上角显示 AetherUI 演示面板")
            .define("showDemo", false);

    private static final ModConfigSpec SPEC = BUILDER.build();

    private Config() {}

    public static void register(ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, SPEC);
    }

    public static boolean customHudEnabled() {
        return ENABLE_CUSTOM_HUD.get();
    }

    public static boolean targetInfoEnabled() {
        return ENABLE_TARGET_INFO.get();
    }

    public static boolean showArmorDurability() {
        return SHOW_ARMOR_DURABILITY.get();
    }

    public static boolean showHeldDurability() {
        return SHOW_HELD_DURABILITY.get();
    }

    public static boolean worldHealthBarEnabled() {
        return ENABLE_WORLD_HEALTH_BAR.get();
    }

    public static boolean showNeutralHealthBar() {
        return SHOW_NEUTRAL_HEALTH_BAR.get();
    }

    public static boolean hideVanillaBars() {
        return HIDE_VANILLA_BARS.get();
    }

    public static boolean showDemo() {
        return SHOW_DEMO.get();
    }
}

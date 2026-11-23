package com.varlike.aetherui;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

/** 客户端配置：控制自定义 HUD 的启用与原版状态条的隐藏。 */
public final class Config {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    /** 总开关：关闭后 AetherUI 提供的所有辅助（HUD/瞄准/血条/标记/世界UI/隐藏原版条）全部停用。 */
    public static final ModConfigSpec.BooleanValue ENABLE_ALL_ASSISTS = BUILDER
            .comment("总开关：关闭后本 Mod 提供的所有辅助功能全部停用（其它 Mod 通过 API 注册的 UI 不受影响）")
            .define("enableAllAssists", true);

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

    public static final ModConfigSpec.BooleanValue ENABLE_BUFF_HUD = BUILDER
            .comment("启用物品栏上方的 Buff 图标 HUD")
            .define("enableBuffHud", true);

    public static final ModConfigSpec.BooleanValue ENABLE_WORLD_HEALTH_BAR = BUILDER
            .comment("启用 3D 世界空间血条（怪物与动物头顶显示，始终面向玩家）")
            .define("enableWorldHealthBar", true);

    public static final ModConfigSpec.BooleanValue SHOW_NEUTRAL_HEALTH_BAR = BUILDER
            .comment("是否显示中立生物的 3D 血条（敌对怪物与 passive 动物不受此项影响）")
            .define("showNeutralHealthBar", true);

    public static final ModConfigSpec.BooleanValue SHOW_HOSTILE_HEALTH_BAR = BUILDER
            .comment("是否显示敌对怪物的 3D 血条")
            .define("showHostileHealthBar", true);

    public static final ModConfigSpec.BooleanValue SHOW_ANIMAL_HEALTH_BAR = BUILDER
            .comment("是否显示被动动物的 3D 血条")
            .define("showAnimalHealthBar", true);

    public static final ModConfigSpec.BooleanValue ENABLE_WORLD_MARKERS = BUILDER
            .comment("启用其它 Mod 通过 WorldMarkerProvider 注册的 3D 世界标记")
            .define("enableWorldMarkers", true);

    public static final ModConfigSpec.BooleanValue ENABLE_WORLD_UI = BUILDER
            .comment("启用世界空间 UI（WorldRenderTarget / WorldIcon，由各 Mod 注册）")
            .define("enableWorldUI", true);

    public static final ModConfigSpec.BooleanValue SHOW_TARGET_BLOCK_INFO = BUILDER
            .comment("瞄准信息：显示方块扩展信息（容器格子、配方等，由其他 Mod 提供）")
            .define("showTargetBlockInfo", true);

    public static final ModConfigSpec.BooleanValue HIDE_VANILLA_BARS = BUILDER
            .comment("隐藏原版状态条（玩家血量、护甲、饥饿、空气、经验、坐骑血量、跳跃条）。物品栏不受影响。")
            .define("hideVanillaBars", true);

    public static final ModConfigSpec.BooleanValue SHOW_DEMO = BUILDER
            .comment("在屏幕左上角显示 AetherUI 演示面板（含 Boss 血条双端绘制演示）")
            .define("showDemo", false);

    public static final ModConfigSpec.BooleanValue SHOW_SMELTING_ICON_DEMO = BUILDER
            .comment("显示方块上方的熔炼产物图标演示（浮动+旋转，在玩家附近）")
            .define("showSmeltingIconDemo", false);

    public static final ModConfigSpec.BooleanValue SHOW_BOSS_BAR_DEMO = BUILDER
            .comment("显示 Boss 血条双端绘制演示（屏幕 HUD + 世界空间）")
            .define("showBossBarDemo", false);

    private static final ModConfigSpec SPEC = BUILDER.build();

    private Config() {}

    public static void register(ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, SPEC);
    }

    /** 总开关是否开启。所有辅助 getter 都先过这一道。 */
    public static boolean allAssistsEnabled() {
        return ENABLE_ALL_ASSISTS.get();
    }

    public static boolean customHudEnabled() {
        return allAssistsEnabled() && ENABLE_CUSTOM_HUD.get();
    }

    public static boolean targetInfoEnabled() {
        return allAssistsEnabled() && ENABLE_TARGET_INFO.get();
    }

    public static boolean showArmorDurability() {
        return customHudEnabled() && SHOW_ARMOR_DURABILITY.get();
    }

    public static boolean showHeldDurability() {
        return customHudEnabled() && SHOW_HELD_DURABILITY.get();
    }

    public static boolean buffHudEnabled() {
        return allAssistsEnabled() && ENABLE_BUFF_HUD.get();
    }

    public static boolean worldHealthBarEnabled() {
        return allAssistsEnabled() && ENABLE_WORLD_HEALTH_BAR.get();
    }

    public static boolean showNeutralHealthBar() {
        return worldHealthBarEnabled() && SHOW_NEUTRAL_HEALTH_BAR.get();
    }

    public static boolean showHostileHealthBar() {
        return worldHealthBarEnabled() && SHOW_HOSTILE_HEALTH_BAR.get();
    }

    public static boolean showAnimalHealthBar() {
        return worldHealthBarEnabled() && SHOW_ANIMAL_HEALTH_BAR.get();
    }

    public static boolean worldMarkersEnabled() {
        return allAssistsEnabled() && ENABLE_WORLD_MARKERS.get();
    }

    public static boolean worldUIEnabled() {
        return allAssistsEnabled() && ENABLE_WORLD_UI.get();
    }

    public static boolean showTargetBlockInfo() {
        return targetInfoEnabled() && SHOW_TARGET_BLOCK_INFO.get();
    }

    public static boolean hideVanillaBars() {
        return allAssistsEnabled() && HIDE_VANILLA_BARS.get();
    }

    public static boolean showDemo() {
        return allAssistsEnabled() && SHOW_DEMO.get();
    }

    public static boolean showSmeltingIconDemo() {
        return allAssistsEnabled() && SHOW_SMELTING_ICON_DEMO.get();
    }

    public static boolean showBossBarDemo() {
        return showDemo() && SHOW_BOSS_BAR_DEMO.get();
    }
}

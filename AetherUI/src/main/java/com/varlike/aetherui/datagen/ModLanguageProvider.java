package com.varlike.aetherui.datagen;

import com.varlike.aetherui.AetherUIMod;
import com.varlike.aetherui.api.lang.AetherUILang;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

/** 双语提供器：en_us 与 zh_cn 的文案在同一个 translate() 中定义。 */
public class ModLanguageProvider extends LanguageProvider {

    private final boolean isChinese;

    public ModLanguageProvider(PackOutput output, String locale) {
        super(output, AetherUIMod.MOD_ID, locale);
        this.isChinese = locale.equals("zh_cn");
    }

    @Override
    protected void addTranslations() {
        defineTranslations();
    }

    private void translate(String key, String en, String zh) {
        add(key, isChinese ? zh : en);
    }

    /** 同时注册配置项的显示名与 tooltip。 */
    private void config(String path, String enName, String zhName, String enTip, String zhTip) {
        String prefix = "aetherui.configuration." + path;
        translate(prefix, enName, zhName);
        translate(prefix + ".tooltip", enTip, zhTip);
    }

    private void defineTranslations() {
        // 配置界面：NeoForge 使用 <modid>.configuration.<path> 与 .tooltip
        config("enableAllAssists", "Enable All Assists", "启用所有辅助",
                "Master switch: disable every AetherUI assist (HUD/target info/health bars/markers/world UI). Mods using the API are unaffected.",
                "总开关：关闭后 AetherUI 自带的所有辅助（HUD/瞄准/血条/标记/世界UI）全部停用，其它 Mod 通过 API 注册的内容不受影响");
        config("enableCustomHud", "Enable Custom HUD", "启用自定义 HUD",
                "Custom HUD with armor/held item durability. Hidden in creative/spectator.",
                "自定义 HUD：显示盔甲与手持物品耐久，创造/旁观模式自动隐藏");
        config("enableTargetInfo", "Enable Target Info", "启用瞄准信息",
                "Show block/entity info at bottom-right in all game modes.",
                "在右下角显示方块/实体信息，所有游戏模式均可显示");
        config("showArmorDurability", "Show Armor Durability", "显示盔甲耐久",
                "Show 4 armor pieces icon and durability.",
                "显示 4 件盔甲（头盔/胸甲/护腿/靴子）的图标与耐久");
        config("showHeldDurability", "Show Held Durability", "显示手持耐久",
                "Show main/off hand item icon and durability.",
                "显示主手/副手物品的图标与耐久");
        config("enableBuffHud", "Enable Buff HUD", "启用 Buff HUD",
                "Show buff icons above the hotbar.",
                "在物品栏上方显示 Buff 图标 HUD");
        config("enableWorldHealthBar", "Enable World Health Bar", "启用 3D 血条",
                "Show billboard health bars above mobs and animals.",
                "在怪物与动物头顶显示始终面向玩家的 3D 血条");
        config("showNeutralHealthBar", "Show Neutral Health Bar", "显示中立生物血条",
                "Show health bars for neutral mobs.",
                "显示中立生物的 3D 血条");
        config("showHostileHealthBar", "Show Hostile Health Bar", "显示敌对生物血条",
                "Show health bars for hostile mobs.",
                "显示敌对怪物的 3D 血条");
        config("showAnimalHealthBar", "Show Animal Health Bar", "显示动物血条",
                "Show health bars for passive animals.",
                "显示被动动物的 3D 血条");
        config("enableWorldMarkers", "Enable World Markers", "启用世界标记",
                "Enable 3D world markers registered via WorldMarkerProvider.",
                "启用其它 Mod 通过 WorldMarkerProvider 注册的 3D 世界标记");
        config("enableWorldUI", "Enable World UI", "启用世界空间 UI",
                "Enable WorldRenderTarget / WorldIcon registered by mods.",
                "启用各 Mod 注册的世界空间 UI（WorldRenderTarget / WorldIcon）");
        config("showTargetBlockInfo", "Show Target Block Info", "显示方块扩展信息",
                "Show extended block info (container slots, recipes) from other mods.",
                "瞄准信息中显示其它 Mod 提供的方块扩展信息（容器格子、配方）");
        config("hideVanillaBars", "Hide Vanilla Bars", "隐藏原版状态条",
                "Hide vanilla health/armor/food/air/experience bars. Hotbar is not affected.",
                "隐藏原版血量/护甲/饥饿/空气/经验条，物品栏不受影响");
        config("showDemo", "Show Demo Panel", "显示演示面板",
                "Show AetherUI demo panel at top-left.",
                "在屏幕左上角显示 AetherUI 演示面板");
        config("showBossBarDemo", "Show Boss Bar Demo", "显示 Boss 血条演示",
                "Show the Boss health bar in both screen HUD and world space.",
                "显示同时绘制在屏幕 HUD 与世界空间的 Boss 血条演示");
        config("showSmeltingIconDemo", "Show Smelting Icon Demo", "显示熔炼图标演示",
                "Show a floating rotating icon above the player (demo of WorldIcon).",
                "在玩家附近显示浮动旋转的世界图标（WorldIcon 演示）");

        // 状态条
        translate(AetherUILang.HP,    "HP",    "生命");
        translate(AetherUILang.FOOD,  "Food",  "饥饿");
        translate(AetherUILang.LEVEL, "Lv",    "等级");
        translate(AetherUILang.ARMOR, "Armor", "护甲");

        // 瞄准信息
        translate(AetherUILang.ENTITY,      "Entity",           "实体");
        translate(AetherUILang.NON_LIVING,  "Non-living entity","非生物实体");
        translate(AetherUILang.TOOL,        "Tool",             "工具");
        translate(AetherUILang.LEVEL_REQ,   "Level",            "等级");
        translate(AetherUILang.CAN_MINE,    "Can mine",         "可以挖掘");
        translate(AetherUILang.CANNOT_MINE, "Cannot mine",      "无法挖掘");

        // 工具类型
        translate(AetherUILang.TOOL_PICKAXE, "Pickaxe", "镐");
        translate(AetherUILang.TOOL_SHOVEL,  "Shovel",  "铲");
        translate(AetherUILang.TOOL_AXE,     "Axe",     "斧");
        translate(AetherUILang.TOOL_HOE,     "Hoe",     "锄");
        translate(AetherUILang.TOOL_HAND,    "Hand",    "徒手");

        // 挖掘等级
        translate(AetherUILang.TIER_NONE,    "None",    "无");
        translate(AetherUILang.TIER_STONE,   "Stone",   "石");
        translate(AetherUILang.TIER_IRON,    "Iron",    "铁");
        translate(AetherUILang.TIER_DIAMOND, "Diamond", "钻石");
    }
}

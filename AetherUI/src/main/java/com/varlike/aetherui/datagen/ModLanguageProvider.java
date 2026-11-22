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
        config("enableWorldHealthBar", "Enable World Health Bar", "启用 3D 血条",
                "Show billboard health bars above mobs and animals.",
                "在怪物与动物头顶显示始终面向玩家的 3D 血条");
        config("showNeutralHealthBar", "Show Neutral Health Bar", "显示中立生物血条",
                "Show health bars for neutral mobs. Hostile and passive are always shown.",
                "显示中立生物的血条，敌对怪物与被动动物不受影响");
        config("hideVanillaBars", "Hide Vanilla Bars", "隐藏原版状态条",
                "Hide vanilla health/armor/food/air/experience bars. Hotbar is not affected.",
                "隐藏原版血量/护甲/饥饿/空气/经验条，物品栏不受影响");
        config("showDemo", "Show Demo Panel", "显示演示面板",
                "Show AetherUI demo panel at top-left.",
                "在屏幕左上角显示 AetherUI 演示面板");

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

package com.varlike.spiritworld.datagen;

import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.data.LanguageProvider;

/** 双语提供器：en_us 与 zh_cn 的文案在同一个 translate() 中定义。 */
public class ModLanguageProvider extends LanguageProvider {

    private final boolean isChinese;

    public ModLanguageProvider(PackOutput output, String locale) {
        super(output, SpiritWorld.MOD_ID, locale);
        this.isChinese = locale.equals("zh_cn");
    }

    @Override
    protected void addTranslations() {
        defineTranslations();
    }

    private void translate(String key, String en, String zh) {
        add(key, isChinese ? zh : en);
    }

    private void item(Item item, String en, String zh) {
        add(item, isChinese ? zh : en);
    }

    private void defineTranslations() {
        item(ModItems.BOTTLED_SPIRIT.get(), "Bottled Spirit", "瓶装精神力");

        translate("message.spiritworld.spirit_restored", "Restored %d spirit", "恢复 %d 点精神力");
        translate("message.spiritworld.spirit_already_full", "Spirit is already full", "精神力已满");

        translate("materialworld.addon_group.spirit_world", "Spirit World", "精神世界");

        translate("spiritworld.configuration.title", "SpiritWorld Configs", "精神世界配置");
        translate("spiritworld.configuration.section.spiritworld.common.toml", "SpiritWorld Common Configs", "精神世界通用配置");
        translate("spiritworld.configuration.section.spiritworld.common.toml.title", "SpiritWorld Common Configs", "精神世界通用配置");
        translate("spiritworld.configuration.section.spiritworld.client.toml", "SpiritWorld Client Configs", "精神世界客户端配置");
        translate("spiritworld.configuration.section.spiritworld.client.toml.title", "SpiritWorld Client Configs", "精神世界客户端配置");

        translate("spiritworld.configuration.spiritRegenAmount", "Spirit Regen Amount", "精神力恢复量");
        translate("spiritworld.configuration.spiritRegenInterval", "Spirit Regen Interval (ticks)", "精神力恢复间隔（tick）");
        translate("spiritworld.configuration.defaultMaxSpirit", "Default Max Spirit", "默认精神力上限");
        translate("spiritworld.configuration.syncInterval", "Sync Interval (ticks)", "同步间隔（tick）");
        translate("spiritworld.configuration.bottledSpiritRestoreAmount", "Bottled Spirit Restore Amount", "瓶装精神力恢复量");
        translate("spiritworld.configuration.spiritClearOnLogin", "Clear Spirit on Login", "登录时清空精神力");
        translate("spiritworld.configuration.spiritClearOnDeath", "Clear Spirit on Death", "死亡时清空精神力");

        translate("spiritworld.configuration.showSpiritBar", "Show Spirit Bar", "显示精神力条");
        translate("spiritworld.configuration.showSpiritText", "Show Spirit Value", "显示精神力数值");
        translate("spiritworld.configuration.scanIconMinScale", "Scan Icon Min Size", "扫描图标最小尺寸");
        translate("spiritworld.configuration.scanIconMaxScale", "Scan Icon Max Size", "扫描图标最大尺寸");

        // 物品
        item(ModItems.SPIRIT_IRON_RING.get(), "Spirit Iron Ring", "精神铁秘戒");
        item(ModItems.SPIRIT_GOLD_RING.get(), "Spirit Gold Ring", "精神金秘戒");
        item(ModItems.SPIRIT_DIAMOND_RING.get(), "Spirit Diamond Ring", "精神钻秘戒");

        // 提示
        translate("tooltip.spiritworld.ring.stored", "Stored: %d / %d", "已存：%d / %d");
        translate("tooltip.spiritworld.ring.usage",
                "Right-click: store spirit. Shift+Right-click: extract. Right-click water cauldron: convert.",
                "右键：存入精神力。Shift+右键：取出精神力。右键含水炼药锅：转化。");

        // 消息
        translate("message.spiritworld.ring.full", "Ring is full", "戒指已满");
        translate("message.spiritworld.ring.empty", "Ring is empty", "戒指已空");
        translate("message.spiritworld.ring.no_spirit", "No spirit to store", "没有可存入的精神力");
        translate("message.spiritworld.ring.player_full", "Your spirit is full", "你的精神力已满");
        translate("message.spiritworld.ring.store", "Stored %d spirit", "存入 %d 点精神力");
        translate("message.spiritworld.ring.extract", "Extracted %d spirit", "取出 %d 点精神力");
        translate("message.spiritworld.ring.not_enough", "Not enough spirit, need %d", "精神力不足，需要 %d");
        translate("message.spiritworld.ring.converted", "Water converted to spirit potion", "水已转化为精神药水");

        // ===== 按键 =====
        translate("key.spiritworld.open_skill_tree", "Open Skill Tree", "打开技能树");
        translate("key.spiritworld.open_loadout", "Open Skill Loadout", "打开技能装配");
        translate("key.spiritworld.open_radial", "Open Skill Radial", "打开技能选盘");
        translate("key.spiritworld.use_skill", "Use Skill", "使用技能");

        // ===== 技能树界面 =====
        translate("screen.spiritworld.skill_tree.title", "Spirit Skill Tree", "精神技能树");
        translate("screen.spiritworld.skill_tree.level", "Vision Level: %d", "灵视等级：%d");
        translate("screen.spiritworld.skill_tree.points", "Vision Points: %d", "灵视点：%d");
        translate("screen.spiritworld.skill_tree.progress", "%d / %d", "%d / %d");

        // ===== 技能类型 =====
        translate("skill.type.active", "Active Skill", "主动技能");
        translate("skill.type.passive", "Passive Skill", "被动技能");
        translate("skill.type.special", "Special Skill", "特殊技能");

        // ===== 技能提示 =====
        translate("skill.tooltip.level", "Level: %d / %d", "等级：%d / %d");
        translate("skill.tooltip.cost", "Next Level Cost: %d", "下一级消耗：%d");
        translate("skill.tooltip.required_vision", "Required Vision Level: %d", "需要灵视等级：%d");
        translate("skill.tooltip.prerequisites", "Prerequisites:", "前置条件：");

        // ===== 内置示例技能 =====
        translate("skill.spiritworld.spirit_sense", "Spirit Sense", "精神感知");
        translate("skill.spiritworld.spirit_sense.desc", "Increases spirit regeneration.", "提升精神力恢复速度。");
        translate("skill.spiritworld.spirit_burst", "Spirit Burst", "精神爆发");
        translate("skill.spiritworld.spirit_burst.desc", "Active skill to burst spirit.", "主动技能，爆发精神力。");
        translate("skill.spiritworld.spirit_focus", "Spirit Focus", "精神专注");
        translate("skill.spiritworld.spirit_focus.desc", "Increases max spirit.", "提升精神力上限。");
        translate("skill.spiritworld.spirit_vision", "Spirit Vision", "精神视界");
        translate("skill.spiritworld.spirit_vision.desc", "Grants spirit vision.", "赋予灵视能力。");
        translate("skill.spiritworld.spirit_storm", "Spirit Storm", "精神风暴");
        translate("skill.spiritworld.spirit_storm.desc", "Devastating area attack.", "毁灭性的范围攻击。");
        translate("skill.spiritworld.spirit_echo", "Spirit Echo", "精神回响");
        translate("skill.spiritworld.spirit_echo.desc", "Echoes spirit energy.", "回荡精神能量。");

        translate("screen.spiritworld.loadout.title", "Skill Loadout", "技能装配");
        translate("screen.spiritworld.loadout.hint",
                "Left-click skill list, then click a slot. Right-click slot to clear.",
                "左键点击技能列表，然后点击槽位。右键槽位清空。");
        translate("screen.spiritworld.loadout.empty", "(No skills)", "（无技能）");
        translate("screen.spiritworld.loadout.passives", "Passive Skills", "被动技能");
        translate("screen.spiritworld.loadout.passive.on", "ON", "开");
        translate("screen.spiritworld.loadout.passive.off", "OFF", "关");
        translate("screen.spiritworld.loadout.passive.always", "ALWAYS", "常驻");
        translate("screen.spiritworld.loadout.filter.all", "All", "全部");
        translate("screen.spiritworld.loadout.filter.equippable", "Equippable", "可装备");
        translate("screen.spiritworld.loadout.switch_to_tree", "Skill Tree", "技能树");
        translate("screen.spiritworld.skill_tree.switch_to_loadout", "Skill Loadout", "技能装配");

        translate("skill.spiritworld.emc_conversion", "EMC Conversion", "EMC 转化");
        translate("skill.spiritworld.emc_conversion.desc",
                "Continuously converts EMC into Vision Points.",
                "持续将 EMC 转化为灵视点。");

        translate("message.spiritworld.skill.emc_conversion.no_emc",
                "Not enough EMC, EMC Conversion stopped",
                "EMC 不足，EMC 转化已停止");
        item(ModItems.SPIRIT_TOOL.get(), "Spirit Tool", "精神工具");
        translate("tooltip.spiritworld.spirit_tool.pickaxe", "Pickaxe: %s", "镐：%s");
        translate("tooltip.spiritworld.spirit_tool.axe", "Axe: %s", "斧：%s");
        translate("tooltip.spiritworld.spirit_tool.shovel", "Shovel: %s", "铲：%s");
        translate("tooltip.spiritworld.spirit_tool.hoe", "Hoe: %s", "锄：%s");
        translate("tooltip.spiritworld.spirit_tool.fortune", "Fortune %d", "时运 %d");
        translate("tooltip.spiritworld.spirit_tool.bound", "Bound to spirit, cannot be dropped", "绑定精神，无法丢弃");
        translate("tooltip.spiritworld.spirit_tool.level.empty", "Bare Hand", "空手");
        translate("tooltip.spiritworld.spirit_tool.level.wood", "Wood", "木");
        translate("tooltip.spiritworld.spirit_tool.level.stone", "Stone", "石");
        translate("tooltip.spiritworld.spirit_tool.level.iron", "Iron", "铁");
        translate("tooltip.spiritworld.spirit_tool.level.diamond", "Diamond", "钻石");
        translate("tooltip.spiritworld.spirit_tool.level.netherite", "Netherite", "下界合金");

        translate("message.spiritworld.spirit_tool.destroyed", "Spirit Tool dissolved", "精神工具已消散");
        translate("message.spiritworld.spirit_tool.already_have", "You already have a Spirit Tool", "你已拥有精神工具");
        translate("message.spiritworld.spirit_tool.no_spirit", "Not enough spirit (need 10)", "精神力不足（需要 10）");
        translate("message.spiritworld.spirit_tool.no_space", "No empty inventory slot", "背包没有空位");
        translate("message.spiritworld.spirit_tool.summoned", "Spirit Tool summoned", "精神工具已召唤");
        translate("message.spiritworld.spirit_tool.no_spirit_mining",
                "Not enough spirit to mine (%d needed)",
                "精神力不足，无法挖掘（需要 %d）");

        // 技能学习失败提示
        translate("message.spiritworld.skill.max_level", "This skill is already at max level", "该技能已满级");
        translate("message.spiritworld.skill.prereq", "Requires %s first", "需要先学习：%s");
        translate("message.spiritworld.skill.prereq_unknown", "Prerequisite skill not learned", "前置技能尚未学习");
        translate("message.spiritworld.skill.no_vision", "Requires Vision Level %d", "需要灵视等级 %d");
        translate("message.spiritworld.skill.no_points", "Not enough Vision Points (need %d)", "灵视点不足（需要 %d）");

        translate("skill.spiritworld.summon_spirit_tool", "Summon Spirit Tool", "召唤精神工具");
        translate("skill.spiritworld.summon_spirit_tool.desc",
                "Summons a spirit tool bound to you. Costs 10 spirit.",
                "召唤一把与你绑定的精神工具，消耗 10 点精神力。");
        translate("skill.spiritworld.spirit_tool_pickaxe", "Spirit Pickaxe", "精神镐");
        translate("skill.spiritworld.spirit_tool_pickaxe.desc",
                "Spirit tool mines pickaxe blocks; faster per level.",
                "精神工具可挖掘镐类方块，等级越高越快。");
        translate("skill.spiritworld.spirit_tool_axe", "Spirit Axe", "精神斧");
        translate("skill.spiritworld.spirit_tool_axe.desc",
                "Spirit tool chops axe blocks; faster per level.",
                "精神工具可砍伐斧类方块，等级越高越快。");
        translate("skill.spiritworld.spirit_tool_shovel", "Spirit Shovel", "精神铲");
        translate("skill.spiritworld.spirit_tool_shovel.desc",
                "Spirit tool digs shovel blocks; faster per level.",
                "精神工具可挖掘铲类方块，等级越高越快。");
        translate("skill.spiritworld.spirit_tool_hoe", "Spirit Hoe", "精神锄");
        translate("skill.spiritworld.spirit_tool_hoe.desc",
                "Spirit tool tills hoe blocks; faster per level.",
                "精神工具可耕作锄类方块，等级越高越快。");
        translate("skill.spiritworld.spirit_fortune", "Spirit Fortune", "精神时运");
        translate("skill.spiritworld.spirit_fortune.desc",
                "Spirit tool gains Fortune while this passive is active.",
                "激活后为精神工具附加时运附魔。");

        translate("skill.spiritworld.spirit_endurance", "Spirit Endurance", "精神耐力");
        translate("skill.spiritworld.spirit_endurance.desc", "+10 max spirit per level.", "每级 +10 精神力上限。");
        translate("skill.spiritworld.spirit_toughness", "Spirit Toughness", "精神韧性");
        translate("skill.spiritworld.spirit_toughness.desc", "+0.5 spirit regen per level.", "每级 +0.5 精神力恢复。");
        translate("skill.spiritworld.spirit_strength", "Spirit Strength", "精神强健");
        translate("skill.spiritworld.spirit_strength.desc", "+5% max spirit per level.", "每级 +5% 精神力上限。");
        translate("skill.spiritworld.spirit_surge", "Spirit Surge", "精神涌动");
        translate("skill.spiritworld.spirit_surge.desc", "+5% spirit regen per level.", "每级 +5% 精神力恢复。");
        translate("skill.spiritworld.spirit_flow", "Spirit Flow", "精神洪流");
        translate("skill.spiritworld.spirit_flow.desc",
                "Regenerate 0.25% max spirit per second per level.",
                "每级每秒恢复 0.25% 精神力上限。");
        translate("skill.spiritworld.spirit_source", "Spirit Source", "精神源泉");
        translate("skill.spiritworld.spirit_source.desc", "+50% max spirit, +50% regen.", "+50% 精神力上限，+50% 恢复。");

        translate("skill.spiritworld.vision_sharpness", "Vision Sharpness", "灵视敏锐");
        translate("skill.spiritworld.vision_sharpness.desc",
                "+2 vision fragments per mob kill per level.",
                "每级每次击杀怪物 +2 灵视碎片。");
        translate("skill.spiritworld.emc_affinity", "EMC Affinity", "EMC 亲和");
        translate("skill.spiritworld.emc_affinity.desc", "-3% EMC Conversion cost per level.", "每级 -3% EMC 转化消耗。");
        translate("skill.spiritworld.vision_mastery", "Vision Mastery", "灵视精通");
        translate("skill.spiritworld.vision_mastery.desc", "+15% vision fragment gain per level.", "每级 +15% 灵视碎片获取。");
        translate("skill.spiritworld.spirit_efficiency", "Spirit Efficiency", "精神效率");
        translate("skill.spiritworld.spirit_efficiency.desc", "Grants Efficiency I-V to the Spirit Tool.", "为精神工具附加效率 I-V 附魔。");
        // ===== 精神护盾 =====
        translate("skill.spiritworld.spirit_shield", "Spirit Shield", "精神护盾");
        translate("skill.spiritworld.spirit_shield.desc",
                "Per level,incoming damage is absorbed by spirit. Requires spirit to activate.",
                "每级将受到伤害由精神力承担，需要精神力才能激活。");
        translate("skill.tooltip.spirit_shield.ratio", "Absorb: %s%% of damage", "吸收：伤害的 %s%%");
        translate("skill.tooltip.spirit_shield.cost", "Spirit cost: %s per damage", "精神力消耗：每点伤害 %s");
        translate("skill.tooltip.spirit_shield.example", "100 damage costs %d spirit", "100 点伤害消耗 %d 精神力");
        translate("message.spiritworld.spirit_shield.insufficient",
                "Spirit depleted, shield failed",
                "精神力耗尽，护盾失效");
        // ===== 精神夜视 =====
        translate("skill.spiritworld.spirit_night_vision", "Spirit Night Vision", "精神夜视");
        translate("skill.spiritworld.spirit_night_vision.desc",
                "Grants night vision while active. Reduces max spirit; higher levels reduce less.",
                "激活时获得夜视，同时降低精神力上限；等级越高惩罚越小。");
        translate("skill.tooltip.spirit_night_vision.penalty", "Max spirit: -%s%% while active", "激活时精神力上限：-%s%%");
        translate("skill.tooltip.spirit_night_vision.penalty.none", "No max spirit penalty", "无精神力上限惩罚");
        // ===== 工具分支 =====
        translate("skill.tooltip.tool_speed.current", "Mining speed: %s", "挖掘速度：%s");
        translate("skill.tooltip.fortune.current", "Fortune %s", "时运 %s");
        translate("skill.tooltip.efficiency.current", "Efficiency %s", "效率 %s");
        // ===== 精神力系 =====
        translate("skill.tooltip.spirit_endurance.current", "Current: +%s max spirit", "当前：+%s 精神力上限");
        translate("skill.tooltip.spirit_toughness.current", "Current: +%s spirit regen", "当前：+%s 精神力恢复");
        translate("skill.tooltip.spirit_strength.current", "Current: +%s%% max spirit", "当前：+%s%% 精神力上限");
        translate("skill.tooltip.spirit_surge.current", "Current: +%s%% spirit regen", "当前：+%s%% 精神力恢复");
        translate("skill.tooltip.spirit_flow.current", "Current: +%s%% max spirit per second", "当前：每秒 +%s%% 精神力上限");
        // ===== 灵视系 =====
        translate("skill.tooltip.vision_sharpness.current", "Current: +%s fragments per kill", "当前：每次击杀 +%s 灵视碎片");
        translate("skill.tooltip.emc_affinity.current", "Current: -%s%% EMC conversion cost", "当前：-%s%% EMC 转化消耗");
        translate("skill.tooltip.vision_mastery.current", "Current: +%s%% vision fragments", "当前：+%s%% 灵视碎片");
        // ===== EMC 转化 =====
        translate("skill.tooltip.emc_conversion.rate", "Current: %s EMC per vision fragment", "当前：每 %s EMC 转化 1 灵视碎片");
        translate("skill.tooltip.emc_conversion.note", "Must be equipped to take effect", "需装配到技能栏才能生效");
        // ===== 精神源泉 =====
        translate("skill.tooltip.spirit_source.current",
                "Bonus: +%s%% max spirit, +%s%% regen",
                "加成：+%s%% 精神力上限，+%s%% 恢复");
        translate("message.spiritworld.skill.cooldown", "Skill on cooldown: %ss", "技能冷却中：%s 秒");
        // ===== 精神修复 =====
        translate("skill.spiritworld.spirit_repair", "Spirit Repair", "精神修复");
        translate("skill.spiritworld.spirit_repair.desc",
                "Consumes spirit to repair the held weapon/tool.",
                "消耗精神力，修复主手武器/工具的耐久。");
        translate("skill.tooltip.spirit_repair.rate", "Restore: %s durability per spirit", "恢复：每点精神力 %s 耐久");
        translate("message.spiritworld.spirit_repair.not_damageable", "Held item cannot be repaired", "主手物品无法修复");
        translate("message.spiritworld.spirit_repair.full", "Held item is already at full durability", "主手物品耐久已满");
        translate("message.spiritworld.spirit_repair.no_spirit", "Not enough spirit to repair", "精神力不足，无法修复");
        translate("message.spiritworld.spirit_repair.success",
                "Repaired %d durability (consumed %d spirit)",
                "修复 %d 点耐久（消耗 %d 点精神力）");
        translate("skill.tooltip.spirit_repair.next", "Next level: %s durability per spirit", "下一级：每点精神力 %s 耐久");
        translate("skill.spiritworld.cooldown_reduction", "Spirit Alacrity", "精神迅捷");
        translate("skill.spiritworld.cooldown_reduction.desc",
                "Reduces all skill cooldowns. Higher levels have diminishing returns.",
                "减少所有技能的冷却时间，等级越高收益递减。");
        translate("skill.tooltip.cooldown_reduction.current", "Current: -%s%% cooldown", "当前：-%s%% 冷却时间");
        translate("skill.tooltip.cooldown_reduction.example",
                "Toggle: %ss  |  Normal: %ss",
                "开关技能：%s 秒  |  普通技能：%s 秒");
        translate("skill.spiritworld.spirit_satiety", "Spirit Satiety", "精神饱食");
        translate("skill.spiritworld.spirit_satiety.desc",
                "While active, keeps your hunger bar full. Reduces max spirit; higher levels reduce less.",
                "激活时饱食度始终保持满值，同时降低精神力上限；等级越高惩罚越小。");
        translate("skill.tooltip.spirit_satiety.penalty", "Max spirit: -%s%% while active", "激活时精神力上限：-%s%%");
        translate("skill.tooltip.spirit_satiety.penalty.none", "No max spirit penalty", "无精神力上限惩罚");
        translate("skill.tooltip.spirit_satiety.note", "Also enables natural health regeneration", "同时获得自然回血效果");
        translate("skill.spiritworld.spirit_maintenance", "Spirit Maintenance", "精神维护");
        translate("skill.spiritworld.spirit_maintenance.desc",
                "Every second, restores durability to one damaged item in your inventory. Higher levels restore more.",
                "每秒为背包中任一损坏的物品恢复耐久，等级越高恢复越多。");
        translate("skill.tooltip.spirit_maintenance.rate", "Restores %d durability per second", "每秒恢复 %d 点耐久");
        translate("skill.tooltip.spirit_maintenance.next", "Next level: %d durability per second", "下一级：每秒恢复 %d 点耐久");
        translate("skill.spiritworld.spirit_flight", "Spirit Flight", "精神飞行");
        translate("skill.spiritworld.spirit_flight.desc",
                "While active, grants creative flight. Consumes spirit per second; rising costs double.",
                "激活时获得创造飞行，每秒消耗精神力；上升时消耗翻倍。");
        translate("skill.tooltip.spirit_flight.hover", "Hover/glide: %s spirit/sec", "悬停/平飞：每秒 %s 精神力");
        translate("skill.tooltip.spirit_flight.rising", "Rising: %s spirit/sec", "上升：每秒 %s 精神力");
        translate("skill.tooltip.spirit_flight.next", "Next level: %s spirit/sec hover", "下一级：悬停每秒 %s 精神力");
        translate("message.spiritworld.spirit_flight.low_spirit",
                "Spirit low, flight disabled. Slow falling engaged.",
                "精神力不足，飞行已关闭，进入缓降");
        // ===== 精神扫描 =====
        translate("skill.spiritworld.spirit_scan", "Spirit Scan", "精神扫描");
        translate("skill.spiritworld.spirit_scan.desc",
                "Scan nearby ores. Right-click the skill node to configure range and target ores.",
                "扫描附近的矿物。右键技能节点打开配置界面，设置范围和目标矿物。");
        translate("skill.tooltip.spirit_scan.max_range", "Max range: %d blocks", "最大范围：%d 格");
        translate("skill.tooltip.spirit_scan.duration", "Icons last %d sec", "图标持续 %d 秒");
        translate("skill.tooltip.spirit_scan.next", "Next level: max range %d", "下一级：最大范围 %d 格");
        translate("skill.tooltip.spirit_scan.no_ore_selected",
                "No ore selected, open scan config",
                "未选择矿物，请打开扫描配置");

        // ===== 扫描矿物名（配置界面 + 图标显示用）=====
        translate("spiritworld.scan.coal", "Coal", "煤炭");
        translate("spiritworld.scan.iron", "Iron", "铁矿");
        translate("spiritworld.scan.gold", "Gold", "金矿");
        translate("spiritworld.scan.redstone", "Redstone", "红石");
        translate("spiritworld.scan.lapis", "Lapis", "青金石");
        translate("spiritworld.scan.diamond", "Diamond", "钻石");
        translate("spiritworld.scan.emerald", "Emerald", "绿宝石");
        translate("spiritworld.scan.ancient_debris", "Ancient Debris", "古代残骸");
        translate("spiritworld.scan.other", "Other Mod Ores", "其他模组矿物");

        // ===== 扫描结果提示 =====
        translate("message.spiritworld.spirit_scan.no_spirit", "Not enough spirit (need %d)", "精神力不足（需要 %d）");
        translate("message.spiritworld.spirit_scan.no_ore", "No ore selected, open scan config", "未选择矿物，请打开扫描配置");
        translate("message.spiritworld.spirit_scan.not_found", "No target ores found", "未发现目标矿物");
        translate("message.spiritworld.spirit_scan.found", "Found %d ore cluster(s)", "发现 %d 处矿物");
        // ===== 扫描配置界面 =====
        translate("screen.spiritworld.scan_config.title", "Spirit Scan Config", "精神扫描配置");
        translate("screen.spiritworld.scan_config.range", "Range: %d blocks", "范围：%d 格");
        translate("screen.spiritworld.scan_config.cost", "Spirit cost: %d per scan", "每次扫描消耗：%d 精神力");
        translate("screen.spiritworld.loadout.scan_config", "Scan Config", "扫描配置");
        // ===== 魔力聚集器 =====
        translate("block.spiritworld.spirit_collector_basic", "Spirit Collector (Basic)", "魔力聚集器（基础）");
        translate("block.spiritworld.spirit_collector_advanced", "Spirit Collector (Advanced)", "魔力聚集器（进阶）");
        translate("block.spiritworld.spirit_collector_ultimate", "Spirit Collector (Ultimate)", "魔力聚集器（终极）");
        translate("gui.spiritworld.collector.tier", "Tier %d", "等级 %d");
        translate("gui.spiritworld.collector.amount", "%d / %d", "%d / %d");
        translate("gui.spiritworld.collector.speed", "%d / sec", "%d / 秒");
        translate("gui.spiritworld.collector.percent", "%s%%", "%s%%");
        translate("gui.spiritworld.collector.remaining", "%d s to full", "剩余 %d 秒");
        translate("gui.spiritworld.collector.full", "Full", "已满");
        // ===== 魔力属性 =====
        translate("gui.spiritworld.mana_type.none", "Neutral", "无属性");
        translate("gui.spiritworld.mana_type.fire", "Fire", "火属性");
        translate("gui.spiritworld.mana_type.water", "Water", "水属性");
        translate("gui.spiritworld.mana_type.wind", "Wind", "风属性");
        translate("gui.spiritworld.mana_type.earth", "Earth", "土属性");
        translate("gui.spiritworld.mana_type.spirit", "Spirit", "精神属性");
        // ===== 聚集器信息 =====
        translate("gui.spiritworld.collector.type", "Type: %s", "属性：%s");
        translate("gui.spiritworld.collector.mana_entry", "%s: %d / %d", "%s：%d / %d");
        translate("gui.spiritworld.collector.total", "Total: %d / %d", "总计：%d / %d");
        // ===== 魔力核心 =====
        translate("block.spiritworld.spirit_core", "Spirit Core", "魔力核心");
        translate("block.spiritworld.mana_block", "Mana Block", "魔力方块");
        translate("block.spiritworld.mana_slab", "Mana Slab", "魔力台阶");
        translate("block.spiritworld.mana_stairs", "Mana Stairs", "魔力楼梯");
        translate("item.spiritworld.mana_brick", "Mana Brick", "魔力砖");
        // ===== 核心信息 =====
        translate("gui.spiritworld.spirit_core.capacity", "%d / %d", "%d / %d");
        translate("gui.spiritworld.spirit_core.bonus", "Capacity bonus: +%d", "容量加成：+%d");
        translate("gui.spiritworld.spirit_core.mana_entry", "%s: %d", "%s：%d");
        translate("gui.spiritworld.spirit_core.collectors", "Nearby collectors: %d", "范围聚集器：%d");
        // ===== 核心交互 =====
        translate("message.spiritworld.spirit_core.insufficient",
                "Not enough mana (need %d Spirit + %d Earth)",
                "魔力不足（需要 %d 精神 + %d 土）");
        translate("message.spiritworld.spirit_core.produced", "Produced a Mana Brick", "产出 1 魔力砖");
        translate("block.spiritworld.spirit_forge", "Spirit Forge", "魔力合成台");
        translate("gui.spiritworld.spirit_forge.select", "Select a recipe", "请选择配方");
        translate("gui.spiritworld.spirit_forge.cost", "%s: %d", "%s：%d");
        translate("gui.spiritworld.spirit_forge.time", "Time: %ds", "时间：%d 秒");
        translate("gui.spiritworld.spirit_forge.output", "%s x%d", "%s ×%d");
        translate("gui.spiritworld.spirit_forge.progress", "%ds / %ds", "%d / %d 秒");
        translate("gui.spiritworld.spirit_forge.no_recipe", "No recipe selected", "未选择配方");
        translate("gui.spiritworld.spirit_forge.core", "Core: %s", "魔力核心：%s");
        translate("gui.spiritworld.spirit_forge.core.found", "In range", "范围内");
        translate("gui.spiritworld.spirit_forge.core.missing", "Out of range", "范围外");
        translate("gui.spiritworld.forge.start", "Start", "开始炼成");
        translate("gui.spiritworld.forge.cancel", "Cancel", "取消");
        translate("gui.spiritworld.forge.target", "Target: %d / Completed: %d", "目标：%d / 已完成：%d");
        translate("gui.spiritworld.forge.target.infinite", "Infinite / Completed: %d", "无限循环 / 已完成：%d");
        translate("gui.spiritworld.forge.crafting_other", "Crafting: %s", "正在炼成：%s");
        translate("item.spiritworld.mana_crystal_basic", "Mana Crystal (Basic)", "魔力水晶（基础）");
        translate("item.spiritworld.mana_crystal_advanced", "Mana Crystal (Advanced)", "魔力水晶（进阶）");
        translate("item.spiritworld.mana_crystal_ultimate", "Mana Crystal (Ultimate)", "魔力水晶（终极）");
        translate("tooltip.spiritworld.mana_crystal.capacity", "Mana: %d / %d", "魔力：%d / %d");
        translate("tooltip.spiritworld.mana_crystal.entry", "%s: %d", "%s：%d");
        translate("tooltip.spiritworld.mana_crystal.hint",
                "Shift+Right-click a collector/core to insert",
                "Shift+右键装入聚集器或核心");
        translate("item.spiritworld.mana_clay", "Mana Clay", "魔力黏土");
        translate("item.spiritworld.mana_alloy", "Mana Alloy", "魔力合金锭");
        translate("item.spiritworld.mana_alloy_helmet", "Mana Alloy Helmet", "魔力合金头盔");
        translate("item.spiritworld.mana_alloy_chestplate", "Mana Alloy Chestplate", "魔力合金胸甲");
        translate("item.spiritworld.mana_alloy_leggings", "Mana Alloy Leggings", "魔力合金护腿");
        translate("item.spiritworld.mana_alloy_boots", "Mana Alloy Boots", "魔力合金靴子");
        translate("item.spiritworld.mana_alloy_sword", "Mana Alloy Sword", "魔力合金剑");
        translate("item.spiritworld.mana_alloy_pickaxe", "Mana Alloy Pickaxe", "魔力合金镐");
        translate("item.spiritworld.mana_alloy_axe", "Mana Alloy Axe", "魔力合金斧");
        translate("item.spiritworld.mana_alloy_shovel", "Mana Alloy Shovel", "魔力合金铲");
        translate("item.spiritworld.mana_alloy_hoe", "Mana Alloy Hoe", "魔力合金锄");
        translate("item.spiritworld.mana_alloy_arrow", "Mana Alloy Arrow", "魔力合金箭");
        translate("effect.spiritworld.mana_alloy_set", "Mana Alloy Set", "魔力合金套装");
        translate("item.spiritworld.mana_alloy_bow", "Mana Alloy Bow", "魔力合金弓");
        translate("enchantment.spiritworld.double_shot", "Double Shot", "双发快射");
        translate("enchantment.spiritworld.double_shot.desc",
                "Fires an additional small arrow 0.1s later. Level increases damage: 25% / 50% / 75%.",
                "射出主箭后 0.1 秒再射出一根小箭，等级提升伤害：25% / 50% / 75%。");
        translate("block.spiritworld.mana_relay_1", "Mana Relay I", "魔力中继器 I");
        translate("block.spiritworld.mana_relay_2", "Mana Relay II", "魔力中继器 II");
        translate("block.spiritworld.mana_relay_3", "Mana Relay III", "魔力中继器 III");
        translate("gui.spiritworld.relay.efficiency", "Current efficiency: %s%%", "当前效率: %s%%");
        translate("gui.spiritworld.relay.children", "Children node: %d / %d", "子节点: %d / %d");
        translate("gui.spiritworld.relay.disconnected", "Status: disconnected", "状态: 游离 (未连接)");
        translate("gui.spiritworld.collector.efficiency", "Current efficiency: %s%%", "传输效率: %s%%");
        translate("gui.spiritworld.collector.disconnected", "Status: disconnected", "状态: 游离 (无连接)");
        translate("gui.spiritworld.core.network_btn", "Network Link", "魔力链接");
        translate("gui.spiritworld.core.back_to_craft", "Back to Craft", "返回合成");
        translate("gui.spiritworld.forge.rebuild", "Rebuild Network", "重构全网");
        translate("gui.spiritworld.core.network_title", "Mana Network", "魔力网络拓扑");
        translate("gui.spiritworld.network.node_detail", "--- Node Details ---", "--- 节点详情 ---");
        translate("gui.spiritworld.network.pos", "Pos: %s", "坐标：%s");
        translate("gui.spiritworld.network.total_mana", "Total Mana: %d / %d", "总魔力：%d / %d");
        translate("gui.spiritworld.network.pull_details", "[Pull Details]", "[抽取明细]");
        translate("gui.spiritworld.network.pull_line", "Pulls %d %s from %s/s", "每秒抽取 %d %s，来自 %s");
        translate("gui.spiritworld.network.efficiency", "Efficiency: %s%%", "传输效率：%s%%");
        translate("gui.spiritworld.network.stock", "Stock: %d / %d", "当前库存：%d / %d");
        translate("gui.spiritworld.network.type", "Type: %s", "魔力属性：%s");
        translate("gui.spiritworld.network.core_node", "Core", "核心");
        translate("gui.spiritworld.network.relay_node", "Relay %d", "中继器 %d");
        translate("gui.spiritworld.network.collector_node", "Collector", "聚集器");
        translate("gui.spiritworld.network.parent", "Parent: %s", "父节点：%s");
        translate("gui.spiritworld.network.parent_none", "None", "无");
        translate("gui.spiritworld.network.children", "Children: %d", "子节点：%d");
        translate("gui.spiritworld.network.segment_efficiency", "Segment efficiency: %s", "段效率：%s");
        translate("gui.spiritworld.network.link_reason", "Status: %s", "状态：%s");
        translate("gui.spiritworld.network.status_connected", "Connected", "已连接");
        translate("gui.spiritworld.network.status_disconnected", "Disconnected", "断链");
        translate("gui.spiritworld.network.offline_gain", "Offline gain: %d", "离线累计：%d");
        translate("gui.spiritworld.network.offline_hint",
                "Full rate for %d min, then decays to %d%% over %d h",
                "前 %d 分钟全速，之后在 %d 小时内衰减至 %d%%");
        translate("gui.spiritworld.network.reason.connected", "Connected", "已连接");
        translate("gui.spiritworld.network.reason.no_parent", "No available parent node", "附近没有可连接的父节点");
        translate("gui.spiritworld.network.reason.parent_full", "Parent node is full", "父节点子节点已满");
        translate("gui.spiritworld.network.reason.out_of_range", "Out of parent range", "超出父节点连接范围");
        translate("gui.spiritworld.network.reason.parent_offline", "Parent not linked to core", "父节点未接入核心");
        translate("gui.spiritworld.network.reason.parent_lost", "Parent lost, waiting to relink", "父节点已断开，等待重连");
        translate("gui.spiritworld.network.reason.no_core", "No reachable core", "未找到可达核心");
        translate("message.spiritworld.network.diagnose.header", "--- Mana Network Diagnosis ---", "--- 魔力网络诊断 ---");
        translate("message.spiritworld.network.diagnose.summary",
                "Nodes: %d, disconnected: %d, <50%%: %d, 50-80%%: %d, >80%%: %d",
                "节点：%d，断链：%d，低于 50%%：%d，50-80%%：%d，高于 80%%：%d");
        translate("message.spiritworld.network.diagnose.not_found", "No mana network nodes found.", "未发现魔力网络节点。");
        // ===== Pocket Dimension =====
        translate("skill.spiritworld.pocket_dimension", "Spirit Pocket Dimension", "精神口袋维度");
        translate("skill.spiritworld.pocket_dimension.desc",
                "Creates your own personal dimension. Right-click to enter. Press V again to leave. Upgrading increases the area size.",
                "创造一个属于你的私人空间。装备后按 V 键进入，再次按下返回主世界。升级可扩大空间面积。");
        translate("skill.tooltip.pocket_dimension.size", "Current Space: %d x %d x %d", "当前空间：%d x %d x %d");
        translate("skill.tooltip.pocket_dimension.next", "Next Level: %d x %d x %d", "下一级空间：%d x %d x %d");
        translate("message.spiritworld.pocket_dimension.no_spirit",
                "Not enough spirit, need %d to teleport.",
                "精神力不足，传送需要 %d 点。");
        translate("message.spiritworld.pocket_dimension.full",
                "The pocket dimension is currently full. Cannot assign new land.",
                "当前口袋维度领地已满，无法分配新的空间。");
        translate("message.spiritworld.pocket_dimension.returned", "Returned to the Overworld.", "已返回主世界。");
        translate("message.spiritworld.pocket_dimension.entered", "Welcome to your pocket dimension.", "欢迎回到你的口袋维度。");
        // ===== Pocket Dimension Visit =====
        translate("message.spiritworld.pocket.visit.already_sent",
                "You have already sent a request. Please wait for a response!",
                "你已经发送过请求了，请等待对方回应！");
        translate("message.spiritworld.pocket.visit.sent", "Sent a domain visit request to %s.", "已向 %s 发送了领地访问请求。");
        translate("message.spiritworld.pocket.visit.request", "Player %s wants to visit your domain!", "玩家 %s 想访问你的领地！");
        translate("message.spiritworld.pocket.visit.accept_btn", "[ Accept ]", "[ 同意 ]");
        translate("message.spiritworld.pocket.visit.accept_hover", "Click to accept and teleport", "点击同意传送");
        translate("message.spiritworld.pocket.visit.deny_btn", "[ Deny ]", "[ 拒绝 ]");
        translate("message.spiritworld.pocket.visit.denied", "%s denied your visit request.", "%s 拒绝了你的访问请求。");
        translate("message.spiritworld.pocket.visit.self", "You cannot visit your own domain.", "你不能访问自己的领地。");
        translate("message.spiritworld.pocket.visit.expired", "The request does not exist or has expired.", "请求不存在或已过期。");
        translate("message.spiritworld.pocket.visit.invalid_uuid", "Invalid player UUID.", "玩家 UUID 无效。");
        translate("message.spiritworld.pocket.visit.no_domain", "You haven't assigned a domain yet!", "你还没有分配领地！");
        translate("message.spiritworld.pocket.visit.entered", "You entered %s's domain.", "你已进入 %s 的领地。");
        translate("message.spiritworld.pocket.visit.guest_entered", "%s entered your domain.", "%s 进入了你的领地。");
    }
}

package com.varlike.spiritworld.datagen;

import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModLanguageProvider extends LanguageProvider {

    public ModLanguageProvider(PackOutput output) {
        super(output, SpiritWorld.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        this.add(ModItems.BOTTLED_SPIRIT.get(), "Bottled Spirit");

        this.add("message.spiritworld.spirit_restored", "Restored %d spirit");
        this.add("message.spiritworld.spirit_already_full", "Spirit is already full");

        this.add("materialworld.addon_group.spirit_world", "Spirit World");

        this.add("spiritworld.configuration.title", "SpiritWorld Configs");
        this.add("spiritworld.configuration.section.spiritworld.common.toml", "SpiritWorld Common Configs");
        this.add("spiritworld.configuration.section.spiritworld.common.toml.title", "SpiritWorld Common Configs");
        this.add("spiritworld.configuration.section.spiritworld.client.toml", "SpiritWorld Client Configs");
        this.add("spiritworld.configuration.section.spiritworld.client.toml.title", "SpiritWorld Client Configs");

        this.add("spiritworld.configuration.spiritRegenAmount", "Spirit Regen Amount");
        this.add("spiritworld.configuration.spiritRegenInterval", "Spirit Regen Interval (ticks)");
        this.add("spiritworld.configuration.defaultMaxSpirit", "Default Max Spirit");
        this.add("spiritworld.configuration.syncInterval", "Sync Interval (ticks)");
        this.add("spiritworld.configuration.bottledSpiritRestoreAmount", "Bottled Spirit Restore Amount");
        this.add("spiritworld.configuration.spiritClearOnLogin", "Clear Spirit on Login");
        this.add("spiritworld.configuration.spiritClearOnDeath", "Clear Spirit on Death");

        this.add("spiritworld.configuration.spiritBarYOffset", "Spirit Bar Y Offset");
        this.add("spiritworld.configuration.showSpiritBar", "Show Spirit Bar");
        this.add("spiritworld.configuration.showSpiritText", "Show Spirit Value");

        // 物品
        this.add(ModItems.SPIRIT_IRON_RING.get(), "Spirit Iron Ring");
        this.add(ModItems.SPIRIT_GOLD_RING.get(), "Spirit Gold Ring");
        this.add(ModItems.SPIRIT_DIAMOND_RING.get(), "Spirit Diamond Ring");

        // 提示
        this.add("tooltip.spiritworld.ring.stored", "Stored: %d / %d");
        this.add("tooltip.spiritworld.ring.usage", "Right-click: store spirit. Shift+Right-click: extract. Right-click water cauldron: convert.");

        // 消息
        this.add("message.spiritworld.ring.full", "Ring is full");
        this.add("message.spiritworld.ring.empty", "Ring is empty");
        this.add("message.spiritworld.ring.no_spirit", "No spirit to store");
        this.add("message.spiritworld.ring.player_full", "Your spirit is full");
        this.add("message.spiritworld.ring.store", "Stored %d spirit");
        this.add("message.spiritworld.ring.extract", "Extracted %d spirit");
        this.add("message.spiritworld.ring.not_enough", "Not enough spirit, need %d");
        this.add("message.spiritworld.ring.converted", "Water converted to spirit potion");

        // ===== 按键 =====
        this.add("key.spiritworld.open_skill_tree", "Open Skill Tree");
        this.add("key.spiritworld.open_loadout", "Open Skill Loadout");
        this.add("key.spiritworld.open_radial", "Open Skill Radial");
        this.add("key.spiritworld.use_skill", "Use Skill");

        // ===== 技能树界面 =====
        this.add("screen.spiritworld.skill_tree.title", "Spirit Skill Tree");
        this.add("screen.spiritworld.skill_tree.level", "Vision Level: %d");
        this.add("screen.spiritworld.skill_tree.points", "Vision Points: %d");
        this.add("screen.spiritworld.skill_tree.progress", "%d / %d");

        // ===== 技能类型 =====
        this.add("skill.type.active", "Active Skill");
        this.add("skill.type.passive", "Passive Skill");
        this.add("skill.type.special", "Special Skill");

        // ===== 技能提示 =====
        this.add("skill.tooltip.level", "Level: %d / %d");
        this.add("skill.tooltip.cost", "Next Level Cost: %d");
        this.add("skill.tooltip.required_vision", "Required Vision Level: %d");
        this.add("skill.tooltip.prerequisites", "Prerequisites:");

        // ===== 内置示例技能 =====
        this.add("skill.spiritworld.spirit_sense", "Spirit Sense");
        this.add("skill.spiritworld.spirit_sense.desc", "Increases spirit regeneration.");
        this.add("skill.spiritworld.spirit_burst", "Spirit Burst");
        this.add("skill.spiritworld.spirit_burst.desc", "Active skill to burst spirit.");
        this.add("skill.spiritworld.spirit_focus", "Spirit Focus");
        this.add("skill.spiritworld.spirit_focus.desc", "Increases max spirit.");
        this.add("skill.spiritworld.spirit_vision", "Spirit Vision");
        this.add("skill.spiritworld.spirit_vision.desc", "Grants spirit vision.");
        this.add("skill.spiritworld.spirit_storm", "Spirit Storm");
        this.add("skill.spiritworld.spirit_storm.desc", "Devastating area attack.");
        this.add("skill.spiritworld.spirit_echo", "Spirit Echo");
        this.add("skill.spiritworld.spirit_echo.desc", "Echoes spirit energy.");

        this.add("screen.spiritworld.loadout.title", "Skill Loadout");
        this.add("screen.spiritworld.loadout.hint", "Left-click skill list, then click a slot. Right-click slot to clear.");
        this.add("screen.spiritworld.loadout.empty", "(No skills)");
        this.add("screen.spiritworld.loadout.passives", "Passive Skills");
        this.add("screen.spiritworld.loadout.passive.on", "ON");
        this.add("screen.spiritworld.loadout.passive.off", "OFF");
        this.add("screen.spiritworld.loadout.passive.always", "ALWAYS");
        this.add("screen.spiritworld.loadout.filter.all", "All");
        this.add("screen.spiritworld.loadout.filter.equippable", "Equippable");
        this.add("screen.spiritworld.loadout.switch_to_tree", "Skill Tree");
        this.add("screen.spiritworld.skill_tree.switch_to_loadout", "Skill Loadout");

        this.add("skill.spiritworld.emc_conversion", "EMC Conversion");
        this.add("skill.spiritworld.emc_conversion.desc", "Continuously converts EMC into Vision Points.");

        this.add("message.spiritworld.skill.emc_conversion.no_emc", "Not enough EMC, EMC Conversion stopped");
        this.add(ModItems.SPIRIT_TOOL.get(), "Spirit Tool");
        this.add("tooltip.spiritworld.spirit_tool.pickaxe", "Pickaxe: %s");
        this.add("tooltip.spiritworld.spirit_tool.axe", "Axe: %s");
        this.add("tooltip.spiritworld.spirit_tool.shovel", "Shovel: %s");
        this.add("tooltip.spiritworld.spirit_tool.hoe", "Hoe: %s");
        this.add("tooltip.spiritworld.spirit_tool.fortune", "Fortune %d");
        this.add("tooltip.spiritworld.spirit_tool.bound", "Bound to spirit, cannot be dropped");
        this.add("tooltip.spiritworld.spirit_tool.level.empty", "Bare Hand");
        this.add("tooltip.spiritworld.spirit_tool.level.wood", "Wood");
        this.add("tooltip.spiritworld.spirit_tool.level.stone", "Stone");
        this.add("tooltip.spiritworld.spirit_tool.level.iron", "Iron");
        this.add("tooltip.spiritworld.spirit_tool.level.diamond", "Diamond");
        this.add("tooltip.spiritworld.spirit_tool.level.netherite", "Netherite");

        this.add("message.spiritworld.spirit_tool.destroyed", "Spirit Tool dissolved");
        this.add("message.spiritworld.spirit_tool.already_have", "You already have a Spirit Tool");
        this.add("message.spiritworld.spirit_tool.no_spirit", "Not enough spirit (need 10)");
        this.add("message.spiritworld.spirit_tool.no_space", "No empty inventory slot");
        this.add("message.spiritworld.spirit_tool.summoned", "Spirit Tool summoned");
        this.add("message.spiritworld.spirit_tool.no_spirit_mining", "Not enough spirit to mine (%d needed)");

        // 技能学习失败提示
        this.add("message.spiritworld.skill.max_level", "This skill is already at max level");
        this.add("message.spiritworld.skill.prereq", "Requires %s first");
        this.add("message.spiritworld.skill.prereq_unknown", "Prerequisite skill not learned");
        this.add("message.spiritworld.skill.no_vision", "Requires Vision Level %d");
        this.add("message.spiritworld.skill.no_points", "Not enough Vision Points (need %d)");

        this.add("skill.spiritworld.summon_spirit_tool", "Summon Spirit Tool");
        this.add("skill.spiritworld.summon_spirit_tool.desc", "Summons a spirit tool bound to you. Costs 10 spirit.");
        this.add("skill.spiritworld.spirit_tool_pickaxe", "Spirit Pickaxe");
        this.add("skill.spiritworld.spirit_tool_pickaxe.desc", "Spirit tool mines pickaxe blocks; faster per level.");
        this.add("skill.spiritworld.spirit_tool_axe", "Spirit Axe");
        this.add("skill.spiritworld.spirit_tool_axe.desc", "Spirit tool chops axe blocks; faster per level.");
        this.add("skill.spiritworld.spirit_tool_shovel", "Spirit Shovel");
        this.add("skill.spiritworld.spirit_tool_shovel.desc", "Spirit tool digs shovel blocks; faster per level.");
        this.add("skill.spiritworld.spirit_tool_hoe", "Spirit Hoe");
        this.add("skill.spiritworld.spirit_tool_hoe.desc", "Spirit tool tills hoe blocks; faster per level.");
        this.add("skill.spiritworld.spirit_fortune", "Spirit Fortune");
        this.add("skill.spiritworld.spirit_fortune.desc", "Spirit tool gains Fortune while this passive is active.");

        this.add("skill.spiritworld.spirit_endurance", "Spirit Endurance");
        this.add("skill.spiritworld.spirit_endurance.desc", "+10 max spirit per level.");
        this.add("skill.spiritworld.spirit_toughness", "Spirit Toughness");
        this.add("skill.spiritworld.spirit_toughness.desc", "+0.5 spirit regen per level.");
        this.add("skill.spiritworld.spirit_strength", "Spirit Strength");
        this.add("skill.spiritworld.spirit_strength.desc", "+5% max spirit per level.");
        this.add("skill.spiritworld.spirit_surge", "Spirit Surge");
        this.add("skill.spiritworld.spirit_surge.desc", "+5% spirit regen per level.");
        this.add("skill.spiritworld.spirit_flow", "Spirit Flow");
        this.add("skill.spiritworld.spirit_flow.desc", "Regenerate 0.25% max spirit per second per level.");
        this.add("skill.spiritworld.spirit_source", "Spirit Source");
        this.add("skill.spiritworld.spirit_source.desc", "+50% max spirit, +50% regen.");

        this.add("skill.spiritworld.vision_sharpness", "Vision Sharpness");
        this.add("skill.spiritworld.vision_sharpness.desc", "+2 vision fragments per mob kill per level.");
        this.add("skill.spiritworld.emc_affinity", "EMC Affinity");
        this.add("skill.spiritworld.emc_affinity.desc", "-3% EMC Conversion cost per level.");
        this.add("skill.spiritworld.vision_mastery", "Vision Mastery");
        this.add("skill.spiritworld.vision_mastery.desc", "+15% vision fragment gain per level.");
        this.add("skill.spiritworld.spirit_efficiency", "Spirit Efficiency");
        this.add("skill.spiritworld.spirit_efficiency.desc", "Grants Efficiency I-V to the Spirit Tool.");
        // ===== 精神护盾 =====
        this.add("skill.spiritworld.spirit_shield", "Spirit Shield");
        this.add("skill.spiritworld.spirit_shield.desc",
                "Per level,incoming damage is absorbed by spirit. Requires spirit to activate.");
        this.add("skill.tooltip.spirit_shield.ratio", "Absorb: %s%% of damage");
        this.add("skill.tooltip.spirit_shield.cost", "Spirit cost: %s per damage");
        this.add("skill.tooltip.spirit_shield.example", "100 damage costs %d spirit");
        this.add("message.spiritworld.spirit_shield.insufficient", "Spirit depleted, shield failed");
        // ===== 精神夜视 =====
        this.add("skill.spiritworld.spirit_night_vision", "Spirit Night Vision");
        this.add("skill.spiritworld.spirit_night_vision.desc",
                "Grants night vision while active. Reduces max spirit; higher levels reduce less.");
        this.add("skill.tooltip.spirit_night_vision.penalty", "Max spirit: -%s%% while active");
        this.add("skill.tooltip.spirit_night_vision.penalty.none", "No max spirit penalty");
        // ===== 工具分支 =====
        this.add("skill.tooltip.tool_speed.current", "Mining speed: %s");
        this.add("skill.tooltip.fortune.current", "Fortune %s");
        this.add("skill.tooltip.efficiency.current", "Efficiency %s");
        // ===== 精神力系 =====
        this.add("skill.tooltip.spirit_endurance.current", "Current: +%s max spirit");
        this.add("skill.tooltip.spirit_toughness.current", "Current: +%s spirit regen");
        this.add("skill.tooltip.spirit_strength.current", "Current: +%s%% max spirit");
        this.add("skill.tooltip.spirit_surge.current", "Current: +%s%% spirit regen");
        this.add("skill.tooltip.spirit_flow.current", "Current: +%s%% max spirit per second");
        // ===== 灵视系 =====
        this.add("skill.tooltip.vision_sharpness.current", "Current: +%s fragments per kill");
        this.add("skill.tooltip.emc_affinity.current", "Current: -%s%% EMC conversion cost");
        this.add("skill.tooltip.vision_mastery.current", "Current: +%s%% vision fragments");
        // ===== EMC 转化 =====
        this.add("skill.tooltip.emc_conversion.rate", "Current: %s EMC per vision fragment");
        this.add("skill.tooltip.emc_conversion.note", "Must be equipped to take effect");
        // ===== 精神源泉 =====
        this.add("skill.tooltip.spirit_source.current", "Bonus: +%s%% max spirit, +%s%% regen");
        this.add("message.spiritworld.skill.cooldown", "Skill on cooldown: %ss");
        // ===== 精神修复 =====
        this.add("skill.spiritworld.spirit_repair", "Spirit Repair");
        this.add("skill.spiritworld.spirit_repair.desc",
                "Consumes spirit to repair the held weapon/tool.");
        this.add("skill.tooltip.spirit_repair.rate", "Restore: %s durability per spirit");
        this.add("message.spiritworld.spirit_repair.not_damageable",
                "Held item cannot be repaired");
        this.add("message.spiritworld.spirit_repair.full",
                "Held item is already at full durability");
        this.add("message.spiritworld.spirit_repair.no_spirit",
                "Not enough spirit to repair");
        this.add("message.spiritworld.spirit_repair.success",
                "Repaired %d durability (consumed %d spirit)");
        this.add("skill.tooltip.spirit_repair.next", "Next level: %s durability per spirit");
        this.add("skill.spiritworld.cooldown_reduction", "Spirit Alacrity");
        this.add("skill.spiritworld.cooldown_reduction.desc",
                "Reduces all skill cooldowns. Higher levels have diminishing returns.");
        this.add("skill.tooltip.cooldown_reduction.current", "Current: -%s%% cooldown");
        this.add("skill.tooltip.cooldown_reduction.example",
                "Toggle: %ss  |  Normal: %ss");
        this.add("skill.spiritworld.spirit_satiety", "Spirit Satiety");
        this.add("skill.spiritworld.spirit_satiety.desc",
                "While active, keeps your hunger bar full. Reduces max spirit; higher levels reduce less.");
        this.add("skill.tooltip.spirit_satiety.penalty", "Max spirit: -%s%% while active");
        this.add("skill.tooltip.spirit_satiety.penalty.none", "No max spirit penalty");
        this.add("skill.tooltip.spirit_satiety.note", "Also enables natural health regeneration");
        this.add("skill.spiritworld.spirit_maintenance", "Spirit Maintenance");
        this.add("skill.spiritworld.spirit_maintenance.desc",
                "Every second, restores durability to one damaged item in your inventory. Higher levels restore more.");
        this.add("skill.tooltip.spirit_maintenance.rate", "Restores %d durability per second");
        this.add("skill.tooltip.spirit_maintenance.next", "Next level: %d durability per second");
        this.add("skill.spiritworld.spirit_flight", "Spirit Flight");
        this.add("skill.spiritworld.spirit_flight.desc",
                "While active, grants creative flight. Consumes spirit per second; rising costs double.");
        this.add("skill.tooltip.spirit_flight.hover", "Hover/glide: %s spirit/sec");
        this.add("skill.tooltip.spirit_flight.rising", "Rising: %s spirit/sec");
        this.add("skill.tooltip.spirit_flight.next", "Next level: %s spirit/sec hover");
        this.add("message.spiritworld.spirit_flight.low_spirit",
                "Spirit low, flight disabled. Slow falling engaged.");
        // ===== 精神扫描 =====
        this.add("skill.spiritworld.spirit_scan", "Spirit Scan");
        this.add("skill.spiritworld.spirit_scan.desc",
                "Scan nearby ores. Right-click the skill node to configure range and target ores.");
        this.add("skill.tooltip.spirit_scan.max_range", "Max range: %d blocks");
        this.add("skill.tooltip.spirit_scan.duration", "Icons last %d sec");
        this.add("skill.tooltip.spirit_scan.next", "Next level: max range %d");
        this.add("skill.tooltip.spirit_scan.no_ore_selected",
                "No ore selected, open scan config");

// ===== 扫描矿物名（配置界面 + 图标显示用）=====
        this.add("spiritworld.scan.coal", "Coal");
        this.add("spiritworld.scan.iron", "Iron");
        this.add("spiritworld.scan.gold", "Gold");
        this.add("spiritworld.scan.redstone", "Redstone");
        this.add("spiritworld.scan.lapis", "Lapis");
        this.add("spiritworld.scan.diamond", "Diamond");
        this.add("spiritworld.scan.emerald", "Emerald");
        this.add("spiritworld.scan.ancient_debris", "Ancient Debris");
        this.add("spiritworld.scan.other", "Other Mod Ores");

// ===== 扫描结果提示 =====
        this.add("message.spiritworld.spirit_scan.no_spirit", "Not enough spirit (need %d)");
        this.add("message.spiritworld.spirit_scan.no_ore", "No ore selected, open scan config");
        this.add("message.spiritworld.spirit_scan.not_found", "No target ores found");
        this.add("message.spiritworld.spirit_scan.found", "Found %d ore cluster(s)");
    }
}
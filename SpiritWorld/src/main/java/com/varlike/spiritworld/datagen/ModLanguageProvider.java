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
    }
}
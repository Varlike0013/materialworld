package com.varlike.spiritworld.datagen;

import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModLanguageProviderZH extends LanguageProvider {

    public ModLanguageProviderZH(PackOutput output) {
        super(output, SpiritWorld.MOD_ID, "zh_cn");
    }

    @Override
    protected void addTranslations() {
        this.add(ModItems.BOTTLED_SPIRIT.get(), "瓶装精神力");

        this.add("message.spiritworld.spirit_restored", "恢复了 %d 点精神力");
        this.add("message.spiritworld.spirit_already_full", "精神力已满");

        this.add("materialworld.addon_group.spirit_world", "精神世界");

        this.add("spiritworld.configuration.title", "精神世界配置");
        this.add("spiritworld.configuration.section.spiritworld.common.toml", "精神世界通用配置");
        this.add("spiritworld.configuration.section.spiritworld.common.toml.title", "精神世界通用配置");
        this.add("spiritworld.configuration.section.spiritworld.client.toml", "精神世界客户端配置");
        this.add("spiritworld.configuration.section.spiritworld.client.toml.title", "精神世界客户端配置");

        this.add("spiritworld.configuration.spiritRegenAmount", "精神力恢复量");
        this.add("spiritworld.configuration.spiritRegenInterval", "精神力恢复间隔（tick）");
        this.add("spiritworld.configuration.defaultMaxSpirit", "默认精神力上限");
        this.add("spiritworld.configuration.syncInterval", "同步间隔（tick）");
        this.add("spiritworld.configuration.bottledSpiritRestoreAmount", "瓶装精神力恢复量");
        this.add("spiritworld.configuration.spiritClearOnLogin", "登录时清空精神力");
        this.add("spiritworld.configuration.spiritClearOnDeath", "死亡时清空精神力");

        this.add("spiritworld.configuration.spiritBarYOffset", "精神力条 Y 轴偏移");
        this.add("spiritworld.configuration.showSpiritBar", "显示精神力条");
        this.add("spiritworld.configuration.showSpiritText", "显示精神力数值");

        this.add(ModItems.SPIRIT_IRON_RING.get(), "精神铁秘戒");
        this.add(ModItems.SPIRIT_GOLD_RING.get(), "精神金秘戒");
        this.add(ModItems.SPIRIT_DIAMOND_RING.get(), "精神钻秘戒");

        this.add("tooltip.spiritworld.ring.stored", "已存储: %d / %d");
        this.add("tooltip.spiritworld.ring.usage", "右键：存入精神力。潜行右键：取出。右键含水炼药锅：转化。");

        this.add("message.spiritworld.ring.full", "戒指已满");
        this.add("message.spiritworld.ring.empty", "戒指是空的");
        this.add("message.spiritworld.ring.no_spirit", "没有精神力可存入");
        this.add("message.spiritworld.ring.player_full", "你的精神力已满");
        this.add("message.spiritworld.ring.store", "存入 %d 点精神力");
        this.add("message.spiritworld.ring.extract", "取出 %d 点精神力");
        this.add("message.spiritworld.ring.not_enough", "精神力不足，需要 %d");
        this.add("message.spiritworld.ring.converted", "已将水转化为精神药水");

        // ===== 按键 =====
        this.add("key.spiritworld.open_skill_tree", "打开技能树");

        // ===== 技能树界面 =====
        this.add("screen.spiritworld.skill_tree.title", "精神视界");
        this.add("screen.spiritworld.skill_tree.level", "灵视等级: %d");
        this.add("screen.spiritworld.skill_tree.points", "灵视点: %d");
        this.add("screen.spiritworld.skill_tree.progress", "%d / %d");

        // ===== 技能类型 =====
        this.add("skill.type.active", "主动技能");
        this.add("skill.type.passive", "被动技能");
        this.add("skill.type.special", "特殊技能");

        // ===== 技能提示 =====
        this.add("skill.tooltip.level", "等级: %d / %d");
        this.add("skill.tooltip.cost", "下一级消耗: %d");
        this.add("skill.tooltip.required_vision", "需要灵视等级: %d");
        this.add("skill.tooltip.prerequisites", "前置：");

        // ===== 内置示例技能 =====
        this.add("skill.spiritworld.spirit_sense", "精神感知");
        this.add("skill.spiritworld.spirit_sense.desc", "提升精神力恢复速度。");
        this.add("skill.spiritworld.spirit_burst", "精神爆发");
        this.add("skill.spiritworld.spirit_burst.desc", "主动技能，释放精神爆发。");
        this.add("skill.spiritworld.spirit_focus", "精神专注");
        this.add("skill.spiritworld.spirit_focus.desc", "提升精神力上限。");
        this.add("skill.spiritworld.spirit_vision", "灵视");
        this.add("skill.spiritworld.spirit_vision.desc", "开启灵视视野。");
        this.add("skill.spiritworld.spirit_storm", "精神风暴");
        this.add("skill.spiritworld.spirit_storm.desc", "毁灭性的范围攻击。");
        this.add("skill.spiritworld.spirit_echo", "精神回响");
        this.add("skill.spiritworld.spirit_echo.desc", "回响精神能量。");
    }
}
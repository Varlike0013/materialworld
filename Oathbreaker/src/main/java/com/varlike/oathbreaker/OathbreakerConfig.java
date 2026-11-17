package com.varlike.oathbreaker;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Mod 通用配置。
 *
 * <p>当前只登记已在设计文档中定值的参数（死魂灵公式、怪物缩放系数、调试开关）。
 * 后续新增可配置项时，按分组 push/pop，避免键名散落。</p>
 */
public final class OathbreakerConfig {
    public static final ModConfigSpec SPEC;

    // 死魂灵
    public static final ModConfigSpec.DoubleValue BASE_SOUL;
    public static final ModConfigSpec.DoubleValue SOUL_LEVEL_BONUS;

    // 怪物缩放（锚点物化时使用，P3 落地）
    public static final ModConfigSpec.DoubleValue MOB_HEALTH_PER_PLAYER;
    public static final ModConfigSpec.DoubleValue MOB_HEALTH_PER_LEVEL;
    public static final ModConfigSpec.DoubleValue MOB_ATTACK_PER_PLAYER;
    public static final ModConfigSpec.DoubleValue MOB_ATTACK_PER_LEVEL;
    public static final ModConfigSpec.DoubleValue MOB_HEALTH_MULTIPLIER;

    // 玩家基础面板（LoL 风格）
    public static final ModConfigSpec.DoubleValue BASE_ARMOR;
    public static final ModConfigSpec.DoubleValue BASE_MAGIC_RESIST;
    public static final ModConfigSpec.DoubleValue BASE_MAX_MANA;
    public static final ModConfigSpec.DoubleValue BASE_MANA_REGENERATION;
    public static final ModConfigSpec.DoubleValue BASE_HEALTH_REGENERATION;
    public static final ModConfigSpec.DoubleValue PLAYER_HEALTH_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue PLAYER_ATTACK_MULTIPLIER;

    // 调试
    public static final ModConfigSpec.BooleanValue DEBUG_LOGGING;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("局外成长：死魂灵").push("souls");
        BASE_SOUL = builder
                .comment("每次击杀获得的基础死魂灵数量")
                .defineInRange("baseSoul", 10.0D, 0.0D, 10000.0D);
        SOUL_LEVEL_BONUS = builder
                .comment("每层关卡对死魂灵的加成系数，对应公式 baseSoul * (1 + bonus * levelIndex)")
                .defineInRange("soulLevelBonus", 0.15D, 0.0D, 10.0D);
        builder.pop();

        builder.comment("怪物缩放系数（锚点物化 / 生成时快照）").push("mob_scaling");
        MOB_HEALTH_PER_PLAYER = builder
                .comment("每多一名玩家，生命值额外增加的倍率")
                .defineInRange("healthPerExtraPlayer", 0.5D, 0.0D, 100.0D);
        MOB_HEALTH_PER_LEVEL = builder
                .comment("每层关卡，生命值额外增加的倍率")
                .defineInRange("healthPerLevel", 0.15D, 0.0D, 100.0D);
        MOB_ATTACK_PER_PLAYER = builder
                .comment("每多一名玩家，攻击力额外增加的倍率")
                .defineInRange("attackPerExtraPlayer", 0.2D, 0.0D, 100.0D);
        MOB_ATTACK_PER_LEVEL = builder
                .comment("每层关卡，攻击力额外增加的倍率")
                .defineInRange("attackPerLevel", 0.25D, 0.0D, 100.0D);
        MOB_HEALTH_MULTIPLIER = builder
                .comment("全局怪物最大生命倍率；用于放大数值方便后续调参")
                .defineInRange("healthMultiplier", 5.0D, 1.0D, 100.0D);
        builder.pop();

        builder.comment("玩家基础面板（LoL 风格，登录/重生时施加固定 ID 修饰符）").push("base_stats");
        BASE_ARMOR = builder
                .comment("基础护甲：原版 armor 属性 + 该值，走 100/(100+护甲) 减伤")
                .defineInRange("baseArmor", 30.0D, 0.0D, 1000.0D);
        BASE_MAGIC_RESIST = builder
                .comment("基础魔法抗性")
                .defineInRange("baseMagicResist", 30.0D, 0.0D, 1000.0D);
        BASE_MAX_MANA = builder
                .comment("基础最大法力值")
                .defineInRange("baseMaxMana", 300.0D, 0.0D, 10000.0D);
        BASE_MANA_REGENERATION = builder
                .comment("基础法力回复（每 5 秒）")
                .defineInRange("baseManaRegeneration", 15.0D, 0.0D, 1000.0D);
        BASE_HEALTH_REGENERATION = builder
                .comment("基础生命回复（每 5 秒）")
                .defineInRange("baseHealthRegeneration", 5.0D, 0.0D, 1000.0D);
        PLAYER_HEALTH_MULTIPLIER = builder
                .comment("玩家最大生命倍率；放大数值方便后续调参")
                .defineInRange("healthMultiplier", 5.0D, 1.0D, 100.0D);
        PLAYER_ATTACK_MULTIPLIER = builder
                .comment("玩家攻击力倍率，作用于 AD 总值（含武器）")
                .defineInRange("attackMultiplier", 5.0D, 1.0D, 100.0D);
        builder.pop();

        builder.comment("调试").push("debug");
        DEBUG_LOGGING = builder
                .comment("输出分类、伤害管线、职业武器适配的调试日志")
                .define("debugLogging", false);
        builder.pop();

        SPEC = builder.build();
    }

    private OathbreakerConfig() {}
}

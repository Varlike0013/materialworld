package com.varlike.spiritworld;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = SpiritWorld.MOD_ID)
public class Config {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.LongValue SPIRIT_REGEN_AMOUNT = BUILDER
            .comment("每次精神力恢复量 / Spirit restored per regen tick")
            .defineInRange("spiritRegenAmount", 1L, 1L, 1000L);

    public static final ModConfigSpec.IntValue SPIRIT_REGEN_INTERVAL = BUILDER
            .comment("精神力恢复间隔（tick）/ Spirit regen interval in ticks")
            .defineInRange("spiritRegenInterval", 40, 1, 72000);

    public static final ModConfigSpec.LongValue DEFAULT_MAX_SPIRIT = BUILDER
            .comment("新玩家的默认精神力上限 / Default max spirit for new players")
            .defineInRange("defaultMaxSpirit", 100L, 1L, 100000L);

    public static final ModConfigSpec.IntValue SYNC_INTERVAL = BUILDER
            .comment("客户端同步间隔（tick）/ Client sync interval in ticks")
            .defineInRange("syncInterval", 10, 1, 1200);

    public static final ModConfigSpec.LongValue BOTTLED_SPIRIT_RESTORE_AMOUNT = BUILDER
            .comment("瓶装精神力恢复的精神力量 / Spirit restored by bottled spirit")
            .defineInRange("bottledSpiritRestoreAmount", 50L, 1L, 10000L);

    public static final ModConfigSpec.BooleanValue SPIRIT_CLEAR_ON_LOGIN = BUILDER
            .comment("玩家进入游戏时是否清空精神力（进度已持久化，默认保留）/ Whether to clear spirit on player login")
            .define("spiritClearOnLogin", false);

    public static final ModConfigSpec.BooleanValue SPIRIT_CLEAR_ON_DEATH = BUILDER
            .comment("玩家死亡重生时是否清空精神力（默认保留，避免与重生自动召唤抢顺序）/ Whether to clear spirit on death respawn")
            .define("spiritClearOnDeath", false);

    public static final ModConfigSpec.LongValue EMC_CONVERSION_EMC_COST = BUILDER
            .comment("EMC 转化技能每级每次消耗的 EMC")
            .defineInRange("emcConversionEmcCost", 20L, 1L, 1000000L);

    public static final ModConfigSpec.IntValue EMC_CONVERSION_BASE_INTERVAL = BUILDER
            .comment("EMC 转化技能基础触发间隔（tick）")
            .defineInRange("emcConversionBaseInterval", 20, 2, 200);

    public static final ModConfigSpec.DoubleValue EMC_CONVERSION_BASE_RATE = BUILDER
            .comment("基础转化率（灵视点 / EMC）")
            .defineInRange("emcConversionBaseRate", 0.02, 0.001, 10.0);

    public static final ModConfigSpec.DoubleValue EMC_CONVERSION_VISION_BONUS = BUILDER
            .comment("每点灵视等级提升的转化率系数")
            .defineInRange("emcConversionVisionBonus", 0.05, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue EMC_CONVERSION_SKILL_BONUS = BUILDER
            .comment("每级技能提升的转化率系数")
            .defineInRange("emcConversionSkillBonus", 0.05, 0.0, 1.0);


    static final ModConfigSpec SPEC = BUILDER.build();

    public static long spiritRegenAmount = 1L;
    public static int spiritRegenInterval = 40;
    public static long defaultMaxSpirit = 100L;
    public static int syncInterval = 10;
    public static boolean spiritClearOnLogin = false;
    public static boolean spiritClearOnDeath = false;
    public static long bottledSpiritRestoreAmount = 50L;
    public static long emcConversionEmcCost = 20L;
    public static int emcConversionBaseInterval = 20;
    public static Double emcConversionBaseRate = 0.02;
    public static Double emcConversionVisionBonus = 0.05;
    public static Double emcConversionSkillBonus = 0.05;

    /** 手动加载配置值，在 FMLCommonSetupEvent 和 ModConfigEvent.Reloading 中调用 */
    public static void loadValues() {
        spiritRegenAmount = SPIRIT_REGEN_AMOUNT.get();
        spiritRegenInterval = SPIRIT_REGEN_INTERVAL.get();
        defaultMaxSpirit = DEFAULT_MAX_SPIRIT.get();
        syncInterval = SYNC_INTERVAL.get();
        spiritClearOnLogin = SPIRIT_CLEAR_ON_LOGIN.get();
        spiritClearOnDeath = SPIRIT_CLEAR_ON_DEATH.get();
        bottledSpiritRestoreAmount = BOTTLED_SPIRIT_RESTORE_AMOUNT.get();
        emcConversionEmcCost = EMC_CONVERSION_EMC_COST.get();
        emcConversionBaseInterval = EMC_CONVERSION_BASE_INTERVAL.get();
        emcConversionBaseRate = EMC_CONVERSION_BASE_RATE.get();
        emcConversionVisionBonus = EMC_CONVERSION_VISION_BONUS.get();
        emcConversionSkillBonus = EMC_CONVERSION_SKILL_BONUS.get();
    }
    @SubscribeEvent
    static void onReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() != SPEC) return;
        loadValues();
    }
}
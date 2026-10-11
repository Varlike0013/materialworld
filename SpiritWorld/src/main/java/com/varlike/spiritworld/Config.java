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

    public static final ModConfigSpec.BooleanValue SPIRIT_CLEAR_ON_LOGIN = BUILDER
            .comment("玩家进入游戏时是否清空精神力 / Whether to clear spirit on player login")
            .define("spiritClearOnLogin", true);

    public static final ModConfigSpec.BooleanValue SPIRIT_CLEAR_ON_DEATH = BUILDER
            .comment("玩家死亡重生时是否清空精神力 / Whether to clear spirit on death respawn")
            .define("spiritClearOnDeath", true);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static long spiritRegenAmount = 1L;
    public static int spiritRegenInterval = 40;
    public static long defaultMaxSpirit = 100L;
    public static int syncInterval = 10;
    public static boolean spiritClearOnLogin = true;
    public static boolean spiritClearOnDeath = true;

    @SubscribeEvent
    static void onLoad(ModConfigEvent event) {
        if (event.getConfig().getSpec() != SPEC) return;
        spiritRegenAmount = SPIRIT_REGEN_AMOUNT.get();
        spiritRegenInterval = SPIRIT_REGEN_INTERVAL.get();
        defaultMaxSpirit = DEFAULT_MAX_SPIRIT.get();
        syncInterval = SYNC_INTERVAL.get();
        spiritClearOnLogin = SPIRIT_CLEAR_ON_LOGIN.get();
        spiritClearOnDeath = SPIRIT_CLEAR_ON_DEATH.get();
    }
}
package com.varlike.materialworld;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = MaterialWorld.MOD_ID, value = Dist.CLIENT)
public class ClientConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.EnumValue<TransmutationDisplayMode> TRANSMUTATION_DISPLAY_MODE = BUILDER
            .comment("物质转换界面的显示模式 / Transmutation screen display mode",
                    "ALL: 显示所有有 EMC 的物品 / Show all items with EMC",
                    "LEARNED_ONLY: 只显示已学习的物品 / Show only learned items")
            .defineEnum("transmutationDisplayMode", TransmutationDisplayMode.LEARNED_ONLY);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static TransmutationDisplayMode transmutationDisplayMode = TransmutationDisplayMode.LEARNED_ONLY;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() != SPEC) return;
        transmutationDisplayMode = TRANSMUTATION_DISPLAY_MODE.get();
    }
    @SubscribeEvent
    static void onReload(final ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() != SPEC) return;
        transmutationDisplayMode = TRANSMUTATION_DISPLAY_MODE.get();
    }

    public enum TransmutationDisplayMode {
        ALL,
        LEARNED_ONLY
    }
}
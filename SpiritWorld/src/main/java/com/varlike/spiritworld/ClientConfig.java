package com.varlike.spiritworld;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = SpiritWorld.MOD_ID, value = Dist.CLIENT)
public class ClientConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue SPIRIT_BAR_Y_OFFSET = BUILDER
            .comment("精神力条相对饱食度条上方的额外 Y 轴偏移（像素），正值向上",
                    "Spirit bar Y offset above the food bar in pixels, positive moves up")
            .defineInRange("spiritBarYOffset", 0, -200, 200);

    public static final ModConfigSpec.BooleanValue SHOW_SPIRIT_BAR = BUILDER
            .comment("是否显示精神力条 / Whether to show the spirit bar")
            .define("showSpiritBar", true);

    public static final ModConfigSpec.BooleanValue SHOW_SPIRIT_TEXT = BUILDER
            .comment("是否在精神力条上显示数值 / Whether to show numeric spirit value")
            .define("showSpiritText", true);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static int spiritBarYOffset = 0;
    public static boolean showSpiritBar = true;
    public static boolean showSpiritText = true;

    @SubscribeEvent
    static void onLoad(ModConfigEvent event) {
        if (event.getConfig().getSpec() != SPEC) return;
        spiritBarYOffset = SPIRIT_BAR_Y_OFFSET.get();
        showSpiritBar = SHOW_SPIRIT_BAR.get();
        showSpiritText = SHOW_SPIRIT_TEXT.get();
    }
}
package com.varlike.spiritworld;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = SpiritWorld.MOD_ID, value = Dist.CLIENT)
public class ClientConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue SHOW_SPIRIT_BAR = BUILDER
            .comment("是否显示精神力条")
            .define("showSpiritBar", true);

    public static final ModConfigSpec.BooleanValue SHOW_SPIRIT_TEXT = BUILDER
            .comment("是否显示精神力数值")
            .define("showSpiritText", true);

    public static final ModConfigSpec.DoubleValue SCAN_ICON_MIN_SCALE = BUILDER
            .comment("扫描矿物图标最小缩放；距离很远时不会小于该值")
            .defineInRange("scanIconMinScale", 0.35, 0.1, 4.0);

    public static final ModConfigSpec.DoubleValue SCAN_ICON_MAX_SCALE = BUILDER
            .comment("扫描矿物图标最大缩放；距离很近时不会大于该值")
            .defineInRange("scanIconMaxScale", 2.0, 0.1, 4.0);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean showSpiritBar = true;
    public static boolean showSpiritText = true;
    public static float scanIconMinScale = 0.35f;
    public static float scanIconMaxScale = 2.0f;

    public static void loadValues() {
        showSpiritBar = SHOW_SPIRIT_BAR.get();
        showSpiritText = SHOW_SPIRIT_TEXT.get();
        scanIconMinScale = SCAN_ICON_MIN_SCALE.get().floatValue();
        scanIconMaxScale = SCAN_ICON_MAX_SCALE.get().floatValue();
    }

    @SubscribeEvent
    static void onReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() != SPEC) return;
        loadValues();
    }
}

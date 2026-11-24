package com.varlike.spiritworld;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = SpiritWorld.MOD_ID, value = Dist.CLIENT)
public class ClientConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // 已废弃：HUD 改由 AetherUI 布局后，位置由 AetherUI 统一控制。
    public static final ModConfigSpec.IntValue SPIRIT_BAR_Y_OFFSET = BUILDER
            .comment("【已废弃】精神力条 Y 偏移，现在由 AetherUI 统一布局")
            .defineInRange("spiritBarYOffset", 0, -200, 200);

    public static final ModConfigSpec.BooleanValue SHOW_SPIRIT_BAR = BUILDER
            .comment("是否显示精神力条")
            .define("showSpiritBar", true);

    public static final ModConfigSpec.BooleanValue SHOW_SPIRIT_TEXT = BUILDER
            .comment("是否显示精神力数值")
            .define("showSpiritText", true);
    // 以下三项已废弃：被动图标改由 AetherUI BuffHud 统一布局（每行 8 个，物品栏上方）。
    public static final ModConfigSpec.IntValue HUD_PASSIVE_MAX_ROWS = BUILDER
            .comment("【已废弃】被动图标行数，现在由 AetherUI BuffHud 控制")
            .defineInRange("hudPassiveMaxRows", 1, 1, 5);

    public static final ModConfigSpec.IntValue HUD_PASSIVE_PER_ROW = BUILDER
            .comment("【已废弃】被动图标每行数量，现在由 AetherUI BuffHud 控制")
            .defineInRange("hudPassivePerRow", 8, 1, 16);

    public static final ModConfigSpec.IntValue HUD_PASSIVE_ICON_SIZE = BUILDER
            .comment("【已废弃】被动图标尺寸，现在由 AetherUI BuffHud 控制")
            .defineInRange("hudPassiveIconSize", 12, 8, 24);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static int spiritBarYOffset = 0;
    public static boolean showSpiritBar = true;
    public static boolean showSpiritText = true;
    public static int hudPassiveMaxRows = 0;
    public static int hudPassivePerRow = 0;
    public static int hudPassiveIconSize = 0;

    public static void loadValues() {
        spiritBarYOffset = SPIRIT_BAR_Y_OFFSET.get();
        showSpiritBar = SHOW_SPIRIT_BAR.get();
        showSpiritText = SHOW_SPIRIT_TEXT.get();
        hudPassiveMaxRows = HUD_PASSIVE_MAX_ROWS.get();
        hudPassivePerRow = HUD_PASSIVE_PER_ROW.get();
        hudPassiveIconSize = HUD_PASSIVE_ICON_SIZE.get();
    }

    @SubscribeEvent
    static void onReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() != SPEC) return;
        loadValues();
    }
}
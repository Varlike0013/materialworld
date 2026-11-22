package com.varlike.aetherui.core.hud;

import com.varlike.aetherui.Config;
import com.varlike.aetherui.AetherUIMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.api.distmarker.Dist;

import java.util.Set;

/** 按配置取消原版状态条层；物品栏、准星、聊天等其他 UI 不受影响。 */
@EventBusSubscriber(modid = AetherUIMod.MOD_ID, value = Dist.CLIENT)
public final class VanillaHudHandler {

    private static final Set<net.minecraft.resources.ResourceLocation> STATUS_BARS = Set.of(
            VanillaGuiLayers.PLAYER_HEALTH,
            VanillaGuiLayers.ARMOR_LEVEL,
            VanillaGuiLayers.FOOD_LEVEL,
            VanillaGuiLayers.AIR_LEVEL,
            VanillaGuiLayers.EXPERIENCE_BAR,
            VanillaGuiLayers.EXPERIENCE_LEVEL,
            VanillaGuiLayers.VEHICLE_HEALTH,
            VanillaGuiLayers.JUMP_METER,
            VanillaGuiLayers.EFFECTS
    );

    private VanillaHudHandler() {}

    @SubscribeEvent
    static void onRenderGuiLayerPre(RenderGuiLayerEvent.Pre event) {
        if (!Config.hideVanillaBars()) return;
        if (STATUS_BARS.contains(event.getName())) event.setCanceled(true);
    }
}

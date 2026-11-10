package com.varlike.spiritworld.event;

import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.world.PocketDimensionManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ExplosionEvent;

@EventBusSubscriber(modid = SpiritWorld.MOD_ID)
public class PocketExplosionEvents {

    @SubscribeEvent
    public static void onExplosionStart(ExplosionEvent.Start event) {
        // 如果爆炸发生在口袋维度，直接取消
        if (event.getLevel().dimension().equals(PocketDimensionManager.POCKET_DIMENSION)) {
            event.setCanceled(true);
        }
    }
}
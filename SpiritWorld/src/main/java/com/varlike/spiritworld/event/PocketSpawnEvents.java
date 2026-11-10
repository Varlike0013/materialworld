package com.varlike.spiritworld.event;

import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.world.PocketDimensionManager;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

@EventBusSubscriber(modid = SpiritWorld.MOD_ID)
public class PocketSpawnEvents {

    @SubscribeEvent
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        // 如果不在口袋维度，直接放行
        if (!event.getLevel().getLevel().dimension().equals(PocketDimensionManager.POCKET_DIMENSION)) {
            return;
        }

        // 获取怪物的生成来源
        MobSpawnType type = event.getSpawnType();

        // 拦截自然生成和区块生成
        // 放行：刷怪笼 (SPAWNER)、刷怪蛋 (SPAWN_EGG)、指令 (COMMAND)、结构生成等
        if (type == MobSpawnType.NATURAL || type == MobSpawnType.CHUNK_GENERATION) {
            event.setCanceled(true);
        }
    }
}
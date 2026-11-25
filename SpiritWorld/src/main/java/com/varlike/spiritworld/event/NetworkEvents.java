package com.varlike.spiritworld.event;

import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.network.NetworkManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** 魔力网络的周期性维护：孤儿节点延迟重连。 */
@EventBusSubscriber(modid = SpiritWorld.MOD_ID)
public final class NetworkEvents {

    private NetworkEvents() {}

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        event.getServer().getAllLevels().forEach(NetworkManager::tickRelinks);
    }
}

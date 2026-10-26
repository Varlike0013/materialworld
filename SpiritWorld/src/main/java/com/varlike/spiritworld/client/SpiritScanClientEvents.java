package com.varlike.spiritworld.client;

import com.varlike.spiritworld.SpiritWorld;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

@EventBusSubscriber(modid = SpiritWorld.MOD_ID, value = Dist.CLIENT)
public final class SpiritScanClientEvents {

    private SpiritScanClientEvents() {}

    /** 客户端登出：清空扫描图标缓存 */
    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientScanData.clear();
    }

    /** 客户端重生 / 切维度：LocalPlayer 实例被替换，旧图标失效 */
    @SubscribeEvent
    public static void onClone(ClientPlayerNetworkEvent.Clone event) {
        ClientScanData.clear();
    }
}
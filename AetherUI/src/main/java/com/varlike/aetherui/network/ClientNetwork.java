package com.varlike.aetherui.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

/** 客户端发包入口：只在玩家所在世界且有连接时发送请求，并做节流。 */
public final class ClientNetwork {

    private static final long MIN_INTERVAL_MS = 350L;
    private static long lastRequestMs = Long.MIN_VALUE;

    private ClientNetwork() {}

    public static void request(BlockPos target, BlockPos origin) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() == null || mc.level == null || mc.player == null) return;

        long now = System.currentTimeMillis();
        if (now - lastRequestMs < MIN_INTERVAL_MS) return;
        lastRequestMs = now;

        BlockPos safeOrigin = origin != null ? origin : mc.player.blockPosition();
        mc.getConnection().send(new InspectPayloads.Request(
                target != null,
                target != null ? target.immutable() : BlockPos.ZERO,
                safeOrigin.immutable()));
    }
}

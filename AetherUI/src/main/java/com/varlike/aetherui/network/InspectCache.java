package com.varlike.aetherui.network;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 客户端保存最近一次服务端查询快照，避免每帧发包。 */
public final class InspectCache {

    private static final long TTL_MS = 5000L;

    private static volatile long lastUpdateMs = Long.MIN_VALUE;
    private static volatile InspectPayloads.ContainerInfo container;
    private static final Map<BlockPos, InspectPayloads.FurnaceInfo> FURNACES = new LinkedHashMap<>();

    private InspectCache() {}

    public static void apply(InspectPayloads.Snapshot snapshot) {
        if (snapshot == null) return;
        synchronized (FURNACES) {
            container = snapshot.container();
            FURNACES.clear();
            for (InspectPayloads.FurnaceInfo info : snapshot.furnaces()) {
                ItemStack copy = info.output().copy();
                FURNACES.put(info.pos().immutable(), new InspectPayloads.FurnaceInfo(info.pos(), copy, info.lit()));
            }
            lastUpdateMs = System.currentTimeMillis();
        }
    }

    public static void clear() {
        synchronized (FURNACES) {
            container = null;
            FURNACES.clear();
            lastUpdateMs = Long.MIN_VALUE;
        }
    }

    public static InspectPayloads.ContainerInfo container(BlockPos pos) {
        if (!fresh() || pos == null) return null;
        InspectPayloads.ContainerInfo info = container;
        return info != null && info.pos().equals(pos) ? info : null;
    }

    public static InspectPayloads.FurnaceInfo furnace(BlockPos pos) {
        if (!fresh() || pos == null) return null;
        synchronized (FURNACES) {
            return FURNACES.get(pos);
        }
    }

    public static List<InspectPayloads.FurnaceInfo> furnaces() {
        if (!fresh()) return List.of();
        synchronized (FURNACES) {
            return new ArrayList<>(FURNACES.values());
        }
    }

    private static boolean fresh() {
        return System.currentTimeMillis() - lastUpdateMs < TTL_MS;
    }
}

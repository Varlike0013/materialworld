package com.varlike.spiritworld.client;

import com.varlike.spiritworld.network.ClientboundScanResultPacket.OreIcon;

import java.util.List;

/**
 * 客户端扫描结果缓存。
 * 渲染层读这里的数据画图标，过期后自动清空。
 */
public final class ClientScanData {

    private static List<OreIcon> icons = List.of();
    private static long expireAtMillis = 0L;

    private ClientScanData() {}

    public static void setIcons(List<OreIcon> newIcons, int durationSeconds) {
        icons = newIcons == null ? List.of() : List.copyOf(newIcons);
        expireAtMillis = System.currentTimeMillis() + durationSeconds * 1000L;
    }

    /** 当前有效的图标列表；过期返回空列表 */
    public static List<OreIcon> getActiveIcons() {
        if (icons.isEmpty()) return List.of();
        if (System.currentTimeMillis() >= expireAtMillis) {
            icons = List.of();
            return List.of();
        }
        return icons;
    }

    /** 剩余时间占初始时长的比例，用于淡出；过期返回 0 */
    public static float getFadeRatio(int totalDurationSeconds) {
        long now = System.currentTimeMillis();
        long total = totalDurationSeconds * 1000L;
        long remain = expireAtMillis - now;
        if (remain <= 0L) return 0.0f;
        if (remain >= total) return 1.0f;
        return (float) remain / (float) total;
    }

    /** 切维度 / 登出时清空 */
    public static void clear() {
        icons = List.of();
        expireAtMillis = 0L;
    }
}
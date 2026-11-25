package com.varlike.spiritworld.network;

import com.varlike.spiritworld.skill.ManaType;

/** 聚集器离线段影子数据：收益有累计上限，超过全速窗口后线性衰减。 */
public class OfflineCollectorData {

    /** 前 30 分钟按 100% 速度累计。 */
    public static final long FULL_RATE_SECONDS = 30L * 60L;
    /** 最多累计 2 小时，之后不再增加。 */
    public static final long MAX_OFFLINE_SECONDS = 2L * 60L * 60L;
    /** 衰减下限：到达累计上限时仍保留 50% 速度。 */
    public static final double MIN_OFFLINE_FACTOR = 0.5;

    public long mana = 0;
    public ManaType type = ManaType.NONE;
    public long lastUpdateTick = 0;
    public long speed = 1;
    public long capacity = 100;

    public OfflineCollectorData() {}

    public OfflineCollectorData(long mana, ManaType type, long lastUpdateTick, long speed, long capacity) {
        this.mana = mana;
        this.type = type;
        this.lastUpdateTick = lastUpdateTick;
        this.speed = speed;
        this.capacity = capacity;
    }

    /** 计算从上次更新到现在应该产出多少魔力。 */
    public long calculateOfflineGain(long currentTick) {
        if (currentTick <= lastUpdateTick) return 0;

        long fullTicks = FULL_RATE_SECONDS * 20L;
        long maxTicks = MAX_OFFLINE_SECONDS * 20L;
        long elapsed = Math.min(currentTick - lastUpdateTick, maxTicks);

        double factor = 1.0;
        if (elapsed > fullTicks) {
            double decay = (double) (elapsed - fullTicks) / (double) (maxTicks - fullTicks);
            factor = 1.0 - (1.0 - MIN_OFFLINE_FACTOR) * Math.min(1.0, decay);
        }

        long gain = (long) Math.floor(speed * (elapsed / 20.0) * factor);
        return Math.max(0L, Math.min(gain, capacity - mana));
    }

    /** 供 GUI 展示当前影子队列的衰减系数。 */
    public double currentFactor(long currentTick) {
        if (currentTick <= lastUpdateTick) return 1.0;
        long fullTicks = FULL_RATE_SECONDS * 20L;
        long maxTicks = MAX_OFFLINE_SECONDS * 20L;
        long elapsed = Math.min(currentTick - lastUpdateTick, maxTicks);
        if (elapsed <= fullTicks) return 1.0;
        double decay = (double) (elapsed - fullTicks) / (double) (maxTicks - fullTicks);
        return 1.0 - (1.0 - MIN_OFFLINE_FACTOR) * Math.min(1.0, decay);
    }
}

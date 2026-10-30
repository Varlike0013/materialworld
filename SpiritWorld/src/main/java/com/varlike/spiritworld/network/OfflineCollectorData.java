package com.varlike.spiritworld.network;

import com.varlike.spiritworld.skill.ManaType;

public class OfflineCollectorData {
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

    /** 计算从上次更新到现在应该产出多少魔力 */
    public long calculateOfflineGain(long currentTick) {
        if (currentTick <= lastUpdateTick) return 0;
        long deltaTicks = currentTick - lastUpdateTick;
        long gain = (speed * deltaTicks) / 20; // 每 20 tick 产出 speed 点
        return Math.min(gain, capacity - mana);
    }
}
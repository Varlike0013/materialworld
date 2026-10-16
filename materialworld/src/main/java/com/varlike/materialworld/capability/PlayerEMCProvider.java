package com.varlike.materialworld.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerEMCProvider
        implements ICapabilityProvider<Entity, Void, PlayerEMC> {

    private final Map<UUID, PlayerEMC> dataMap = new ConcurrentHashMap<>();

    @Override
    @Nullable
    public PlayerEMC getCapability(Entity entity, Void context) {
        if (entity == null) return null;
        return dataMap.computeIfAbsent(entity.getUUID(), id -> new PlayerEMC());
    }

    /** 导出指定玩家的数据为 NBT。 */
    public CompoundTag serializeNBT(UUID uuid) {
        PlayerEMC data = dataMap.get(uuid);
        return data == null ? new CompoundTag() : data.save();
    }

    /** 从 NBT 恢复指定玩家的数据。 */
    public void deserializeNBT(UUID uuid, CompoundTag nbt) {
        if (nbt == null || nbt.isEmpty()) return;
        dataMap.computeIfAbsent(uuid, id -> new PlayerEMC()).load(nbt);
    }

    /** 死亡重生等场景下，把旧 UUID 的数据复制到新 UUID。 */
    public void copyFrom(UUID oldUuid, UUID newUuid) {
        PlayerEMC oldData = dataMap.get(oldUuid);
        if (oldData == null) return;
        PlayerEMC newData = dataMap.computeIfAbsent(newUuid, id -> new PlayerEMC());
        newData.copyFrom(oldData);
    }

    /** 玩家退出时清理内存。 */
    public void remove(UUID uuid) {
        dataMap.remove(uuid);
    }
}
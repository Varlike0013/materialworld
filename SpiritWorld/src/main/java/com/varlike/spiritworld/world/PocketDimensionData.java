package com.varlike.spiritworld.world;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PocketDimensionData extends SavedData {

    // 持久化数据：记录玩家 UUID 对应的领地中心
    private final Map<UUID, BlockPos> playerCenters = new HashMap<>();
    // 记录已经被占用的中心点
    private final Set<BlockPos> occupiedCenters = new HashSet<>();
    // 待处理的领地访问请求：目标玩家 UUID -> (访客 UUID -> 发送时间毫秒)
    private final Map<UUID, Map<UUID, Long>> visitRequests = new HashMap<>();

    public PocketDimensionData() {}

    // ==================== 核心读写 ====================

    public Map<UUID, BlockPos> getPlayerCenters() {
        return playerCenters;
    }

    public Set<BlockPos> getOccupiedCenters() {
        return occupiedCenters;
    }

    public Map<UUID, Map<UUID, Long>> getVisitRequests() {
        return visitRequests;
    }

    // ==================== 序列化与反序列化 ====================

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        // 序列化已占用的中心点
        ListTag occupiedList = new ListTag();
        for (BlockPos pos : occupiedCenters) {
            CompoundTag pTag = new CompoundTag();
            pTag.putLong("pos", pos.asLong());
            occupiedList.add(pTag);
        }
        tag.put("OccupiedCenters", occupiedList);

        // 序列化玩家映射
        ListTag playerList = new ListTag();
        for (Map.Entry<UUID, BlockPos> entry : playerCenters.entrySet()) {
            CompoundTag pTag = new CompoundTag();
            pTag.putUUID("uuid", entry.getKey());
            pTag.putLong("pos", entry.getValue().asLong());
            playerList.add(pTag);
        }
        tag.put("PlayerCenters", playerList);

        // 序列化访问请求
        ListTag requestList = new ListTag();
        for (Map.Entry<UUID, Map<UUID, Long>> targetEntry : visitRequests.entrySet()) {
            CompoundTag targetTag = new CompoundTag();
            targetTag.putUUID("target", targetEntry.getKey());

            ListTag senderList = new ListTag();
            for (Map.Entry<UUID, Long> senderEntry : targetEntry.getValue().entrySet()) {
                CompoundTag senderTag = new CompoundTag();
                senderTag.putUUID("sender", senderEntry.getKey());
                senderTag.putLong("time", senderEntry.getValue());
                senderList.add(senderTag);
            }
            targetTag.put("senders", senderList);
            requestList.add(targetTag);
        }
        tag.put("VisitRequests", requestList);

        return tag;
    }

    public static PocketDimensionData load(CompoundTag tag, HolderLookup.Provider registries) {
        PocketDimensionData data = new PocketDimensionData();

        ListTag occupiedList = tag.getList("OccupiedCenters", Tag.TAG_COMPOUND);
        for (int i = 0; i < occupiedList.size(); i++) {
            data.occupiedCenters.add(BlockPos.of(occupiedList.getCompound(i).getLong("pos")));
        }

        ListTag playerList = tag.getList("PlayerCenters", Tag.TAG_COMPOUND);
        for (int i = 0; i < playerList.size(); i++) {
            CompoundTag pTag = playerList.getCompound(i);
            data.playerCenters.put(pTag.getUUID("uuid"), BlockPos.of(pTag.getLong("pos")));
        }

        ListTag requestList = tag.getList("VisitRequests", Tag.TAG_COMPOUND);
        for (int i = 0; i < requestList.size(); i++) {
            CompoundTag targetTag = requestList.getCompound(i);
            Map<UUID, Long> senders = new HashMap<>();
            ListTag senderList = targetTag.getList("senders", Tag.TAG_COMPOUND);
            for (int j = 0; j < senderList.size(); j++) {
                CompoundTag senderTag = senderList.getCompound(j);
                senders.put(senderTag.getUUID("sender"), senderTag.getLong("time"));
            }
            data.visitRequests.put(targetTag.getUUID("target"), senders);
        }

        return data;
    }

    // ==================== 获取实例的静态方法 ====================

    public static PocketDimensionData get(MinecraftServer server) {
        // 将数据挂在主世界的 dataStorage 下，确保全局可见
        return server.overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(
                        PocketDimensionData::new,
                        PocketDimensionData::load
                ),
                "spiritworld_pocket_dimensions"
        );
    }
}
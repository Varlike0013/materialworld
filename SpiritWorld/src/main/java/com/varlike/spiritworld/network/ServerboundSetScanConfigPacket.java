package com.varlike.spiritworld.network;

import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.capability.ModCapabilities;
import com.varlike.spiritworld.capability.PlayerSpirit;
import com.varlike.spiritworld.skill.ModSkills;
import com.varlike.spiritworld.skill.SkillValues;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashSet;
import java.util.Set;

public record ServerboundSetScanConfigPacket(int range, Set<ResourceLocation> ores)
        implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ServerboundSetScanConfigPacket> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(SpiritWorld.MOD_ID, "set_scan_config"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundSetScanConfigPacket> STREAM_CODEC =
            StreamCodec.ofMember(ServerboundSetScanConfigPacket::encode, ServerboundSetScanConfigPacket::new);

    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(range);
        buf.writeVarInt(ores.size());
        for (ResourceLocation id : ores) {
            buf.writeResourceLocation(id);
        }
    }

    public ServerboundSetScanConfigPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readVarInt(), readOres(buf));
    }

    private static Set<ResourceLocation> readOres(RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Set<ResourceLocation> set = new HashSet<>();
        for (int i = 0; i < size; i++) set.add(buf.readResourceLocation());
        return set;
    }

    public static void handle(ServerboundSetScanConfigPacket pkt, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            PlayerSpirit data = player.getCapability(ModCapabilities.PLAYER_SPIRIT, null);
            if (data == null) return;

            // 校验等级
            int level = data.getSkillLevel(ModSkills.SPIRIT_SCAN_ID);
            if (level <= 0) return;   // 未学，忽略

            // 校验范围
            int maxRange = SkillValues.scanRangeMax(level);
            int range = Math.max(SkillValues.SCAN_RANGE_MIN,
                    Math.min(pkt.range(), maxRange));
            range = (range - SkillValues.SCAN_RANGE_MIN)
                    / SkillValues.SCAN_RANGE_STEP
                    * SkillValues.SCAN_RANGE_STEP
                    + SkillValues.SCAN_RANGE_MIN;   // 对齐到步长

            // 校验矿物（只允许已解锁的）
            Set<ResourceLocation> unlocked = SkillValues.unlockedOres(level);
            Set<ResourceLocation> sanitized = new HashSet<>();
            for (ResourceLocation id : pkt.ores()) {
                if (unlocked.contains(id)) sanitized.add(id);
            }

            data.setScanRange(range);
            data.setScanOres(sanitized);
            data.markDirty();
            NetworkHandler.syncTo(player);
        });
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
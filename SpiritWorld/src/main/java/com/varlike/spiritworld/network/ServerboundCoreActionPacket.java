package com.varlike.spiritworld.network;

import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.block.SpiritCoreBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundCoreActionPacket(BlockPos pos, int action) implements CustomPacketPayload {

    public static final Type<ServerboundCoreActionPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(SpiritWorld.MOD_ID, "core_action")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundCoreActionPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ServerboundCoreActionPacket::pos,
            ByteBufCodecs.VAR_INT, ServerboundCoreActionPacket::action,
            ServerboundCoreActionPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ServerboundCoreActionPacket pkt, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            // 距离与加载校验：防止伪造包远程操作任意坐标的核心
            var pos = pkt.pos();
            if (player.level() == null || !player.level().isLoaded(pos)) return;
            double dx = pos.getX() + 0.5 - player.getX();
            double dy = pos.getY() + 0.5 - player.getY();
            double dz = pos.getZ() + 0.5 - player.getZ();
            if (dx * dx + dy * dy + dz * dz > 64.0) return;

            if (player.level().getBlockEntity(pos) instanceof SpiritCoreBlockEntity core) {
                core.adjustTarget(pkt.action());
            }
        });
    }
}
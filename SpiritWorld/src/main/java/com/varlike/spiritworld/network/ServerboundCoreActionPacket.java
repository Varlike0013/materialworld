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

            // 检查该坐标的方块实体是否是魔力核心
            if (player.level().getBlockEntity(pkt.pos()) instanceof SpiritCoreBlockEntity core) {
                // 调用核心的处理逻辑
                core.adjustTarget(pkt.action());
            }
        });
    }
}
package com.varlike.spiritworld.network;

import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.capability.ModCapabilities;
import com.varlike.spiritworld.capability.PlayerSpirit;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundSetSelectedSlotPacket(int slot) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ServerboundSetSelectedSlotPacket> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(SpiritWorld.MOD_ID, "set_selected_slot"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundSetSelectedSlotPacket> STREAM_CODEC =
            StreamCodec.ofMember(ServerboundSetSelectedSlotPacket::encode, ServerboundSetSelectedSlotPacket::new);

    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(slot);
    }

    public ServerboundSetSelectedSlotPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readVarInt());
    }

    public static void handle(ServerboundSetSelectedSlotPacket pkt, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            PlayerSpirit data = player.getCapability(ModCapabilities.PLAYER_SPIRIT);
            if (data == null) return;
            data.setSelectedSlot(pkt.slot());
            NetworkHandler.syncTo(player);
        });
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
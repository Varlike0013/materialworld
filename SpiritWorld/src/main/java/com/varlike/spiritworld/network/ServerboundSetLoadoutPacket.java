package com.varlike.spiritworld.network;

import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.capability.ModCapabilities;
import com.varlike.spiritworld.capability.PlayerSpirit;
import com.varlike.spiritworld.skill.SkillNode;
import com.varlike.spiritworld.skill.SkillTree;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundSetLoadoutPacket(int slot, ResourceLocation skillId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ServerboundSetLoadoutPacket> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(SpiritWorld.MOD_ID, "set_loadout"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundSetLoadoutPacket> STREAM_CODEC =
            StreamCodec.ofMember(ServerboundSetLoadoutPacket::encode, ServerboundSetLoadoutPacket::new);

    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(slot);
        buf.writeBoolean(skillId != null);
        if (skillId != null) buf.writeResourceLocation(skillId);
    }

    public ServerboundSetLoadoutPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readBoolean() ? buf.readResourceLocation() : null);
    }

    public static void handle(ServerboundSetLoadoutPacket pkt, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            PlayerSpirit data = player.getCapability(ModCapabilities.PLAYER_SPIRIT);
            if (data == null) return;
            if (pkt.slot() < 0 || pkt.slot() >= PlayerSpirit.LOADOUT_SIZE) return;

            if (pkt.skillId() == null) {
                data.setLoadoutSlot(pkt.slot(), null);
                NetworkHandler.syncTo(player);
                return;
            }

            // 校验技能可装备且已学
            SkillNode node = SkillTree.get(pkt.skillId());
            if (node == null || !node.isEquippable()) return;
            if (data.getSkillLevel(pkt.skillId()) <= 0) return;

            data.setLoadoutSlot(pkt.slot(), pkt.skillId());
            NetworkHandler.syncTo(player);
        });
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
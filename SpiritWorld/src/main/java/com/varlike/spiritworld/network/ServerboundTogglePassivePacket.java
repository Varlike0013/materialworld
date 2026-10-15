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

public record ServerboundTogglePassivePacket(ResourceLocation skillId, boolean enabled)
        implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ServerboundTogglePassivePacket> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(SpiritWorld.MOD_ID, "toggle_passive"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundTogglePassivePacket> STREAM_CODEC =
            StreamCodec.ofMember(ServerboundTogglePassivePacket::encode, ServerboundTogglePassivePacket::new);

    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeResourceLocation(skillId);
        buf.writeBoolean(enabled);
    }

    public ServerboundTogglePassivePacket(RegistryFriendlyByteBuf buf) {
        this(buf.readResourceLocation(), buf.readBoolean());
    }

    public static void handle(ServerboundTogglePassivePacket pkt, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            PlayerSpirit data = player.getCapability(ModCapabilities.PLAYER_SPIRIT);
            if (data == null) return;

            SkillNode node = SkillTree.get(pkt.skillId());
            if (node == null || !node.isToggleable()) return;
            if (data.getSkillLevel(pkt.skillId()) <= 0) return;

            if (pkt.enabled()) data.addPassive(pkt.skillId());
            else data.removePassive(pkt.skillId());

            NetworkHandler.syncTo(player);
        });
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
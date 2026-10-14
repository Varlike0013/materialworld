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

public record ServerboundLearnSkillPacket(ResourceLocation skillId) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ServerboundLearnSkillPacket> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(SpiritWorld.MOD_ID, "learn_skill"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundLearnSkillPacket> STREAM_CODEC =
            StreamCodec.ofMember(ServerboundLearnSkillPacket::encode, ServerboundLearnSkillPacket::new);

    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeResourceLocation(skillId);
    }

    public ServerboundLearnSkillPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readResourceLocation());
    }

    public static void handle(ServerboundLearnSkillPacket pkt, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            SkillNode node = SkillTree.get(pkt.skillId());
            if (node == null) return;

            PlayerSpirit data = player.getCapability(ModCapabilities.PLAYER_SPIRIT);
            if (data == null) return;

            // 已达上限
            int currentLevel = data.getSkillLevel(node.getId());
            if (currentLevel >= node.getMaxLevel()) return;

            // 前置未满足
            for (ResourceLocation prereq : node.getPrerequisites()) {
                if (data.getSkillLevel(prereq) <= 0) return;
            }

            // 灵视等级不足
            if (data.getVisionLevel() < node.getRequiredVisionLevel()) return;

            // 灵视点不足
            if (!data.consumeVisionPoints(node.getCostPerLevel())) return;

            // 应用
            data.setSkillLevel(node.getId(), currentLevel + 1);
            data.addSpentVisionPoints(node.getCostPerLevel());

            NetworkHandler.syncTo(player);
        });
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
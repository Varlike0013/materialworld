package com.varlike.spiritworld.network;

import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.capability.ModCapabilities;
import com.varlike.spiritworld.capability.PlayerSpirit;
import com.varlike.spiritworld.skill.ModSkills;
import com.varlike.spiritworld.skill.SkillNode;
import com.varlike.spiritworld.skill.SkillTree;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundUseSkillPacket() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ServerboundUseSkillPacket> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(SpiritWorld.MOD_ID, "use_skill"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundUseSkillPacket> STREAM_CODEC =
            StreamCodec.ofMember(ServerboundUseSkillPacket::encode, ServerboundUseSkillPacket::new);

    public void encode(RegistryFriendlyByteBuf buf) {}

    public ServerboundUseSkillPacket(RegistryFriendlyByteBuf buf) { this(); }

    public static void handle(ServerboundUseSkillPacket pkt, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            PlayerSpirit data = player.getCapability(ModCapabilities.PLAYER_SPIRIT);
            if (data == null) return;

            ResourceLocation id = data.getEquippedSkill();
            if (id == null) return;

            SkillNode node = SkillTree.get(id);
            if (node == null) return;
            if (data.getSkillLevel(id) <= 0) return;

            if (node.isToggleable()) {
                // 切换被动：开 -> 关，关 -> 开
                if (data.isPassiveActive(id)) {
                    data.removePassive(id);
                } else {
                    data.addPassive(id);
                }
                NetworkHandler.syncTo(player);
            } else {
                if (id.equals(ModSkills.SUMMON_TOOL_ID)) {
                    // 触发召唤
                    ServerboundSummonToolPacket.handle(new ServerboundSummonToolPacket(), context);
                    return;
                }
            }
        });
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
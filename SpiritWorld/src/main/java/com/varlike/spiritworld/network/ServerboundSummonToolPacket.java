package com.varlike.spiritworld.network;

import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.capability.ModCapabilities;
import com.varlike.spiritworld.capability.PlayerSpirit;
import com.varlike.spiritworld.registry.ModItems;
import com.varlike.spiritworld.skill.ModSkills;
import com.varlike.spiritworld.skill.SpiritToolBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundSummonToolPacket() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ServerboundSummonToolPacket> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(SpiritWorld.MOD_ID, "summon_tool"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundSummonToolPacket> STREAM_CODEC =
            StreamCodec.ofMember(ServerboundSummonToolPacket::encode, ServerboundSummonToolPacket::new);

    public void encode(RegistryFriendlyByteBuf buf) {}

    public ServerboundSummonToolPacket(RegistryFriendlyByteBuf buf) { this(); }

    public static void handle(ServerboundSummonToolPacket pkt, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            PlayerSpirit data = player.getCapability(ModCapabilities.PLAYER_SPIRIT);
            if (data == null) return;

            // 1. 检查技能
            if (data.getSkillLevel(ModSkills.SUMMON_TOOL_ID) <= 0) return;

            // 2. 检查背包是否已有工具
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                if (player.getInventory().getItem(i).is(ModItems.SPIRIT_TOOL.get())) {
                    player.sendSystemMessage(Component.translatable(
                            "message.spiritworld.spirit_tool.already_have"));
                    return;
                }
            }

            // 3. 检查精神力
            final long COST = 10L;
            if (data.getSpirit() < COST) {
                player.sendSystemMessage(Component.translatable(
                        "message.spiritworld.spirit_tool.no_spirit"));
                return;
            }

            // 4. 找空位
            int emptySlot = -1;
            for (int i = 0; i < 36; i++) {
                if (player.getInventory().getItem(i).isEmpty()) { emptySlot = i; break; }
            }
            if (emptySlot < 0) {
                player.sendSystemMessage(Component.translatable(
                        "message.spiritworld.spirit_tool.no_space"));
                return;
            }

            // 5. 消耗 + 生成
            data.consumeSpirit(COST);
            ItemStack tool = SpiritToolBuilder.build(player, data);
            player.getInventory().setItem(emptySlot, tool);
            data.setSpiritToolActive(true);

            NetworkHandler.syncTo(player);
            player.sendSystemMessage(Component.translatable("message.spiritworld.spirit_tool.summoned"));
        });
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
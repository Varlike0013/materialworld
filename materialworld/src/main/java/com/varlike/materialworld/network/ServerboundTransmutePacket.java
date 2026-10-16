package com.varlike.materialworld.network;

import com.varlike.materialworld.MaterialWorld;
import com.varlike.materialworld.api.event.EMCChangeEvent;
import com.varlike.materialworld.capability.ModCapabilities;
import com.varlike.materialworld.capability.PlayerEMC;
import com.varlike.materialworld.emc.EMCManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ServerboundTransmutePacket(Item item) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ServerboundTransmutePacket> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(MaterialWorld.MOD_ID, "transmute"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundTransmutePacket> STREAM_CODEC =
            StreamCodec.ofMember(ServerboundTransmutePacket::encode, ServerboundTransmutePacket::new);

    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(item));
    }

    public ServerboundTransmutePacket(RegistryFriendlyByteBuf buf) {
        this(BuiltInRegistries.ITEM.get(buf.readResourceLocation()));
    }

    public static void handle(ServerboundTransmutePacket pkt, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            Item item = pkt.item();
            if (item == null || item == Items.AIR) return;

            long cost = EMCManager.getEMC(item);
            if (cost <= 0) {
                player.sendSystemMessage(Component.translatable("message.materialworld.no_emc"));
                return;
            }

            PlayerEMC data = player.getCapability(ModCapabilities.PLAYER_EMC);
            if (data == null) return;

            if (!data.isLearned(item)) {
                player.sendSystemMessage(Component.translatable("message.materialworld.not_learned"));
                return;
            }

            long oldBalance = data.getEmcBalance();

            if (!data.consumeEmc(cost)) {
                player.sendSystemMessage(Component.translatable(
                        "message.materialworld.emc_insufficient",
                        cost, oldBalance));
                return;
            }

            NeoForge.EVENT_BUS.post(new EMCChangeEvent(player, oldBalance, data.getEmcBalance()));

            ItemStack stack = new ItemStack(item);
            if (!player.getInventory().add(stack)) player.drop(stack, false);
            NetworkHandler.syncTo(player);
        });
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
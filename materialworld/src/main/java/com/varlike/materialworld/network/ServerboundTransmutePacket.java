package com.varlike.materialworld.network;

import com.varlike.materialworld.capability.ModCapabilities;
import com.varlike.materialworld.emc.EMCManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class ServerboundTransmutePacket {
    private final Item item;

    public ServerboundTransmutePacket(Item item) { this.item = item; }

    public static void encode(ServerboundTransmutePacket pkt, FriendlyByteBuf buf) {
        buf.writeResourceLocation(ForgeRegistries.ITEMS.getKey(pkt.item));
    }

    public static ServerboundTransmutePacket decode(FriendlyByteBuf buf) {
        return new ServerboundTransmutePacket(ForgeRegistries.ITEMS.getValue(buf.readResourceLocation()));
    }

    public static void handle(ServerboundTransmutePacket pkt, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null || pkt.item == null) return;

            long cost = EMCManager.getEMC(pkt.item);
            if (cost <= 0) {
                player.sendSystemMessage(Component.literal("§c该物品没有 EMC 值"));
                return;
            }
            player.getCapability(ModCapabilities.PLAYER_EMC).ifPresent(data -> {
                if (!data.isLearned(pkt.item)) {
                    player.sendSystemMessage(Component.literal("§c尚未学会这个物品"));
                    return;
                }
                if (!data.consumeEmc(cost)) {
                    player.sendSystemMessage(Component.literal(
                            "§cEMC 不足，需要 " + cost + "，当前 " + data.getEmcBalance()));
                    return;
                }
                ItemStack stack = new ItemStack(pkt.item);
                if (!player.getInventory().add(stack)) player.drop(stack, false);
                NetworkHandler.syncTo(player);
            });
        });
        ctx.setPacketHandled(true);
    }
}
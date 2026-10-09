package com.varlike.materialworld.network;

import com.varlike.materialworld.capability.ModCapabilities;
import com.varlike.materialworld.emc.EMCManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerboundRecyclePacket {
    private final int inventorySlot;

    public ServerboundRecyclePacket(int inventorySlot) {
        this.inventorySlot = inventorySlot;
    }

    public static void encode(ServerboundRecyclePacket pkt, FriendlyByteBuf buf) {
        buf.writeVarInt(pkt.inventorySlot);
    }

    public static ServerboundRecyclePacket decode(FriendlyByteBuf buf) {
        return new ServerboundRecyclePacket(buf.readVarInt());
    }

    public static void handle(ServerboundRecyclePacket pkt, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;
            if (pkt.inventorySlot < 0 || pkt.inventorySlot >= player.getInventory().getContainerSize()) return;

            ItemStack stack = player.getInventory().getItem(pkt.inventorySlot);
            if (stack.isEmpty()) return;

            Item item = stack.getItem();
            long unit = EMCManager.getEMC(item);
            if (unit <= 0) {
                player.sendSystemMessage(Component.literal("§c该物品没有 EMC 值，无法回收"));
                return;
            }

            int count = stack.getCount();
            long total = unit * count;
            String name = stack.getHoverName().getString();

            player.getCapability(ModCapabilities.PLAYER_EMC).ifPresent(data -> {
                boolean newLearn = !data.isLearned(item);
                data.learn(item);
                data.addEmc(total);

                // 清空该格子
                stack.setCount(0);

                String msg = newLearn
                        ? "§a新解锁 §e" + name + " §a+" + total + " EMC"
                        : "§a回收 §e" + count + "x " + name + " §a+" + total + " EMC";
                player.sendSystemMessage(Component.literal(msg));

                NetworkHandler.syncTo(player);
            });
        });
        ctx.setPacketHandled(true);
    }
}
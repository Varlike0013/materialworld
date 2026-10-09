package com.varlike.materialworld.network;

import com.varlike.materialworld.capability.ModCapabilities;
import com.varlike.materialworld.emc.EMCManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ServerboundEMCCraftPacket {

    /** key = 合成网格索引 0..8；value = 要放进该槽的物品 */
    private final Map<Integer, ItemStack> slotItems;

    public ServerboundEMCCraftPacket(Map<Integer, ItemStack> slotItems) {
        this.slotItems = new LinkedHashMap<>(slotItems);
    }

    public static void encode(ServerboundEMCCraftPacket pkt, FriendlyByteBuf buf) {
        buf.writeVarInt(pkt.slotItems.size());
        for (Map.Entry<Integer, ItemStack> e : pkt.slotItems.entrySet()) {
            buf.writeVarInt(e.getKey());
            buf.writeItem(e.getValue());
        }
    }

    public static ServerboundEMCCraftPacket decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<Integer, ItemStack> map = new LinkedHashMap<>();
        for (int i = 0; i < size; i++) {
            int slot = buf.readVarInt();
            ItemStack stack = buf.readItem();
            map.put(slot, stack);
        }
        return new ServerboundEMCCraftPacket(map);
    }

    public static void handle(ServerboundEMCCraftPacket pkt, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;
            if (!(player.containerMenu instanceof CraftingMenu menu)) return;

            player.getCapability(ModCapabilities.PLAYER_EMC).ifPresent(data -> {
                // 1. 校验 + 计算总花费
                long totalCost = 0;
                for (Map.Entry<Integer, ItemStack> e : pkt.slotItems.entrySet()) {
                    ItemStack stack = e.getValue();
                    if (stack.isEmpty()) continue;
                    long unit = EMCManager.getEMC(stack.getItem());
                    if (unit <= 0 || !data.isLearned(stack.getItem())) {
                        player.sendSystemMessage(Component.literal("§c无法用 EMC 合成："
                                + stack.getHoverName().getString()));
                        return;
                    }
                    totalCost += unit * stack.getCount();
                }

                if (data.getEmcBalance() < totalCost) {
                    player.sendSystemMessage(Component.literal("§cEMC 不足，需要 " + totalCost));
                    return;
                }

                // 2. 扣除 EMC
                data.consumeEmc(totalCost);

                // 3. 按槽位放置（合成网格 slot 1..9 对应索引 0..8）
                for (Map.Entry<Integer, ItemStack> e : pkt.slotItems.entrySet()) {
                    int gridIndex = e.getKey();
                    if (gridIndex < 0 || gridIndex > 8) continue;
                    int menuSlot = gridIndex + 1; // slot 0 是输出槽
                    ItemStack stack = e.getValue().copy();
                    menu.getSlot(menuSlot).set(stack);
                    menu.getSlot(menuSlot).setChanged();
                }
                menu.broadcastChanges();

                NetworkHandler.syncTo(player);
            });
        });
        ctx.setPacketHandled(true);
    }
}
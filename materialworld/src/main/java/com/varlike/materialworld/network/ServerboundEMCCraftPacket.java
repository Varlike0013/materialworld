package com.varlike.materialworld.network;

import com.varlike.materialworld.MaterialWorld;
import com.varlike.materialworld.capability.ModCapabilities;
import com.varlike.materialworld.capability.PlayerEMC;
import com.varlike.materialworld.emc.EMCManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.LinkedHashMap;
import java.util.Map;

public record ServerboundEMCCraftPacket(Map<Integer, ItemStack> slotItems)
        implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ServerboundEMCCraftPacket> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(MaterialWorld.MOD_ID, "emc_craft"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundEMCCraftPacket> STREAM_CODEC =
            StreamCodec.ofMember(ServerboundEMCCraftPacket::encode, ServerboundEMCCraftPacket::new);

    public ServerboundEMCCraftPacket(Map<Integer, ItemStack> slotItems) {
        this.slotItems = new LinkedHashMap<>(slotItems);
    }

    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(slotItems.size());
        for (Map.Entry<Integer, ItemStack> e : slotItems.entrySet()) {
            buf.writeVarInt(e.getKey());
            ItemStack.STREAM_CODEC.encode(buf, e.getValue());
        }
    }

    public ServerboundEMCCraftPacket(RegistryFriendlyByteBuf buf) {
        this(readMap(buf));
    }

    private static Map<Integer, ItemStack> readMap(RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<Integer, ItemStack> map = new LinkedHashMap<>();
        for (int i = 0; i < size; i++) {
            int slot = buf.readVarInt();
            ItemStack stack = ItemStack.STREAM_CODEC.decode(buf);
            map.put(slot, stack);
        }
        return map;
    }

    public static void handle(ServerboundEMCCraftPacket pkt, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (!(player.containerMenu instanceof CraftingMenu menu)) return;

            PlayerEMC data = player.getCapability(ModCapabilities.PLAYER_EMC);
            if (data == null) return;

            // ===== 1. 校验 + 计算总花费 =====
            long totalCost = 0;
            for (Map.Entry<Integer, ItemStack> e : pkt.slotItems().entrySet()) {
                ItemStack stack = e.getValue();
                if (stack.isEmpty()) continue;
                long unit = EMCManager.getEMC(stack.getItem());
                if (unit <= 0 || !data.isLearned(stack.getItem())) {
                    player.sendSystemMessage(Component.translatable(
                            "message.materialworld.cannot_craft",
                            stack.getHoverName()));
                    return;
                }
                totalCost += unit * stack.getCount();
            }

            if (data.getEmcBalance() < totalCost) {
                player.sendSystemMessage(Component.translatable(
                        "message.materialworld.emc_insufficient_short", totalCost));
                return;
            }

            // ===== 2. 扣除 EMC =====
            data.consumeEmc(totalCost);

            // ===== 3. 按槽位放置到合成网格 =====
            for (Map.Entry<Integer, ItemStack> e : pkt.slotItems().entrySet()) {
                int gridIndex = e.getKey();
                if (gridIndex < 0 || gridIndex > 8) continue;
                int menuSlot = gridIndex + 1; // slot 0 是输出槽
                ItemStack stack = e.getValue().copy();
                menu.getSlot(menuSlot).set(stack);
                menu.getSlot(menuSlot).setChanged();
            }
            menu.broadcastChanges();

            // ===== 4. 同步最新 EMC 数据给客户端 =====
            NetworkHandler.syncTo(player);
        });
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
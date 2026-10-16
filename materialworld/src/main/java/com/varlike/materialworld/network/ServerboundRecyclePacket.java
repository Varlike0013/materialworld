package com.varlike.materialworld.network;

import com.varlike.materialworld.MaterialWorld;
import com.varlike.materialworld.api.event.EMCChangeEvent;
import com.varlike.materialworld.api.event.ItemLearnedEvent;
import com.varlike.materialworld.capability.ModCapabilities;
import com.varlike.materialworld.capability.PlayerEMC;
import com.varlike.materialworld.emc.EMCManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record ServerboundRecyclePacket(int inventorySlot) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ServerboundRecyclePacket> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(MaterialWorld.MOD_ID, "recycle"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundRecyclePacket> STREAM_CODEC =
            StreamCodec.ofMember(ServerboundRecyclePacket::encode, ServerboundRecyclePacket::new);

    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(inventorySlot);
    }

    public ServerboundRecyclePacket(RegistryFriendlyByteBuf buf) {
        this(buf.readVarInt());
    }

    public static void handle(ServerboundRecyclePacket pkt, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            if (pkt.inventorySlot() < 0
                    || pkt.inventorySlot() >= player.getInventory().getContainerSize()) return;

            ItemStack stack = player.getInventory().getItem(pkt.inventorySlot());
            if (stack.isEmpty()) return;

            Item item = stack.getItem();
            long unit = EMCManager.getEMC(item);
            if (unit <= 0) {
                player.sendSystemMessage(Component.translatable("message.materialworld.cannot_recycle"));
                return;
            }

            PlayerEMC data = player.getCapability(ModCapabilities.PLAYER_EMC);
            if (data == null) return;

            int count = stack.getCount();
            long total = unit * count;
            String name = stack.getHoverName().getString();

            // 1. 先记录加款前余额
            long oldBalance = data.getEmcBalance();

            // 2. 学习
            boolean newLearn = !data.isLearned(item);
            data.learn(item);

            // 3. 加款并发布 EMCChangeEvent
            data.addEmc(total);
            NeoForge.EVENT_BUS.post(new EMCChangeEvent(player, oldBalance, data.getEmcBalance()));

            // 4. 首次学习时发布 ItemLearnedEvent
            if (newLearn) {
                NeoForge.EVENT_BUS.post(new ItemLearnedEvent(player, item));
            }

            // 5. 清空原格子
            stack.setCount(0);

            Component msg = newLearn
                    ? Component.translatable("message.materialworld.new_learn", name, total)
                    : Component.translatable("message.materialworld.recycled", count, name, total);
            player.sendSystemMessage(msg);

            NetworkHandler.syncTo(player);
        });
    }

    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
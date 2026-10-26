package com.varlike.spiritworld.network;

import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.client.ClientScanData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record ClientboundScanResultPacket(List<OreIcon> icons, int durationSeconds)
        implements CustomPacketPayload {

    /** 单个矿物图标：质心 + 图标物品 + 块数 */
    public record OreIcon(double x, double y, double z, Item iconItem, int count) {}

    public static final CustomPacketPayload.Type<ClientboundScanResultPacket> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(SpiritWorld.MOD_ID, "scan_result"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundScanResultPacket> STREAM_CODEC =
            StreamCodec.ofMember(ClientboundScanResultPacket::encode, ClientboundScanResultPacket::new);

    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(icons.size());
        for (OreIcon icon : icons) {
            buf.writeDouble(icon.x());
            buf.writeDouble(icon.y());
            buf.writeDouble(icon.z());
            buf.writeResourceLocation(BuiltInRegistries.ITEM.getKey(icon.iconItem()));
            buf.writeVarInt(icon.count());
        }
        buf.writeVarInt(durationSeconds);
    }

    public ClientboundScanResultPacket(RegistryFriendlyByteBuf buf) {
        this(readIcons(buf), buf.readVarInt());
    }

    private static List<OreIcon> readIcons(RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<OreIcon> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            double x = buf.readDouble();
            double y = buf.readDouble();
            double z = buf.readDouble();
            ResourceLocation itemId = buf.readResourceLocation();
            Item item = BuiltInRegistries.ITEM.get(itemId);
            int count = buf.readVarInt();
            list.add(new OreIcon(x, y, z, item, count));
        }
        return list;
    }

    public static void handle(ClientboundScanResultPacket pkt, IPayloadContext context) {
        context.enqueueWork(() -> {
            ClientScanData.setIcons(pkt.icons(), pkt.durationSeconds());
        });
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
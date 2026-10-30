package com.varlike.spiritworld.network;

import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.skill.ManaType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record ClientboundNetworkTreePacket(
        List<NodeData> nodes,
        List<PullDetail> details
) implements CustomPacketPayload {

    // 节点数据（核心、聚集器）
    public record NodeData(
            BlockPos pos,
            BlockPos parentPos,
            String type,
            double efficiency,
            long mana,
            long capacity,
            ManaType manaType
    ) {}

    // 抽水明细（核心专属）
    public record PullDetail(
            BlockPos collectorPos,
            long amount,
            ManaType type
    ) {}

    public static final Type<ClientboundNetworkTreePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(SpiritWorld.MOD_ID, "network_tree")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundNetworkTreePacket> STREAM_CODEC = StreamCodec.ofMember(
            ClientboundNetworkTreePacket::encode,
            ClientboundNetworkTreePacket::new
    );

    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeCollection(nodes, (b, n) -> {
            b.writeBlockPos(n.pos());
            b.writeBoolean(n.parentPos() != null);
            if (n.parentPos() != null) b.writeBlockPos(n.parentPos());
            b.writeUtf(n.type());
            b.writeDouble(n.efficiency());
            b.writeLong(n.mana());
            b.writeLong(n.capacity());
            b.writeUtf(n.manaType().id());
        });
        buf.writeCollection(details, (b, d) -> {
            b.writeBlockPos(d.collectorPos());
            b.writeLong(d.amount());
            b.writeUtf(d.type().id());
        });
    }

    public ClientboundNetworkTreePacket(RegistryFriendlyByteBuf buf) {
        this(
                buf.readCollection(ArrayList::new, b -> new NodeData(
                        b.readBlockPos(),
                        b.readBoolean() ? b.readBlockPos() : null,
                        b.readUtf(),
                        b.readDouble(),
                        b.readLong(),
                        b.readLong(),
                        ManaType.byId(b.readUtf())
                )),
                buf.readCollection(ArrayList::new, b -> new PullDetail(
                        b.readBlockPos(),
                        b.readLong(),
                        ManaType.byId(b.readUtf())
                ))
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(ClientboundNetworkTreePacket pkt, IPayloadContext context) {
        context.enqueueWork(() -> {
            // 如果当前客户端屏幕是网络界面，则更新数据
            if (net.minecraft.client.Minecraft.getInstance().screen instanceof com.varlike.spiritworld.client.SpiritCoreNetworkScreen screen) {
                screen.updateData(pkt.nodes(), pkt.details());
            }
        });
    }
}
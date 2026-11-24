package com.varlike.aetherui.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** TargetInfoHud / 熔炼图标演示使用的服务端查询负载。 */
public final class InspectPayloads {

    private InspectPayloads() {}

    /** 客户端 → 服务器：请求瞄准容器的统计，以及玩家附近正在燃烧的熔炉。 */
    public record Request(boolean hasTarget, BlockPos target, BlockPos origin) implements CustomPacketPayload {

        public static final Type<Request> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath("aetherui", "inspect_request"));

        public static final StreamCodec<RegistryFriendlyByteBuf, Request> CODEC =
                StreamCodec.of(Request::encode, Request::decode);

        private static void encode(RegistryFriendlyByteBuf buf, Request payload) {
            buf.writeBoolean(payload.hasTarget());
            buf.writeBlockPos(payload.target() != null ? payload.target() : BlockPos.ZERO);
            buf.writeBlockPos(payload.origin() != null ? payload.origin() : BlockPos.ZERO);
        }

        private static Request decode(RegistryFriendlyByteBuf buf) {
            boolean hasTarget = buf.readBoolean();
            BlockPos target = buf.readBlockPos();
            BlockPos origin = buf.readBlockPos();
            return new Request(hasTarget, target, origin);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** 服务器 → 客户端：一次查询的完整快照。 */
    public record Snapshot(ContainerInfo container, List<FurnaceInfo> furnaces) implements CustomPacketPayload {

        public static final Type<Snapshot> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath("aetherui", "inspect_snapshot"));

        public static final StreamCodec<RegistryFriendlyByteBuf, Snapshot> CODEC =
                StreamCodec.of(Snapshot::encode, Snapshot::decode);

        public Snapshot {
            furnaces = furnaces == null ? List.of() : List.copyOf(furnaces);
        }

        private static void encode(RegistryFriendlyByteBuf buf, Snapshot payload) {
            ContainerInfo container = payload.container();
            buf.writeBoolean(container != null);
            if (container != null) {
                buf.writeBlockPos(container.pos());
                buf.writeVarInt(container.used());
                buf.writeVarInt(container.total());
            }

            List<FurnaceInfo> furnaces = payload.furnaces();
            buf.writeInt(furnaces.size());
            for (FurnaceInfo furnace : furnaces) {
                buf.writeBlockPos(furnace.pos());
                buf.writeBoolean(furnace.lit());
                ItemStack stack = furnace.output();
                ItemStack.STREAM_CODEC.encode(buf, stack != null ? stack : ItemStack.EMPTY);
            }
        }

        private static Snapshot decode(RegistryFriendlyByteBuf buf) {
            ContainerInfo container = buf.readBoolean()
                    ? new ContainerInfo(buf.readBlockPos(), buf.readVarInt(), buf.readVarInt())
                    : null;

            int count = Math.max(0, buf.readInt());
            List<FurnaceInfo> furnaces = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                BlockPos pos = buf.readBlockPos();
                boolean lit = buf.readBoolean();
                ItemStack output = ItemStack.STREAM_CODEC.decode(buf);
                furnaces.add(new FurnaceInfo(pos, output, lit));
            }
            return new Snapshot(container, furnaces);
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** 容器统计；大箱子会由服务器合并两半后返回。 */
    public record ContainerInfo(BlockPos pos, int used, int total) {}

    /** 附近熔炉快照；output 是产物槽中的物品副本。 */
    public record FurnaceInfo(BlockPos pos, ItemStack output, boolean lit) {}
}

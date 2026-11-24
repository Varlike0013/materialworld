package com.varlike.aetherui.network;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.List;

/** 自定义网络：把容器与熔炉的权威数据从服务端带到客户端 UI。 */
public final class AetherUINetwork {

    private static final int FURNACE_RADIUS = 10;
    private static final int FURNACE_MIN_DY = -4;
    private static final int FURNACE_MAX_DY = 8;
    private static final int MAX_FURNACES = 64;

    private AetherUINetwork() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1")
                .playToServer(InspectPayloads.Request.TYPE, InspectPayloads.Request.CODEC, AetherUINetwork::handleRequest)
                .playToClient(InspectPayloads.Snapshot.TYPE, InspectPayloads.Snapshot.CODEC,
                        (payload, context) -> InspectCache.apply(payload));
    }

    private static void handleRequest(InspectPayloads.Request payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) return;
        ServerLevel level = player.serverLevel();

        InspectPayloads.ContainerInfo container = null;
        if (payload.hasTarget()) {
            container = inspectContainer(level, payload.target());
        }

        BlockPos origin = payload.origin() != null ? payload.origin() : player.blockPosition();
        List<InspectPayloads.FurnaceInfo> furnaces = inspectFurnaces(level, origin);

        context.reply(new InspectPayloads.Snapshot(container, furnaces));
    }

    private static InspectPayloads.ContainerInfo inspectContainer(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof Container container)) return null;

        Container target = container;
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof ChestBlock chest) {
            Container combined = ChestBlock.getContainer(chest, state, level, pos, true);
            if (combined != null) target = combined;
        }

        int total = target.getContainerSize();
        int used = 0;
        for (int i = 0; i < total; i++) {
            if (!target.getItem(i).isEmpty()) used++;
        }
        return new InspectPayloads.ContainerInfo(pos.immutable(), used, total);
    }

    private static List<InspectPayloads.FurnaceInfo> inspectFurnaces(Level level, BlockPos origin) {
        List<InspectPayloads.FurnaceInfo> result = new ArrayList<>();
        for (int dx = -FURNACE_RADIUS; dx <= FURNACE_RADIUS; dx++) {
            for (int dz = -FURNACE_RADIUS; dz <= FURNACE_RADIUS; dz++) {
                for (int dy = FURNACE_MIN_DY; dy <= FURNACE_MAX_DY; dy++) {
                    BlockPos pos = origin.offset(dx, dy, dz);
                    BlockEntity be = level.getBlockEntity(pos);
                    if (!(be instanceof AbstractFurnaceBlockEntity furnace)) continue;

                    BlockState state = level.getBlockState(pos);
                    boolean lit = state.hasProperty(BlockStateProperties.LIT)
                            && state.getValue(BlockStateProperties.LIT);
                    if (!lit) continue;

                    var output = furnace.getItem(2);
                    if (output.isEmpty()) continue;

                    result.add(new InspectPayloads.FurnaceInfo(pos.immutable(), output.copy(), true));
                    if (result.size() >= MAX_FURNACES) return result;
                }
            }
        }
        return result;
    }
}

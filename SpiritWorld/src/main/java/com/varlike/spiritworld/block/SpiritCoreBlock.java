package com.varlike.spiritworld.block;

import com.mojang.serialization.MapCodec;
import com.varlike.spiritworld.client.SpiritCoreMenu;
import com.varlike.spiritworld.item.ManaCrystalItem;
import com.varlike.spiritworld.network.ClientboundNetworkTreePacket;
import com.varlike.spiritworld.network.EfficiencyCalculator;
import com.varlike.spiritworld.network.INetworkNode;
import com.varlike.spiritworld.network.NetworkManager;
import com.varlike.spiritworld.network.NodeType;
import com.varlike.spiritworld.network.OfflineCollectorData;
import com.varlike.spiritworld.skill.ManaType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SpiritCoreBlock extends BaseEntityBlock {

    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public SpiritCoreBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH));
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite());
    }
    @Override
    protected @NotNull BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }
    @Override
    @SuppressWarnings("deprecation")
    protected @NotNull BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(SpiritCoreBlock::new);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SpiritCoreBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return (lvl, pos, st, be) -> {
            if (be instanceof SpiritCoreBlockEntity core) {
                SpiritCoreBlockEntity.serverTick(lvl, pos, st, core);
            }
        };
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
                                              BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            if (!(stack.getItem() instanceof ManaCrystalItem)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            if (!(level.getBlockEntity(pos) instanceof SpiritCoreBlockEntity be)) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
            if (level.isClientSide) return ItemInteractionResult.SUCCESS;
            be.insertCrystal(stack, player);
            // 返回 SUCCESS 而非 CONSUME：insertCrystal 已手动 shrink，CONSUME 会二次扣除
            return ItemInteractionResult.SUCCESS;
        }

        // 普通右键无论手中是否持物都打开 UI，避免部分情况下 useWithoutItem 不会被调用
        return openCoreMenu(player, level, pos)
                ? ItemInteractionResult.SUCCESS
                : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        // 处理 Shift+右键取出水晶
        if (player.isShiftKeyDown()) {
            if (level.isClientSide) return InteractionResult.SUCCESS;
            if (!(level.getBlockEntity(pos) instanceof SpiritCoreBlockEntity be)) return InteractionResult.PASS;
            if (be.getCrystal().isEmpty()) return InteractionResult.PASS;

            ItemStack out = be.extractCrystalWithMana();
            if (!player.getInventory().add(out)) player.drop(out, false);
            return InteractionResult.CONSUME;
        }

        return openCoreMenu(player, level, pos) ? InteractionResult.CONSUME : InteractionResult.PASS;
    }

    /** 打开魔力核心 UI；客户端只返回成功以消费交互，服务端负责创建 Menu。 */
    private static boolean openCoreMenu(Player player, Level level, BlockPos pos) {
        if (level.isClientSide) return true;
        if (!(level.getBlockEntity(pos) instanceof SpiritCoreBlockEntity core)) return false;

        player.openMenu(new SimpleMenuProvider(
                (id, inv, p) -> new SpiritCoreMenu(id, inv, core),
                Component.translatable("block.spiritworld.spirit_core")
        ), buf -> buf.writeBlockPos(pos));
        return true;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moved) {
        if (!level.isClientSide && !oldState.is(state.getBlock())) {
            NetworkManager.registerNode(level, pos, NodeType.CORE);
            // 新核心落地后让已有节点立即重新寻父，避免等待退避冷却
            for (BlockPos nodePos : NetworkManager.getNodesInLevel(level).keySet()) {
                if (nodePos.equals(pos)) continue;
                BlockEntity be = level.getBlockEntity(nodePos);
                if (be instanceof INetworkNode node) {
                    node.findParent();
                }
            }
        }
        super.onPlace(state, level, pos, oldState, moved);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof SpiritCoreBlockEntity core) {
                core.disconnectChildren();
                core.getConnectedCollectors().clear();
                core.setChanged();
                core.dropContents(level, pos);
            }
            if (!level.isClientSide) {
                NetworkManager.unregisterNode(level, pos);
            }
        }
        super.onRemove(state, level, pos, newState, moved);
    }
    /** 从核心收集所有节点（含中继器）与诊断数据并发送给客户端 */
    public static void sendNetworkData(ServerPlayer player, SpiritCoreBlockEntity core) {
        Level level = player.level();
        Map<BlockPos, NodeType> registry = NetworkManager.getNodesInLevel(level);
        List<ClientboundNetworkTreePacket.NodeData> nodes = new ArrayList<>();
        List<ClientboundNetworkTreePacket.PullDetail> details = new ArrayList<>();

        // 1. 核心节点
        nodes.add(new ClientboundNetworkTreePacket.NodeData(
                core.getBlockPos(), null, NodeType.CORE.name(), 1.0,
                core.getTotalMana(), core.getTotalCapacity(), ManaType.NONE,
                1.0, "connected", 0L, core.getChildren().size()
        ));

        // 2. 所有非核心节点：总效率、段效率、断链原因、离线收益、子节点数
        for (Map.Entry<BlockPos, NodeType> entry : registry.entrySet()) {
            BlockPos pos = entry.getKey();
            NodeType type = entry.getValue();
            if (type == NodeType.CORE || pos.equals(core.getBlockPos())) continue;
            if (!(level.getBlockEntity(pos) instanceof INetworkNode node)) continue;

            BlockPos parentPos = node.getParentPos();
            double segmentEfficiency = -1.0;
            if (parentPos != null) {
                NodeType parentType = registry.get(parentPos);
                if (parentType != null) {
                    segmentEfficiency = EfficiencyCalculator.calculateSegmentEfficiency(parentType, parentPos, pos);
                }
            }

            long mana = 0L;
            long capacity = 0L;
            long offlineGain = 0L;
            ManaType manaType = ManaType.NONE;
            if (node instanceof SpiritCollectorBlockEntity collector) {
                mana = collector.getTotalMana();
                capacity = collector.getCapacity();
                manaType = collector.getCurrentType();
                OfflineCollectorData shadow = NetworkManager.getShadowData(pos);
                if (shadow != null) {
                    offlineGain = shadow.calculateOfflineGain(level.getGameTime());
                }
            }

            String reason = parentPos == null ? node.getLinkReason() : "connected";
            nodes.add(new ClientboundNetworkTreePacket.NodeData(
                    pos, parentPos, type.name(), node.getTotalEfficiency(),
                    mana, capacity, manaType, segmentEfficiency, reason,
                    offlineGain, node.getChildren().size()
            ));
        }

        // 3. 抽水明细
        for (var entry : core.getLastPullDetails().entrySet()) {
            details.add(new ClientboundNetworkTreePacket.PullDetail(
                    entry.getKey(), entry.getValue(), core.getLastPullTypes().get(entry.getKey())
            ));
        }

        PacketDistributor.sendToPlayer(player, new ClientboundNetworkTreePacket(nodes, details));
    }
}
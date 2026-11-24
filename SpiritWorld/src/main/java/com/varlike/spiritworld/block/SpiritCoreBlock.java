package com.varlike.spiritworld.block;

import com.mojang.serialization.MapCodec;
import com.varlike.spiritworld.client.SpiritCoreMenu;
import com.varlike.spiritworld.item.ManaCrystalItem;
import com.varlike.spiritworld.network.ClientboundNetworkTreePacket;
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
        if (!player.isShiftKeyDown()) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!(stack.getItem() instanceof ManaCrystalItem)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (!(level.getBlockEntity(pos) instanceof SpiritCoreBlockEntity be)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (level.isClientSide) return ItemInteractionResult.SUCCESS;
        be.insertCrystal(stack, player);
        // 返回 SUCCESS 而非 CONSUME：insertCrystal 已手动 shrink，CONSUME 会二次扣除
        return ItemInteractionResult.SUCCESS;
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

        // 普通右键：打开 UI
        if (level.isClientSide) {
            return InteractionResult.SUCCESS; // 客户端返回成功，防止挥手动画卡住
        }

        // 服务端打开 Menu
        if (level.getBlockEntity(pos) instanceof SpiritCoreBlockEntity core) {
            player.openMenu(new SimpleMenuProvider(
                    (id, inv, p) -> new SpiritCoreMenu(id, inv, core),
                    Component.translatable("block.spiritworld.spirit_core")
            ), buf -> buf.writeBlockPos(pos)); // ⚠️ 必须传入坐标，否则客户端会崩溃
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof SpiritCoreBlockEntity core) {
                core.dropContents(level, pos);
            }
        }
        super.onRemove(state, level, pos, newState, moved);
    }
    /** 从核心收集所有节点数据并发送给客户端 */
    public static void sendNetworkData(ServerPlayer player, SpiritCoreBlockEntity core) {
        List<ClientboundNetworkTreePacket.NodeData> nodes = new ArrayList<>();
        List<ClientboundNetworkTreePacket.PullDetail> details = new ArrayList<>();

        // 1. 核心节点
        nodes.add(new ClientboundNetworkTreePacket.NodeData(
                core.getBlockPos(), null, "CORE", 1.0, core.getTotalMana(), core.getTotalCapacity(), ManaType.NONE
        ));

        // 2. 聚集器节点（遍历核心维护的扁平名单）
        for (var entry : core.getConnectedCollectors().entrySet()) {
            BlockPos cPos = entry.getKey();
            double eff = entry.getValue();

            if (player.level().getBlockEntity(cPos) instanceof SpiritCollectorBlockEntity col) {
                nodes.add(new ClientboundNetworkTreePacket.NodeData(
                        cPos, core.getBlockPos(), "COLLECTOR", eff, col.getTotalMana(), col.getCapacity(), col.getCurrentType()
                ));
            }
        }

        // 3. 抽水明细
        for (var entry : core.getLastPullDetails().entrySet()) {
            details.add(new ClientboundNetworkTreePacket.PullDetail(
                    entry.getKey(), entry.getValue(), core.getLastPullTypes().get(entry.getKey())
            ));
        }

        // 发给客户端
        PacketDistributor.sendToPlayer(player, new ClientboundNetworkTreePacket(nodes, details));
    }
}
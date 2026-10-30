package com.varlike.spiritworld.block;

import com.mojang.serialization.MapCodec;
import com.varlike.spiritworld.client.SpiritCoreMenu;
import com.varlike.spiritworld.item.ManaCrystalItem;
import com.varlike.spiritworld.network.ClientboundNetworkTreePacket;
import com.varlike.spiritworld.skill.ManaType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SpiritCoreBlock extends BaseEntityBlock {

    public SpiritCoreBlock(Properties props) {
        super(props);
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
        return ItemInteractionResult.CONSUME;
    }

    /** 空手 shift+右键取出水晶 */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            if (level.isClientSide) return InteractionResult.SUCCESS;
            if (!(level.getBlockEntity(pos) instanceof SpiritCoreBlockEntity be)) return InteractionResult.PASS;
            if (be.getCrystal().isEmpty()) return InteractionResult.PASS;

            ItemStack out = be.extractCrystalWithMana();
            if (!player.getInventory().add(out)) player.drop(out, false);
            return InteractionResult.CONSUME;
        }

        // 普通空手右键：打开 UI（现有逻辑）
        return super.useWithoutItem(state, level, pos, player, hit);
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
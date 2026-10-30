package com.varlike.spiritworld.block;

import com.mojang.serialization.MapCodec;
import com.varlike.spiritworld.network.NodeType;
import com.varlike.spiritworld.network.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ManaRelayBlock extends BaseEntityBlock {

    private final int tier;

    public ManaRelayBlock(Properties props, int tier) {
        super(props);
        this.tier = tier;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(props -> new ManaRelayBlock(props, 1)); // 占位，实际通过 ModBlocks 构造
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ManaRelayBlockEntity(pos, state);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ManaRelayBlockEntity relay) {
                NetworkManager.registerNode(level, pos, relay.getNodeType());
                relay.findParent(); // 放置时立刻找父节点
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ManaRelayBlockEntity relay) {
                relay.disconnectChildren(); // 通知子节点找新爸爸
                NetworkManager.unregisterNode(level, pos); // 从全局移除
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    public int getTier() {
        return tier;
    }
}
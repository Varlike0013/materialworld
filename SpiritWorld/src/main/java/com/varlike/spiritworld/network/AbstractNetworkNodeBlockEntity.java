package com.varlike.spiritworld.network;

import com.varlike.spiritworld.block.SpiritCollectorBlockEntity;
import com.varlike.spiritworld.block.SpiritCoreBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractNetworkNodeBlockEntity extends BlockEntity implements INetworkNode {

    protected BlockPos parentPos;
    protected double totalEfficiency = 0.0;
    protected final List<BlockPos> children = new ArrayList<>();

    public AbstractNetworkNodeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public BlockPos getBlockPos() { return this.worldPosition; }

    @Override
    public double getTotalEfficiency() { return totalEfficiency; }

    @Override
    public void setTotalEfficiency(double efficiency) { this.totalEfficiency = efficiency; }

    @Override
    public BlockPos getParentPos() { return parentPos; }

    @Override
    public void setParentPos(BlockPos pos) { this.parentPos = pos; }

    @Override
    public List<BlockPos> getChildren() { return children; }

    @Override
    public void addChild(BlockPos pos) {
        if (!children.contains(pos)) {
            children.add(pos);
            this.setChanged();
        }
    }

    @Override
    public void removeChild(BlockPos pos) {
        children.remove(pos);
        this.setChanged();
    }

    /**
     * 核心的寻父算法：遍历全图可用节点，找效率最高的。
     */
    @Override
    public void findParent() {
        Level level = this.level;
        if (level == null || level.isClientSide) return;

        // 1. 先断开旧父节点
        if (parentPos != null) {
            BlockEntity oldParent = level.getBlockEntity(parentPos);
            if (oldParent instanceof INetworkNode node) {
                node.removeChild(this.getBlockPos());
            }
            parentPos = null;
            totalEfficiency = 0.0;
        }

        // 2. 遍历全局节点寻找最优父节点
        Map<BlockPos, NodeType> allNodes = NetworkManager.getNodesInLevel(level);
        BlockPos myPos = this.getBlockPos();
        double bestEfficiency = -1.0;
        BlockPos bestParent = null;

        for (Map.Entry<BlockPos, NodeType> entry : allNodes.entrySet()) {
            BlockPos targetPos = entry.getKey();
            NodeType targetType = entry.getValue();

            if (targetPos.equals(myPos)) continue;
            if (targetType == NodeType.COLLECTOR) continue; // 聚集器不能做父节点

            BlockEntity be = level.getBlockEntity(targetPos);
            if (!(be instanceof INetworkNode parentNode)) continue;

            // ✅ 检查父节点容量是否满载
            if (parentNode.getChildren().size() >= parentNode.getMaxChildren()) continue;

            // 计算连接效率
            double segEff = EfficiencyCalculator.calculateSegmentEfficiency(targetType, targetPos, myPos);
            if (segEff <= 0) continue; // 超距

            // 总效率 = 父节点效率 * 段效率
            double candidateEff = parentNode.getTotalEfficiency() * segEff;

            if (candidateEff > bestEfficiency) {
                bestEfficiency = candidateEff;
                bestParent = targetPos;
            }
        }

        // 3. 建立新连接
        if (bestParent != null) {
            this.parentPos = bestParent;
            this.totalEfficiency = bestEfficiency;
            BlockEntity parent = level.getBlockEntity(bestParent);
            if (parent instanceof INetworkNode node) {
                node.addChild(myPos);
            }
            this.setChanged();
            // 自身连上了，递归让子节点也更新效率
            updateChildrenEfficiency();

            // ✅ 通知核心更新名单
            BlockPos rootCore = findRootCore(level, bestParent);
            if (rootCore != null && level.getBlockEntity(rootCore) instanceof SpiritCoreBlockEntity core) {
                if (this instanceof SpiritCollectorBlockEntity) {
                    core.registerCollector(myPos, bestEfficiency);
                }
            }
        } else {
            this.totalEfficiency = 0.0;
            this.setChanged();
            disconnectChildren(); // 自身游离，子节点也断开
        }
    }

    /** 向上追溯找到核心 */
    protected BlockPos findRootCore(Level level, BlockPos startPos) {
        BlockPos current = startPos;
        int depth = 0; // 防死循环
        while (current != null && depth < 20) {
            BlockEntity be = level.getBlockEntity(current);
            if (be instanceof SpiritCoreBlockEntity) return current;
            if (be instanceof INetworkNode node) {
                current = node.getParentPos();
            } else {
                break;
            }
            depth++;
        }
        return null;
    }

    /**
     * 当自身效率更新时，级联刷新所有子节点的效率
     */
    protected void updateChildrenEfficiency() {
        for (BlockPos childPos : new ArrayList<>(children)) {
            BlockEntity be = level.getBlockEntity(childPos);
            if (be instanceof AbstractNetworkNodeBlockEntity child) {
                // 重新计算段效率
                double segEff = EfficiencyCalculator.calculateSegmentEfficiency(
                        this.getNodeType(), this.getBlockPos(), childPos);
                if (segEff > 0) {
                    child.setTotalEfficiency(this.totalEfficiency * segEff);
                    child.updateChildrenEfficiency();
                } else {
                    child.findParent(); // 距离变远导致断开，让它重新找
                }
            }
        }
    }

    @Override
    public void disconnectChildren() {
        for (BlockPos childPos : new ArrayList<>(children)) {
            BlockEntity be = level.getBlockEntity(childPos);
            if (be instanceof AbstractNetworkNodeBlockEntity child) {
                child.parentPos = null;
                child.totalEfficiency = 0.0;
                child.findParent(); // 强制孤儿重新找爸爸
            }
        }
        children.clear();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (parentPos != null) tag.putLong("ParentPos", parentPos.asLong());
        tag.putDouble("TotalEfficiency", totalEfficiency);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("ParentPos")) {
            parentPos = BlockPos.of(tag.getLong("ParentPos"));
        } else {
            parentPos = null;
        }
        totalEfficiency = tag.getDouble("TotalEfficiency");
    }
}
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

    public static final int MAX_RELINK_ATTEMPTS = 3;
    public static final int RELINK_COOLDOWN_TICKS = 100;

    protected BlockPos parentPos;
    protected double totalEfficiency = 0.0;
    protected final List<BlockPos> children = new ArrayList<>();

    private String linkReason = "no_parent";
    private int relinkCooldown;
    private int relinkAttempts;

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

    @Override
    public String getLinkReason() { return linkReason; }

    @Override
    public void setLinkReason(String reason) {
        this.linkReason = reason == null ? "no_parent" : reason;
    }

    @Override
    public void requestRelink() {
        relinkCooldown = Math.max(relinkCooldown, RELINK_COOLDOWN_TICKS);
    }

    @Override
    public int getRelinkAttempts() { return relinkAttempts; }

    @Override
    public boolean tickRelink() {
        if (relinkCooldown > 0) {
            relinkCooldown--;
            return false;
        }
        if (parentPos != null && totalEfficiency > 0.0) return false;

        relinkAttempts++;
        findParent();
        return true;
    }

    @Override
    public void resetRelink() {
        relinkAttempts = 0;
        relinkCooldown = parentPos == null ? 1 : 0;
        if (parentPos == null) linkReason = "no_parent";
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

        // 2. 遍历全局节点寻找最优父节点，并记录断链原因
        Map<BlockPos, NodeType> allNodes = NetworkManager.getNodesInLevel(level);
        BlockPos myPos = this.getBlockPos();
        double bestEfficiency = -1.0;
        BlockPos bestParent = null;
        boolean sawFull = false;
        boolean sawOutOfRange = false;
        boolean sawCandidate = false;
        boolean sawInactiveParent = false;

        for (Map.Entry<BlockPos, NodeType> entry : allNodes.entrySet()) {
            BlockPos targetPos = entry.getKey();
            NodeType targetType = entry.getValue();

            if (targetPos.equals(myPos)) continue;
            if (targetType == NodeType.COLLECTOR) continue; // 聚集器不能做父节点

            BlockEntity be = level.getBlockEntity(targetPos);
            if (!(be instanceof INetworkNode parentNode)) continue;

            if (parentNode.getChildren().size() >= parentNode.getMaxChildren()) {
                sawFull = true;
                continue;
            }

            double segEff = EfficiencyCalculator.calculateSegmentEfficiency(targetType, targetPos, myPos);
            if (segEff <= 0) {
                sawOutOfRange = true;
                continue;
            }

            sawCandidate = true;
            double candidateEff = parentNode.getTotalEfficiency() * segEff;
            if (candidateEff <= 0) {
                sawInactiveParent = true;
                continue;
            }

            if (candidateEff > bestEfficiency) {
                bestEfficiency = candidateEff;
                bestParent = targetPos;
            }
        }

        // 3. 建立新连接或记录断链原因并等待冷却重连
        if (bestParent != null) {
            this.parentPos = bestParent;
            this.totalEfficiency = bestEfficiency;
            this.linkReason = "connected";
            this.relinkAttempts = 0;
            this.relinkCooldown = 0;
            BlockEntity parent = level.getBlockEntity(bestParent);
            if (parent instanceof INetworkNode node) {
                node.addChild(myPos);
            }
            this.setChanged();
            updateChildrenEfficiency();

            BlockPos rootCore = findRootCore(level, bestParent);
            if (rootCore != null && level.getBlockEntity(rootCore) instanceof SpiritCoreBlockEntity core) {
                if (this instanceof SpiritCollectorBlockEntity) {
                    core.registerCollector(myPos, bestEfficiency);
                }
            }
        } else {
            this.totalEfficiency = 0.0;
            if (!sawCandidate && !sawOutOfRange && !sawFull) {
                this.linkReason = "no_parent";
            } else if (sawFull && !sawCandidate && !sawOutOfRange) {
                this.linkReason = "parent_full";
            } else if (sawOutOfRange && !sawCandidate) {
                this.linkReason = "out_of_range";
            } else if (sawInactiveParent) {
                this.linkReason = "parent_offline";
            } else {
                this.linkReason = "no_core";
            }
            long backoff = Math.min(RELINK_COOLDOWN_TICKS * (1L << Math.min(relinkAttempts, 4)), 1600L);
            this.relinkCooldown = Math.max(this.relinkCooldown, (int) backoff);
            this.setChanged();
            disconnectChildren(); // 自身游离，子节点进入延迟重连
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
                    child.setLinkReason("out_of_range");
                    child.requestRelink();
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
                child.setLinkReason("parent_lost");
                child.requestRelink(); // 延迟重连，避免递归全图扫描
            }
        }
        children.clear();
        this.setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (parentPos != null) tag.putLong("ParentPos", parentPos.asLong());
        tag.putDouble("TotalEfficiency", totalEfficiency);
        tag.putString("LinkReason", linkReason);
        tag.putInt("RelinkAttempts", relinkAttempts);
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
        linkReason = tag.getString("LinkReason");
        if (linkReason.isEmpty()) linkReason = "no_parent";
        relinkAttempts = tag.getInt("RelinkAttempts");
        if (parentPos == null && relinkAttempts > 0) {
            relinkCooldown = RELINK_COOLDOWN_TICKS;
        }
        // 区块/世界加载时重新登记节点，避免静态节点表在重启后为空
        if (level != null && !level.isClientSide) {
            NetworkManager.registerNode(level, getBlockPos(), getNodeType());
        }
    }
}
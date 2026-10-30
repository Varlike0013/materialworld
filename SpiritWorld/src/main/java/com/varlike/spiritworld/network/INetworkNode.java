package com.varlike.spiritworld.network;

import net.minecraft.core.BlockPos;
import java.util.List;

public interface INetworkNode {

    NodeType getNodeType();
    BlockPos getBlockPos();

    /** 当前节点记录的总效率（从核心到自己） */
    double getTotalEfficiency();
    void setTotalEfficiency(double efficiency);

    /** 父节点坐标 */
    BlockPos getParentPos();
    void setParentPos(BlockPos pos);

    /** 子节点列表 */
    List<BlockPos> getChildren();

    /** 节点的最大子节点承载量（核心 8，中继器 16，聚集器 0） */
    int getMaxChildren();

    /** 触发寻找父节点（重构网络或放置时调用） */
    void findParent();

    /** 子节点发生变动时，通知本节点（比如子节点被摧毁时，中继器需要清理记录） */
    void onChildDisconnected(BlockPos childPos);
    /** 添加子节点 */
    void addChild(BlockPos pos);
    /** 移除子节点 */
    void removeChild(BlockPos pos);
    /** 当自身被破坏或断开连接时，通知所有子节点去重新寻找父节点 */
    void disconnectChildren();
}
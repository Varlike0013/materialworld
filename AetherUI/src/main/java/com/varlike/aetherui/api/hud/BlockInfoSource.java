package com.varlike.aetherui.api.hud;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

/**
 * 方块自描述信息源。
 *
 * <p>与需要注册的 {@link BlockInfoProvider} 不同，自定义 {@code Block} 或 {@code BlockEntity}
 * 可直接实现本接口，无需调用任何注册 API。玩家瞄准该方块时，TargetInfoHud 会自动调用
 * {@link #getTargetInfoLines(Level, BlockPos, BlockState, BlockEntity, BlockHitResult, Player)}
 * 并把返回的信息行追加到挖掘信息下方。
 *
 * <p>回调在客户端执行。若数据保存在服务端 BlockEntity 中，请通过
 * {@code getUpdateTag} / {@code onDataPacket}、Data Attachment 或自定义网络包同步到客户端，
 * 否则客户端读取到的会是默认值。
 *
 * <p>若方块与方块实体都实现了本接口，两者的信息都会按“方块 → 方块实体 → 已注册提供者”的顺序追加。
 */
public interface BlockInfoSource {

    /**
     * 返回瞄准该方块时要显示的信息行。
     *
     * @param level       客户端世界
     * @param pos         方块坐标
     * @param state       方块状态
     * @param blockEntity 方块实体，可能为 null
     * @param hit         射线命中结果
     * @param player      当前玩家
     * @return 信息行列表；返回 null 或空列表表示无信息
     */
    List<Component> getTargetInfoLines(Level level, BlockPos pos, BlockState state,
                                       BlockEntity blockEntity, BlockHitResult hit, Player player);
}

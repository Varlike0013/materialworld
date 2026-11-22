package com.varlike.aetherui.api.hud;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

/**
 * 方块信息提供者：其它 mod 实现此接口并注册后，当玩家瞄准的方块匹配时，
 * 返回的信息会显示在右下角 HUD（挖掘信息下方）。
 *
 * <p>每行是一个 {@link Component}，可通过 {@code Component.literal(text).withStyle(...)}
 * 设置颜色、粗体等样式，天然支持多行与多色。返回 {@code null} 或空列表表示该方块无信息。
 *
 * <p>提供者按注册顺序查询，所有匹配提供者的行会依次追加显示。
 */
public interface BlockInfoProvider {

    /** 唯一标识，用于去重与注销。 */
    ResourceLocation id();

    /**
     * 获取瞄准方块的信息行。
     *
     * @param level       客户端世界
     * @param pos         方块坐标
     * @param state       方块状态
     * @param blockEntity 方块实体（可能为 null）
     * @param hit         射线命中结果
     * @param player      当前玩家
     * @return 信息行列表；返回 null 或空列表表示无信息
     */
    List<Component> getLines(Level level, BlockPos pos, BlockState state,
                             BlockEntity blockEntity, BlockHitResult hit, LocalPlayer player);
}

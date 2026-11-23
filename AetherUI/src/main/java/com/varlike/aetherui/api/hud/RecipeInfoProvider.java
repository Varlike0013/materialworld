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
 * 配方信息提供者：其它 mod 实现此接口并注册后，当玩家瞄准对应方块（如熔炉、魔力聚集器）时，
 * 右下角 HUD 会在挖掘信息下方追加一条可视化的配方：[材料] → [时间] → [产物]。
 *
 * <p>每行配方是一个 {@link RecipeInfo}，可用 {@link #title()} 覆写该条目的标题（如"熔炼配方"），
 * 默认无标题。返回 null 或空列表表示不匹配。
 *
 * <p>提供者按注册顺序查询，所有匹配提供者的配方会依次追加显示。
 */
public interface RecipeInfoProvider {

    /** 唯一标识，用于去重和注销。 */
    ResourceLocation id();

    /**
     * 获取瞄准方块的配方信息。
     *
     * @param level       客户端世界
     * @param pos         方块坐标
     * @param state       方块状态
     * @param blockEntity 方块实体（可能为 null）
     * @param hit         射线命中结果
     * @param player      当前玩家
     * @return 配方列表；返回 null 或空列表表示无信息
     */
    List<RecipeInfo> getRecipes(Level level, BlockPos pos, BlockState state,
                                BlockEntity blockEntity, BlockHitResult hit, LocalPlayer player);

    /** 可选的条目标题，默认空（不显示）。 */
    default Component title() { return null; }
}

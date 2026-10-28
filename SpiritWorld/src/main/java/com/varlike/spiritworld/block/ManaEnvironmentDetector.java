package com.varlike.spiritworld.block;

import com.varlike.spiritworld.skill.ManaType;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

/**
 * 环境 → 魔力属性检测。
 * 扫描半径 2（5×5×5）内的方块，累计权重，返回权重最高者。
 */
public final class ManaEnvironmentDetector {

    private ManaEnvironmentDetector() {}

    private static final int SCAN_RADIUS = 2;

    public static ManaType detect(Level level, BlockPos pos) {
        int fire = 0, water = 0, spirit = 0;

        for (int dx = -SCAN_RADIUS; dx <= SCAN_RADIUS; dx++) {
            for (int dy = -SCAN_RADIUS; dy <= SCAN_RADIUS; dy++) {
                for (int dz = -SCAN_RADIUS; dz <= SCAN_RADIUS; dz++) {
                    BlockPos p = pos.offset(dx, dy, dz);
                    if (!level.isLoaded(p)) continue;
                    BlockState state = level.getBlockState(p);

                    // 火
                    if (state.is(BlockTags.FIRE) ||
                            state.getFluidState().is(Fluids.LAVA) ||
                            state.is(Blocks.LAVA) || state.is(Blocks.MAGMA_BLOCK) ||
                            state.is(Blocks.FURNACE) || state.is(Blocks.BLAST_FURNACE) ||
                            state.is(Blocks.CAMPFIRE) || state.is(Blocks.SOUL_CAMPFIRE)) {
                        fire++;
                    }

                    // 水
                    if (state.getFluidState().is(Fluids.WATER) ||
                            state.is(Blocks.ICE) || state.is(Blocks.PACKED_ICE) ||
                            state.is(Blocks.BLUE_ICE) || state.is(Blocks.SNOW_BLOCK) ||
                            state.is(Blocks.SEA_LANTERN)) {
                        water++;
                    }

                    // 精神
                    if (state.is(Blocks.AMETHYST_BLOCK) || state.is(Blocks.BUDDING_AMETHYST) ||
                            state.is(Blocks.SOUL_SAND) || state.is(Blocks.SOUL_SOIL) ||
                            state.is(Blocks.SOUL_FIRE)) {
                        spirit++;
                    }
                }
            }
        }

        // 风：高空 + 露天
        boolean wind = pos.getY() > 100 && level.canSeeSky(pos.above());

        // 土：地下（Y < 0）+ 封闭
        boolean earth = pos.getY() < 0 && !level.canSeeSky(pos.above());

        // 优先级：SPIRIT > FIRE > WATER > WIND > EARTH > NONE
        // 权重高者优先，平票时按上述顺序
        if (spirit >= 3) return ManaType.SPIRIT;
        if (fire > water) return ManaType.FIRE;
        if (water > fire) return ManaType.WATER;
        if (wind) return ManaType.WIND;
        if (earth) return ManaType.EARTH;
        return ManaType.NONE;
    }
}
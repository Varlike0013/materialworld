package com.varlike.spiritworld.world;

import com.varlike.spiritworld.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.Iterator;

public class PocketGenerationTask {
    private final ServerLevel level;
    private final BlockPos center;
    private final int oldLevel;
    private final int newLevel;

    // 迭代器
    private Iterator<BlockPos> boundaryIterator;
    private Iterator<BlockPos> dirtIterator;
    private Iterator<BlockPos> removeOldIterator;

    private int phase = 0; // 0: 清理旧边界, 1: 铺泥土, 2: 铺新边界
    private boolean finished = false;

    private static final int BLOCKS_PER_TICK = 2000; // 每 tick 处理 2000 个方块，非常顺滑

    public PocketGenerationTask(ServerLevel level, BlockPos center, int oldLevel, int newLevel) {
        this.level = level;
        this.center = center;
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;

        // 初始化迭代器
        this.removeOldIterator = createOldBoundaryIterator();
    }

    public boolean isFinished() {
        return finished;
    }

    /** 每 tick 由 Manager 调用 */
    public void tick() {
        int processed = 0;

        while (processed < BLOCKS_PER_TICK && !finished) {
            switch (phase) {
                case 0 -> { // 清理旧边界
                    if (removeOldIterator != null && removeOldIterator.hasNext()) {
                        BlockPos pos = removeOldIterator.next();
                        if (level.getBlockState(pos).is(ModBlocks.MAGIC_BARRIER.get())) {
                            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                        }
                        processed++;
                    } else {
                        phase = 1;
                        dirtIterator = createDirtIterator();
                    }
                }
                case 1 -> { // 铺泥土底层
                    if (dirtIterator != null && dirtIterator.hasNext()) {
                        BlockPos pos = dirtIterator.next();
                        if (level.getBlockState(pos).isAir()) {
                            int dy = pos.getY() - center.getY();
                            BlockState state = (dy == 4) ? Blocks.GRASS_BLOCK.defaultBlockState() : Blocks.DIRT.defaultBlockState();
                            level.setBlockAndUpdate(pos, state);
                        }
                        processed++;
                    } else {
                        phase = 2;
                        boundaryIterator = createNewBoundaryIterator();
                    }
                }
                case 2 -> { // 铺新魔法墙
                    if (boundaryIterator != null && boundaryIterator.hasNext()) {
                        BlockPos pos = boundaryIterator.next();
                        if (level.getBlockState(pos).isAir()) {
                            level.setBlockAndUpdate(pos, ModBlocks.MAGIC_BARRIER.get().defaultBlockState());
                        }
                        processed++;
                    } else {
                        finished = true;
                    }
                }
            }
        }
    }

    // ==================== 迭代器生成方法 ====================

    private Iterator<BlockPos> createOldBoundaryIterator() {
        if (oldLevel <= 0) return null;
        int oldR = com.varlike.spiritworld.skill.SkillValues.POCKET_SIZE_XZ[oldLevel] / 2;
        int oldH = com.varlike.spiritworld.skill.SkillValues.POCKET_SIZE_Y[oldLevel];

        // 生成一圈旧边界
        BoundingBox box = new BoundingBox(
                center.getX() - oldR - 1, center.getY() - 1, center.getZ() - oldR - 1,
                center.getX() + oldR, center.getY() + oldH, center.getZ() + oldR
        );
        return ShellIterator.of(box);
    }

    private Iterator<BlockPos> createDirtIterator() {
        int newR = com.varlike.spiritworld.skill.SkillValues.POCKET_SIZE_XZ[newLevel] / 2;
        int oldR = (oldLevel <= 0) ? -1 : com.varlike.spiritworld.skill.SkillValues.POCKET_SIZE_XZ[oldLevel] / 2;

        // 只遍历新增的外环泥土（跳过内部）
        BoundingBox box = new BoundingBox(
                center.getX() - newR, center.getY(), center.getZ() - newR,
                center.getX() + newR - 1, center.getY() + 4, center.getZ() + newR - 1
        );
        return new RingIterator(box, center, oldR);
    }

    private Iterator<BlockPos> createNewBoundaryIterator() {
        int newR = com.varlike.spiritworld.skill.SkillValues.POCKET_SIZE_XZ[newLevel] / 2;
        int newH = com.varlike.spiritworld.skill.SkillValues.POCKET_SIZE_Y[newLevel];

        BoundingBox box = new BoundingBox(
                center.getX() - newR - 1, center.getY() - 1, center.getZ() - newR - 1,
                center.getX() + newR, center.getY() + newH, center.getZ() + newR
        );
        return ShellIterator.of(box);
    }

    // ==================== 自定义迭代器 ====================

    /** 只遍历新旧区域差集的环带，跳过老区域 */
    private static class RingIterator implements Iterator<BlockPos> {
        private final BlockPos center;
        private final int oldR;
        private final int minX, maxX, minY, maxY, minZ, maxZ;
        private int x, y, z;
        private final BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        public RingIterator(BoundingBox box, BlockPos center, int oldR) {
            this.center = center;
            this.oldR = oldR;
            this.minX = box.minX(); this.maxX = box.maxX();
            this.minY = box.minY(); this.maxY = box.maxY();
            this.minZ = box.minZ(); this.maxZ = box.maxZ();
            this.x = minX; this.y = minY; this.z = minZ;
        }
        @Override
        public boolean hasNext() {
            return x <= maxX;
        }
        @Override
        public BlockPos next() {
            while (hasNext()) {
                mutable.set(x, y, z);
                // 推进坐标
                z++;
                if (z > maxZ) {
                    z = minZ;
                    x++;
                    if (x > maxX) {
                        x = minX;
                        y++;
                        if (y > maxY) {
                            x = maxX + 1; // 结束标志
                        }
                    }
                }
                // 跳过老区域
                if (oldR > 0) {
                    int dx = mutable.getX() - center.getX();
                    int dz = mutable.getZ() - center.getZ();
                    if (dx >= -oldR && dx < oldR && dz >= -oldR && dz < oldR) {
                        continue;
                    }
                }
                return mutable.immutable();
            }
            return BlockPos.ZERO; // 不会执行到这里
        }
    }
}
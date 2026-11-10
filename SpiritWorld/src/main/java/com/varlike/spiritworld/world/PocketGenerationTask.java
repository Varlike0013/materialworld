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
                            // 判断是不是最顶层的草方块
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
        // 这里简化为遍历整个外壳（原版 BlockPos.betweenClosed 会遍历实心，我们要过滤外壳）
        return new OuterShellIterator(box);
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
        return new OuterShellIterator(box);
    }

    // ==================== 自定义迭代器 ====================

    /** 只遍历 6 个面（外壳），跳过内部空气，极大减少计算量 */
    private static class OuterShellIterator implements Iterator<BlockPos> {
        private final BoundingBox box;
        private final BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        private int x, y, z;
        private boolean hasNext = true;

        public OuterShellIterator(BoundingBox box) {
            this.box = box;
            this.x = box.minX(); this.y = box.minY(); this.z = box.minZ();
        }

        @Override public boolean hasNext() { return hasNext; }

        @Override public BlockPos next() {
            mutable.set(x, y, z);
            advance();
            return mutable.immutable();
        }

        private void advance() {
            z++;
            if (z > box.maxZ()) { z = box.minZ(); y++; }
            if (y > box.maxY()) { y = box.minY(); x++; }
            if (x > box.maxX()) { hasNext = false; return; }
            // 核心：如果当前点在内部（非表面），直接跳过，实现极速跳跃
            if (x > box.minX() && x < box.maxX() &&
                    y > box.minY() && y < box.maxY() &&
                    z > box.minZ() && z < box.maxZ()) {
                z = box.maxZ(); // 直接跳到本行末尾
                advance();
            }
        }
    }

    /** 只遍历新旧区域差集的环带，跳过老区域 */
    private static class RingIterator implements Iterator<BlockPos> {
        private final BoundingBox box;
        private final BlockPos center;
        private final int oldR;
        private final BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        private int x, y, z;
        private boolean hasNext = true;

        public RingIterator(BoundingBox box, BlockPos center, int oldR) {
            this.box = box; this.center = center; this.oldR = oldR;
            this.x = box.minX(); this.y = box.minY(); this.z = box.minZ();
        }

        @Override public boolean hasNext() { return hasNext; }

        @Override public BlockPos next() {
            mutable.set(x, y, z);
            advance();
            return mutable.immutable();
        }

        private void advance() {
            z++;
            if (z > box.maxZ()) { z = box.minZ(); x++; }
            if (x > box.maxX()) { hasNext = false; return; }

            // 跳过老区域
            if (oldR > 0) {
                int dx = x - center.getX();
                int dz = z - center.getZ();
                if (dx >= -oldR && dx < oldR && dz >= -oldR && dz < oldR) {
                    z = center.getZ() + oldR - 1; // 跳过中间的 Z
                    advance();
                }
            }
        }
    }
}
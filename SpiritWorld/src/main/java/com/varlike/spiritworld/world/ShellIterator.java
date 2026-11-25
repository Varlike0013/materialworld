package com.varlike.spiritworld.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.Iterator;
import java.util.NoSuchElementException;

/** 按六个面遍历 BoundingBox 外壳，避免扫描内部体积，且每个方块只返回一次。 */
final class ShellIterator implements Iterator<BlockPos> {

    private final int minX, maxX, minY, maxY, minZ, maxZ;
    private int phase = 0;
    private int a, b;
    private BlockPos next;

    private ShellIterator(BoundingBox box) {
        this.minX = box.minX();
        this.maxX = box.maxX();
        this.minY = box.minY();
        this.maxY = box.maxY();
        this.minZ = box.minZ();
        this.maxZ = box.maxZ();
        this.a = minX;
        this.b = minZ;
        this.next = findNext();
    }

    static ShellIterator of(BoundingBox box) {
        return new ShellIterator(box);
    }

    static ShellIterator around(BlockPos center, int radius, int height) {
        return of(new BoundingBox(
                center.getX() - radius - 1, center.getY() - 1, center.getZ() - radius - 1,
                center.getX() + radius, center.getY() + height, center.getZ() + radius));
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public BlockPos next() {
        if (next == null) throw new NoSuchElementException();
        BlockPos result = next;
        next = findNext();
        return result;
    }

    private BlockPos findNext() {
        while (phase < 6) {
            switch (phase) {
                case 0 -> { // 底面：y = minY，遍历整个 x-z 平面
                    if (b > maxZ) { b = minZ; a++; }
                    if (a > maxX) {
                        phase = minY == maxY ? 2 : 1;
                        a = minX;
                        b = minZ;
                        continue;
                    }
                    return new BlockPos(a, minY, b++);
                }
                case 1 -> { // 顶面：y = maxY，遍历整个 x-z 平面
                    if (b > maxZ) { b = minZ; a++; }
                    if (a > maxX) {
                        phase = 2;
                        a = minX;
                        b = minY + 1;
                        continue;
                    }
                    return new BlockPos(a, maxY, b++);
                }
                case 2, 3 -> { // 前后两面：z = minZ / maxZ，y 排除底面和顶面
                    if (a > maxX) { a = minX; b++; }
                    if (b >= maxY) {
                        phase = phase == 2 ? 3 : 4;
                        a = phase == 3 ? minX : minZ + 1;
                        b = minY + 1;
                        continue;
                    }
                    return new BlockPos(a++, b, phase == 2 ? minZ : maxZ);
                }
                case 4, 5 -> { // 左右两面：x = minX / maxX，z 排除 corners
                    if (a > maxZ - 1) { a = minZ + 1; b++; }
                    if (b >= maxY) {
                        phase++;
                        a = minZ + 1;
                        b = minY + 1;
                        continue;
                    }
                    return new BlockPos(phase == 4 ? minX : maxX, b, a++);
                }
                default -> throw new IllegalStateException();
            }
        }
        return null;
    }
}

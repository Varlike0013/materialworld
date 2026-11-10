package com.varlike.spiritworld.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;

public class MagicBarrierBlock extends Block {
    public MagicBarrierBlock(Properties props) {
        super(props);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE; // 全透明
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D); // 满格碰撞箱
    }

    /**
     * 客户端 tick 调用：实现靠近显示魔法粒子
     */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // 寻找 3 格内最近的玩家（false 表示包含生存和创造模式）
        Player player = level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3.0, false);
        if (player == null) return;

        // 计算玩家到方块中心的距离平方
        double distSqr = player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);

        // 距离 3 格以内 (3.0 * 3.0 = 9.0)
        if (distSqr <= 9.0) {
            double dist = Math.sqrt(distSqr);

            // 计算概率：3格以外0%，3格时10%，1格内极高
            // 公式：(1.0 - dist/3.0) 得到 0~1 的值，乘以 0.8 得到概率，最低保证 0.1
            double chance = Math.max(0.1, 1.0 - (dist / 3.0) * 0.9);

            // 只有随机数小于概率时才生成，实现“越近越密”
            if (random.nextDouble() < chance) {
                // 在方块表面随机位置生成
                double x = pos.getX() + random.nextDouble();
                double y = pos.getY() + random.nextDouble();
                double z = pos.getZ() + random.nextDouble();

                // 紫黑色的 RGB 参数 (范围 0.0 ~ 1.0)
                // 0.3f 红, 0.0f 绿, 0.5f 蓝 = 紫黑色
                Vector3f color = new Vector3f(0.3f, 0.0f, 0.6f);
                DustParticleOptions particle = new DustParticleOptions(color, 1.0f); // 1.0f 是粒子大小

                // 添加粒子，最后三个参数是速度 (x, y, z)，设为 0 就在原地飘散
                level.addParticle(particle, x, y, z, 0.0D, 0.0D, 0.0D);
            }
        }
    }
}
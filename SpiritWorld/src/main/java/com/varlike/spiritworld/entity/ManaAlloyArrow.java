package com.varlike.spiritworld.entity;

import com.varlike.spiritworld.entity.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

/**
 * 魔力合金箭：
 * <ul>
 *   <li>基础伤害 4.0（普通箭 2.0）</li>
 *   <li>初速度倍率 1.5（在物品里 setDeltaMovement 时应用）</li>
 *   <li>飞行中每 3 tick 索敌一次，8 格半径内最近目标，仅在飞行方向前 60° 内生效</li>
 * </ul>
 */
public class ManaAlloyArrow extends Arrow {

    private static final double HOMING_RADIUS = 8.0;
    private static final double HOMING_MIN_DOT = 0.5;   // 约 60°
    private static final double HOMING_INTERVAL_TICKS = 3;
    private static final double HOMING_STRENGTH = 0.15;

    public ManaAlloyArrow(EntityType<? extends Arrow> type, Level level) {
        super(type, level);
    }

    public ManaAlloyArrow(Level level, LivingEntity shooter, ItemStack pickup, @Nullable ItemStack weapon) {
        super(ModEntities.MANA_ALLOY_ARROW.get(), level);
        this.setOwner(shooter);
        this.setPos(shooter.getX(), shooter.getEyeY() - 0.1, shooter.getZ());
    }
    public ManaAlloyArrow(Level level, double x, double y, double z, ItemStack pickup, @Nullable ItemStack weapon) {
        super(ModEntities.MANA_ALLOY_ARROW.get(), level);
        this.setPos(x, y, z);
        this.setPickupItemStack(pickup);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) return;
        if (this.inGround) return;
        if (this.tickCount % HOMING_INTERVAL_TICKS != 0) return;

        LivingEntity target = findTarget();
        if (target == null) return;
        steerToward(target);
    }

    private LivingEntity findTarget() {
        Vec3 pos = this.position();
        List<LivingEntity> candidates = this.level().getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(HOMING_RADIUS),
                this::isValidTarget);

        return candidates.stream()
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(pos)))
                .orElse(null);
    }

    private boolean isValidTarget(LivingEntity e) {
        if (!e.isAlive()) return false;
        if (e == this.getOwner()) return false;
        if (e.isSpectator()) return false;
        if (this.getOwner() instanceof Player owner && e.isAlliedTo(owner)) return false;
        return true;
    }

    private void steerToward(LivingEntity target) {
        Vec3 current = this.getDeltaMovement();
        double speed = current.length();
        if (speed < 0.05) return;

        Vec3 toTarget = target.position()
                .add(0, target.getBbHeight() / 2.0, 0)
                .subtract(this.position())
                .normalize();

        double dot = current.normalize().dot(toTarget);
        if (dot < HOMING_MIN_DOT) return;

        Vec3 newDir = current.normalize().scale(1.0 - HOMING_STRENGTH)
                .add(toTarget.scale(HOMING_STRENGTH))
                .normalize();

        this.setDeltaMovement(newDir.scale(speed));
    }
    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (this.level().isClientSide) return;
        Entity target = result.getEntity();
        if (!(target instanceof LivingEntity living)) return;

        CompoundTag data = this.getPersistentData();

        // ===== 冲击（原版效果：每级 +1 击退）=====
        int punchLevel = data.getInt("PunchLevel");
        if (punchLevel > 0) {
            Vec3 dir = this.getDeltaMovement();
            // 和原版 Arrow.onHitEntity 里一致的击退方式
            living.push(dir.x * punchLevel, 0.1, dir.z * punchLevel);
            living.hurtMarked = true;
        }

        // ===== 火矢（原版效果：100 tick 燃烧）=====
        if (data.getBoolean("Flame")) {
            living.setRemainingFireTicks(100);
        }
    }
}
package com.varlike.spiritworld.skill;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForgeMod;

/**
 * 技能通用辅助方法。
 * 目前包含飞行状态操作（mayFly / flying），
 * 后续可扩展其他技能共用的工具方法。
 */
public final class SkillHelper {

    private SkillHelper() {}

    // ==================== 飞行状态 ====================

    /**
     * 当前玩家是否允许飞行。
     * 走 CREATIVE_FLIGHT 属性，避免直接读写已弃用的 {@code abilities.mayfly}。
     */
    public static boolean mayFly(Player player) {
        return player.mayFly();
    }

    /**
     * 设置玩家是否允许飞行。
     * CREATIVE_FLIGHT = 1.0 允许，0.0 不允许。
     *
     * @param player 目标玩家
     * @param value  true = 允许飞行，false = 禁止飞行
     */
    public static void setMayFly(Player player, boolean value) {
        player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT)
                .setBaseValue(value ? 1.0 : 0.0);
    }

    /**
     * 设置玩家当前是否处于飞行状态。
     * {@code flying} 未被弃用，仍可直接操作。
     */
    public static void setFlying(Player player, boolean value) {
        player.getAbilities().flying = value;
    }

    /**
     * 一次性设置「允许飞行 + 当前飞行」，并同步客户端。
     * 常用组合，避免每次都写三行。
     */
    public static void setFlightState(Player player, boolean mayFly, boolean flying) {
        setMayFly(player, mayFly);
        setFlying(player, flying);
        player.onUpdateAbilities();
    }
}
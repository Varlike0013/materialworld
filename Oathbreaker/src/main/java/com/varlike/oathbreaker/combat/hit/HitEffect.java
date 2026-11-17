package com.varlike.oathbreaker.combat.hit;

/**
 * 命中后触发的效果接口。
 *
 * <p>开发者可以：</p>
 * <ul>
 *     <li>通过 {@code WeaponPool.register(...).hitEffect(...)} 为某件武器注册；</li>
 *     <li>让自定义 Item 实现 {@link WeaponHitProvider}，直接返回效果；</li>
 *     <li>技能系统接入后，用同一接口注册技能命中效果。</li>
 * </ul>
 *
 * <p>实现必须是服务端安全的；所有回调都在服务端主线程执行。</p>
 */
public interface HitEffect {
    void onHit(HitContext context);

    /** 可选：目标因此次命中死亡时触发；默认不处理。 */
    default void onKill(HitContext context) {
    }
}

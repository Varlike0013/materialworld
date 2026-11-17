package com.varlike.oathbreaker.core.weapon;

import com.varlike.oathbreaker.combat.DamageProfile;
import com.varlike.oathbreaker.combat.hit.HitContext;
import com.varlike.oathbreaker.combat.hit.HitEffect;
import com.varlike.oathbreaker.combat.hit.HitSource;
import com.varlike.oathbreaker.combat.hit.WeaponHitProvider;
import com.varlike.oathbreaker.core.classification.OathbreakerDataMaps;
import com.varlike.oathbreaker.core.classification.WeaponProfile;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * 武器池。
 *
 * <p>数据来源：</p>
 * <ul>
 *     <li>JSON：{@code data/<ns>/oathbreaker/weapons/*.json}，只增加池成员、权重、池标签；</li>
 *     <li>代码：{@link #register(WeaponDefinition)}，可附带 profile 与命中效果，同 ID 覆盖 JSON。</li>
 * </ul>
 *
 * <p>解析优先级：代码定义 &gt; JSON 池条目 &gt; DataMap {@code weapon_profile}。
 * JSON 条目没有 profile，因此不会改变武器原有伤害；代码条目可以覆盖。</p>
 */
public final class WeaponPool {
    private static final Map<ResourceLocation, WeaponDefinition> JSON = new LinkedHashMap<>();
    private static final Map<ResourceLocation, WeaponDefinition> CODE = new LinkedHashMap<>();
    private static List<WeaponDefinition> resolved = List.of();

    private WeaponPool() {}

    /** 代码注册；建议在 mod 初始化阶段调用。同 ID 覆盖 JSON。 */
    public static synchronized void register(WeaponDefinition definition) {
        CODE.put(definition.id(), definition);
        rebuild();
    }

    /** 由 reload listener 调用；只替换 JSON 层，保留代码层。 */
    public static synchronized void acceptJson(List<WeaponDefinition> definitions) {
        JSON.clear();
        for (WeaponDefinition definition : definitions) {
            JSON.put(definition.id(), definition);
        }
        rebuild();
    }

    private static void rebuild() {
        Map<ResourceLocation, WeaponDefinition> merged = new LinkedHashMap<>(JSON);
        merged.putAll(CODE);
        List<WeaponDefinition> list = new ArrayList<>(merged.values());
        list.sort((a, b) -> Boolean.compare(b.codeRegistered(), a.codeRegistered()));
        resolved = List.copyOf(list);
    }

    public static List<WeaponDefinition> all() {
        return resolved;
    }

    public static int size() {
        return resolved.size();
    }

    /** 找到与手持物匹配的池条目；代码条目优先。 */
    @Nullable
    public static WeaponDefinition resolve(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        for (WeaponDefinition definition : resolved) {
            if (definition.matches(stack)) {
                return definition;
            }
        }
        return null;
    }

    /** 解析实际使用的武器 profile：代码覆盖 &gt; DataMap。 */
    @Nullable
    public static WeaponProfile resolveProfile(ItemStack stack) {
        WeaponDefinition definition = resolve(stack);
        if (definition != null && definition.profile() != null) {
            return definition.profile();
        }
        return OathbreakerDataMaps.weaponProfile(stack);
    }

    /** 按权重抽取一件武器池条目。weight &lt;= 0 的条目忽略。 */
    public static Optional<WeaponDefinition> roll(RandomSource random, Predicate<WeaponDefinition> filter) {
        List<WeaponDefinition> candidates = resolved.stream()
                .filter(filter)
                .filter(definition -> definition.weight() > 0)
                .toList();
        int total = candidates.stream().mapToInt(WeaponDefinition::weight).sum();
        if (total <= 0) {
            return Optional.empty();
        }
        int roll = random.nextInt(total);
        for (WeaponDefinition definition : candidates) {
            roll -= definition.weight();
            if (roll < 0) {
                return Optional.of(definition);
            }
        }
        return Optional.empty();
    }

    /** 武器命中后触发代码定义与 Item 自带的效果。 */
    public static void fireHit(ServerPlayer attacker, LivingEntity target, ItemStack weapon,
                               DamageProfile profile, float damageDealt, DamageSource source) {
        WeaponDefinition definition = resolve(weapon);
        HitContext context = new HitContext(attacker, target, weapon, definition,
                profile == null ? DamageProfile.PHYSICAL : profile, damageDealt, source, HitSource.WEAPON);

        HitEffect definitionEffect = definition != null ? definition.hitEffect() : null;
        if (definitionEffect != null) {
            definitionEffect.onHit(context);
        }

        if (weapon.getItem() instanceof WeaponHitProvider provider) {
            HitEffect providerEffect = provider.getWeaponHitEffect(weapon);
            if (providerEffect != null && providerEffect != definitionEffect) {
                providerEffect.onHit(context);
            }
        }
    }

    /** 目标死亡时触发 onKill（可选）；技能系统也可复用。 */
    public static void fireKill(ServerPlayer attacker, LivingEntity target, ItemStack weapon,
                                DamageProfile profile, DamageSource source) {
        WeaponDefinition definition = resolve(weapon);
        HitContext context = new HitContext(attacker, target, weapon, definition,
                profile == null ? DamageProfile.PHYSICAL : profile, 0.0F, source, HitSource.WEAPON);

        HitEffect definitionEffect = definition != null ? definition.hitEffect() : null;
        if (definitionEffect != null) {
            definitionEffect.onKill(context);
        }
        if (weapon.getItem() instanceof WeaponHitProvider provider) {
            HitEffect providerEffect = provider.getWeaponHitEffect(weapon);
            if (providerEffect != null && providerEffect != definitionEffect) {
                providerEffect.onKill(context);
            }
        }
    }
}

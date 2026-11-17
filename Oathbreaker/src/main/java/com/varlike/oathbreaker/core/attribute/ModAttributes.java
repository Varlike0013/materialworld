package com.varlike.oathbreaker.core.attribute;

import com.varlike.oathbreaker.Oathbreaker;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 自定义属性注册。
 *
 * <p>属性集合对齐 LoL 角色面板：AD 复用原版 attack_damage，AP、护甲/魔抗、
 * 暴击、穿透、技能急速、吸血、法力与回复走本 Mod 属性。
 * 数值尺度仍沿用 Minecraft（20 点生命），不照搬 LoL 的 600 血/60 AD。</p>
 */
public class ModAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(Registries.ATTRIBUTE, Oathbreaker.MOD_ID);

    /** 远程攻击伤害；保留自设计文档 4.1，P2 面板值优先使用 AD/AP。 */
    public static final DeferredHolder<Attribute, Attribute> RANGED_DAMAGE =
            ATTRIBUTES.register("generic.ranged_damage",
                    () -> new RangedAttribute("attribute.oathbreaker.ranged_damage", 0.0, 0.0, 2048.0)
                            .setSyncable(true));

    /** 法术强度：技能与法术武器的伤害系数。 */
    public static final DeferredHolder<Attribute, Attribute> ABILITY_POWER =
            ATTRIBUTES.register("generic.ability_power",
                    () -> new RangedAttribute("attribute.oathbreaker.ability_power", 0.0, 0.0, 10000.0)
                            .setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> CRIT_CHANCE =
            ATTRIBUTES.register("generic.crit_chance",
                    () -> new RangedAttribute("attribute.oathbreaker.crit_chance", 0.0, 0.0, 1.0)
                            .setSyncable(true));

    /** LoL 默认暴击伤害为 175%。 */
    public static final DeferredHolder<Attribute, Attribute> CRIT_DAMAGE =
            ATTRIBUTES.register("generic.crit_damage",
                    () -> new RangedAttribute("attribute.oathbreaker.crit_damage", 1.75, 0.0, 100.0)
                            .setSyncable(true));

    /** 生命回复，按“每 5 秒”理解。 */
    public static final DeferredHolder<Attribute, Attribute> HEALTH_REGENERATION =
            ATTRIBUTES.register("generic.health_regeneration",
                    () -> new RangedAttribute("attribute.oathbreaker.health_regeneration", 0.0, 0.0, 1000.0)
                            .setSyncable(true));

    /** 法力回复，按“每 5 秒”理解。 */
    public static final DeferredHolder<Attribute, Attribute> MANA_REGENERATION =
            ATTRIBUTES.register("generic.mana_regeneration",
                    () -> new RangedAttribute("attribute.oathbreaker.mana_regeneration", 0.0, 0.0, 1000.0)
                            .setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> MAX_MANA =
            ATTRIBUTES.register("generic.max_mana",
                    () -> new RangedAttribute("attribute.oathbreaker.max_mana", 0.0, 0.0, 10000.0)
                            .setSyncable(true));

    /** 固定护甲穿透。 */
    public static final DeferredHolder<Attribute, Attribute> ARMOR_PENETRATION =
            ATTRIBUTES.register("generic.armor_penetration",
                    () -> new RangedAttribute("attribute.oathbreaker.armor_penetration", 0.0, 0.0, 1000.0)
                            .setSyncable(true));

    /** 百分比护甲穿透，0–1。 */
    public static final DeferredHolder<Attribute, Attribute> ARMOR_PENETRATION_PERCENT =
            ATTRIBUTES.register("generic.armor_penetration_percent",
                    () -> new RangedAttribute("attribute.oathbreaker.armor_penetration_percent", 0.0, 0.0, 1.0)
                            .setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> MAGIC_RESIST =
            ATTRIBUTES.register("generic.magic_resist",
                    () -> new RangedAttribute("attribute.oathbreaker.magic_resist", 0.0, 0.0, 1000.0)
                            .setSyncable(true));

    /** 固定法术穿透。 */
    public static final DeferredHolder<Attribute, Attribute> MAGIC_PENETRATION =
            ATTRIBUTES.register("generic.magic_penetration",
                    () -> new RangedAttribute("attribute.oathbreaker.magic_penetration", 0.0, 0.0, 1000.0)
                            .setSyncable(true));

    /** 百分比法术穿透，0–1。 */
    public static final DeferredHolder<Attribute, Attribute> MAGIC_PENETRATION_PERCENT =
            ATTRIBUTES.register("generic.magic_penetration_percent",
                    () -> new RangedAttribute("attribute.oathbreaker.magic_penetration_percent", 0.0, 0.0, 1.0)
                            .setSyncable(true));

    /** 技能急速：冷却缩减 = AH / (100 + AH)。 */
    public static final DeferredHolder<Attribute, Attribute> ABILITY_HASTE =
            ATTRIBUTES.register("generic.ability_haste",
                    () -> new RangedAttribute("attribute.oathbreaker.ability_haste", 0.0, 0.0, 500.0)
                            .setSyncable(true));

    /** 生命偷取，仅作用于物理普攻，0–1。 */
    public static final DeferredHolder<Attribute, Attribute> LIFESTEAL =
            ATTRIBUTES.register("generic.lifesteal",
                    () -> new RangedAttribute("attribute.oathbreaker.lifesteal", 0.0, 0.0, 1.0)
                            .setSyncable(true));

    /** 全能吸血，作用于所有伤害类型，0–1。 */
    public static final DeferredHolder<Attribute, Attribute> OMNIVAMP =
            ATTRIBUTES.register("generic.omnivamp",
                    () -> new RangedAttribute("attribute.oathbreaker.omnivamp", 0.0, 0.0, 1.0)
                            .setSyncable(true));
}

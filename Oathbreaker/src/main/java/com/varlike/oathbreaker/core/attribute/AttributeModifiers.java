package com.varlike.oathbreaker.core.attribute;

import com.varlike.oathbreaker.Oathbreaker;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * 固定 ID 属性修饰符工具。
 *
 * <p>1.21.1 的 {@link AttributeModifier} 已从 UUID 改为 {@link ResourceLocation} 作为唯一 ID，
 * 因此设计文档中的“固定 UUID Modifier”统一按“固定 ResourceLocation ID”实现。
 * 同一 ID 重复施加前会先移除，避免旧层数叠加；移除时也只需 ID。</p>
 */
public final class AttributeModifiers {
    private AttributeModifiers() {}

    public static ResourceLocation id(String path) {
        return Oathbreaker.id(path);
    }

    /** 局内临时修饰符：技能、阶段快照等，死亡重生后需要重新施加。 */
    public static void applyTransient(LivingEntity entity, Holder<Attribute> attribute, ResourceLocation id,
                                      double amount, AttributeModifier.Operation operation) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        instance.removeModifier(id);
        instance.addTransientModifier(new AttributeModifier(id, amount, operation));
    }

    /** 局外永久修饰符：契约天赋、永久属性点等，登录/重生时统一重建。 */
    public static void applyPermanent(LivingEntity entity, Holder<Attribute> attribute, ResourceLocation id,
                                      double amount, AttributeModifier.Operation operation) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        instance.removeModifier(id);
        instance.addPermanentModifier(new AttributeModifier(id, amount, operation));
    }

    public static void remove(LivingEntity entity, Holder<Attribute> attribute, ResourceLocation id) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance != null) {
            instance.removeModifier(id);
        }
    }

    public static boolean has(LivingEntity entity, Holder<Attribute> attribute, ResourceLocation id) {
        AttributeInstance instance = entity.getAttribute(attribute);
        return instance != null && instance.hasModifier(id);
    }
}

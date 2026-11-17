package com.varlike.oathbreaker.core.weapon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.varlike.oathbreaker.Oathbreaker;
import com.varlike.oathbreaker.combat.hit.HitEffect;
import com.varlike.oathbreaker.core.classification.WeaponProfile;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * 武器池条目。
 *
 * <p>JSON 条目只描述“池成员/权重/池标签”，不携带 profile 与命中效果，
 * 因此不会修改武器本身的伤害或行为；代码注册的条目可以带
 * {@link WeaponProfile} 与 {@link HitEffect}，从而覆盖/扩展武器效果。</p>
 */
public record WeaponDefinition(
        ResourceLocation id,
        Optional<ResourceLocation> item,
        Optional<ResourceLocation> itemTag,
        int weight,
        Set<String> poolTags,
        @Nullable WeaponProfile profile,
        @Nullable HitEffect hitEffect,
        boolean codeRegistered
) {
    /** JSON 只读字段；id 来自文件路径，profile/hitEffect 不参与 JSON 解码。 */
    public static final Codec<WeaponDefinition> JSON_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.optionalFieldOf("item").forGetter(WeaponDefinition::item),
            ResourceLocation.CODEC.optionalFieldOf("tag").forGetter(WeaponDefinition::itemTag),
            Codec.INT.optionalFieldOf("weight", 10).forGetter(WeaponDefinition::weight),
            Codec.STRING.listOf().optionalFieldOf("pool_tags", List.of())
                    .forGetter(definition -> List.copyOf(definition.poolTags()))
    ).apply(instance, (item, tag, weight, tags) -> new WeaponDefinition(
            Oathbreaker.id("unbound"), item, tag, weight, Set.copyOf(tags), null, null, false)));

    public WeaponDefinition {
        weight = Math.max(0, weight);
        poolTags = Set.copyOf(poolTags);
    }

    public WeaponDefinition withId(ResourceLocation newId) {
        return new WeaponDefinition(newId, item, itemTag, weight, poolTags, profile, hitEffect, codeRegistered);
    }

    public boolean matches(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (item.isPresent() && BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(item.get())) {
            return true;
        }
        return itemTag.map(tag -> stack.is(TagKey.create(Registries.ITEM, tag))).orElse(false);
    }

    public static Builder builder(ResourceLocation id) {
        return new Builder(id);
    }

    public static final class Builder {
        private final ResourceLocation id;
        private final Set<String> poolTags = new LinkedHashSet<>();
        private Optional<ResourceLocation> item = Optional.empty();
        private Optional<ResourceLocation> itemTag = Optional.empty();
        private int weight = 10;
        private WeaponProfile profile;
        private HitEffect hitEffect;

        private Builder(ResourceLocation id) {
            this.id = id;
        }

        public Builder item(ResourceLocation itemId) {
            this.item = Optional.of(itemId);
            return this;
        }

        public Builder tag(ResourceLocation itemTagId) {
            this.itemTag = Optional.of(itemTagId);
            return this;
        }

        public Builder weight(int weight) {
            this.weight = weight;
            return this;
        }

        public Builder poolTag(String tag) {
            this.poolTags.add(tag);
            return this;
        }

        public Builder profile(WeaponProfile profile) {
            this.profile = profile;
            return this;
        }

        public Builder hitEffect(HitEffect hitEffect) {
            this.hitEffect = hitEffect;
            return this;
        }

        public WeaponDefinition build() {
            if (item.isEmpty() && itemTag.isEmpty()) {
                throw new IllegalStateException("Weapon definition " + id + " must target an item or item tag");
            }
            return new WeaponDefinition(id, item, itemTag, Math.max(0, weight),
                    new HashSet<>(poolTags), profile, hitEffect, true);
        }
    }
}

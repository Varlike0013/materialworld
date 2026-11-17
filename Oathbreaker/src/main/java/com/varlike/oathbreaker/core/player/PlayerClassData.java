package com.varlike.oathbreaker.core.player;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;

/**
 * 玩家当前职业与职业标签。
 *
 * <p>注意：设计文档里的“固定 UUID Modifier”针对 1.21.1 已不适用，属性修饰符使用
 * {@link ResourceLocation} 作为固定 ID；职业本身同样用 {@link ResourceLocation} 标识，
 * 例如 {@code oathbreaker:ranger}。</p>
 *
 * <p>这是 P1 的最小实现，只服务于“非本职业武器 1 点伤害”的判定；
 * 完整职业解锁列表在后续的账户层（AccountSavedData）中维护。</p>
 */
public record PlayerClassData(Optional<ResourceLocation> activeClass, Set<ResourceLocation> classTags) {
    public static final PlayerClassData EMPTY = new PlayerClassData(Optional.empty(), Set.of());

    private static final Codec<Set<ResourceLocation>> CLASS_TAG_SET_CODEC =
            ResourceLocation.CODEC.listOf().xmap(Set::copyOf, List::copyOf);

    public static final Codec<PlayerClassData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.optionalFieldOf("active_class").forGetter(PlayerClassData::activeClass),
            CLASS_TAG_SET_CODEC.optionalFieldOf("class_tags", Set.of()).forGetter(PlayerClassData::classTags)
    ).apply(instance, PlayerClassData::new));

    public PlayerClassData {
        classTags = Set.copyOf(classTags);
    }

    public PlayerClassData withClass(ResourceLocation classId) {
        return new PlayerClassData(Optional.of(classId), classTags);
    }

    public PlayerClassData withoutClass() {
        return new PlayerClassData(Optional.empty(), classTags);
    }

    public PlayerClassData withTag(ResourceLocation tag) {
        Set<ResourceLocation> tags = new HashSet<>(classTags);
        tags.add(tag);
        return new PlayerClassData(activeClass, tags);
    }

    public PlayerClassData withoutTag(ResourceLocation tag) {
        Set<ResourceLocation> tags = new HashSet<>(classTags);
        tags.remove(tag);
        return new PlayerClassData(activeClass, tags);
    }

    public PlayerClassData withTags(Set<ResourceLocation> tags) {
        return new PlayerClassData(activeClass, tags);
    }

    public boolean hasClass(ResourceLocation classId) {
        return activeClass.filter(classId::equals).isPresent();
    }

    public boolean hasAnyTag(Set<ResourceLocation> tags) {
        return classTags.stream().anyMatch(tags::contains);
    }
}

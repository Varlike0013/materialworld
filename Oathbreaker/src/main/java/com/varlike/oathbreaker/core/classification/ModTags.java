package com.varlike.oathbreaker.core.classification;

import com.varlike.oathbreaker.Oathbreaker;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

/**
 * 分类 Tag 常量。
 *
 * <p>Tag 只做布尔路由（“是不是远程武器 / 是不是精英”），富配置走 DataMap。
 * 技能与伤害管线只查询这里的常量，不认识具体物品或实体 ID。</p>
 */
public final class ModTags {
    private ModTags() {}

    public static final class Items {
        public static final TagKey<Item> WEAPON_MELEE = create("weapon_melee");
        public static final TagKey<Item> WEAPON_HEAVY = create("weapon_heavy");
        public static final TagKey<Item> WEAPON_RANGED = create("weapon_ranged");
        public static final TagKey<Item> WEAPON_MAGIC = create("weapon_magic");

        private static TagKey<Item> create(String path) {
            return TagKey.create(Registries.ITEM, Oathbreaker.id(path));
        }

        private Items() {}
    }

    public static final class EntityTypes {
        public static final TagKey<EntityType<?>> COMMON = create("common");
        public static final TagKey<EntityType<?>> ELITE = create("elite");
        public static final TagKey<EntityType<?>> BOSS = create("boss");
        public static final TagKey<EntityType<?>> UNDEAD = create("undead");

        private static TagKey<EntityType<?>> create(String path) {
            return TagKey.create(Registries.ENTITY_TYPE, Oathbreaker.id(path));
        }

        private EntityTypes() {}
    }

    public static final class DamageTypes {
        public static final TagKey<DamageType> PHYSICAL = create("physical");
        public static final TagKey<DamageType> MAGIC = create("magic");
        public static final TagKey<DamageType> TRUE = create("true");

        private static TagKey<DamageType> create(String path) {
            return TagKey.create(Registries.DAMAGE_TYPE, Oathbreaker.id(path));
        }

        private DamageTypes() {}
    }
}

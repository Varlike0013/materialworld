package com.varlike.oathbreaker.core.classification;

import com.varlike.oathbreaker.Oathbreaker;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;
import org.jetbrains.annotations.Nullable;

/**
 * 分类层 DataMap 注册与查询入口。
 *
 * <p>JSON 路径：{@code data/oathbreaker/data_maps/item/weapon_profile.json}、
 * {@code data/oathbreaker/data_maps/entity_type/mob_profile.json}。</p>
 */
@EventBusSubscriber(modid = Oathbreaker.MOD_ID)
public final class OathbreakerDataMaps {
    /**
     * 武器分类。同步到客户端，后续 Tooltip 与 HUD 直接读同一份数据。
     * mandatory=false： vanilla 客户端仍可连接，只是看不到分类信息。
     */
    public static final DataMapType<Item, WeaponProfile> WEAPON_PROFILE = DataMapType
            .<WeaponProfile, Item>builder(Oathbreaker.id("weapon_profile"), Registries.ITEM, WeaponProfile.CODEC)
            .synced(WeaponProfile.CODEC, false)
            .build();

    public static final DataMapType<EntityType<?>, MobProfile> MOB_PROFILE = DataMapType
            .<MobProfile, EntityType<?>>builder(Oathbreaker.id("mob_profile"), Registries.ENTITY_TYPE, MobProfile.CODEC)
            .build();

    private OathbreakerDataMaps() {}

    @SubscribeEvent
    public static void register(RegisterDataMapTypesEvent event) {
        event.register(WEAPON_PROFILE);
        event.register(MOB_PROFILE);
    }

    /** 数据包加载完成后输出条目数，便于确认 JSON 是否被正确解析。 */
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        Oathbreaker.LOGGER.debug("Loaded data maps: weapon_profile entries={}, mob_profile entries={}",
                BuiltInRegistries.ITEM.getDataMap(WEAPON_PROFILE).size(),
                BuiltInRegistries.ENTITY_TYPE.getDataMap(MOB_PROFILE).size());
    }

    @Nullable
    public static WeaponProfile weaponProfile(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        return stack.getItemHolder().getData(WEAPON_PROFILE);
    }

    @Nullable
    public static MobProfile mobProfile(EntityType<?> entityType) {
        return BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(entityType).getData(MOB_PROFILE);
    }
}

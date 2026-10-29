package com.varlike.spiritworld.entity;

import com.varlike.spiritworld.SpiritWorld;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, SpiritWorld.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<ManaAlloyArrow>> MANA_ALLOY_ARROW =
            ENTITIES.register("mana_alloy_arrow", () -> EntityType.Builder
                    .<ManaAlloyArrow>of(ManaAlloyArrow::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .build("mana_alloy_arrow"));

    public static void register(IEventBus modEventBus) {
        ENTITIES.register(modEventBus);
    }
}
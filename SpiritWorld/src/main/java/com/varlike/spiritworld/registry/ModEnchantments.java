package com.varlike.spiritworld.registry;

import com.varlike.spiritworld.SpiritWorld;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

public final class ModEnchantments {

    private ModEnchantments() {}

    /** 双发快射：弓射箭时额外射出一根小箭。 */
    public static final ResourceKey<Enchantment> DOUBLE_SHOT = ResourceKey.create(
            Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(SpiritWorld.MOD_ID, "double_shot"));
}
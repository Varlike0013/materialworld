package com.varlike.spiritworld.registry;

import com.varlike.spiritworld.SpiritWorld;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.List;

public class ModArmorMaterials {

    /**
     * 魔力合金盔甲：防御点数同钻石（3/8/6/3），toughness 2.5、knockbackResistance 0.05。
     * 介于钻石（toughness 2.0、kb 0）和下界合金（toughness 3.0、kb 0.1）之间。
     */
    public static final Holder<ArmorMaterial> MANA_ALLOY = Holder.direct(new ArmorMaterial(
            Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                map.put(ArmorItem.Type.HELMET, 3);
                map.put(ArmorItem.Type.CHESTPLATE, 8);
                map.put(ArmorItem.Type.LEGGINGS, 6);
                map.put(ArmorItem.Type.BOOTS, 3);
                map.put(ArmorItem.Type.BODY, 11);
            }),
            12,
            SoundEvents.ARMOR_EQUIP_DIAMOND,
            () -> Ingredient.of(ModItems.MANA_ALLOY.get()),
            List.of(new ArmorMaterial.Layer(
                    ResourceLocation.fromNamespaceAndPath(SpiritWorld.MOD_ID, "mana_alloy"))),
            2.5f,
            0.05f
    ));
}
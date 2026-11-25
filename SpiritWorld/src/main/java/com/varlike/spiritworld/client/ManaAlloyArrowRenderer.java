package com.varlike.spiritworld.client;

import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.entity.ManaAlloyArrow;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/** 魔力合金箭渲染器：使用 Mod 自带的合金箭纹理，与普通箭明显区分。 */
public class ManaAlloyArrowRenderer extends ArrowRenderer<ManaAlloyArrow> {

    public static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(SpiritWorld.MOD_ID, "textures/item/mana_alloy_arrow.png");

    public ManaAlloyArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(ManaAlloyArrow entity) {
        return TEXTURE;
    }
}
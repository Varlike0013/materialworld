package com.varlike.spiritworld.client;

import com.varlike.spiritworld.entity.ManaAlloyArrow;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * 魔力合金箭渲染器：使用原版箭的纹理，暂不做自定义外观。
 * 后续可替换为自定义纹理（如带紫色光效的箭）。
 */
public class ManaAlloyArrowRenderer extends ArrowRenderer<ManaAlloyArrow> {

    /** 原版箭纹理，占位用 */
    public static final ResourceLocation TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/entity/projectiles/arrow.png");

    public ManaAlloyArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(ManaAlloyArrow entity) {
        return TEXTURE;
    }
}
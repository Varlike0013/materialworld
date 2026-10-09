package com.varlike.materialworld.compat.jei;

import com.varlike.materialworld.MaterialWorld;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.constants.RecipeTypes;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public class MaterialWorldJEIPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(MaterialWorld.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        // 为原版工作台注册我们的自定义EMC转移处理器
        registration.addRecipeTransferHandler(
                new EMCRecipeTransferHandler(registration.getTransferHelper()),
                RecipeTypes.CRAFTING
        );
    }
}
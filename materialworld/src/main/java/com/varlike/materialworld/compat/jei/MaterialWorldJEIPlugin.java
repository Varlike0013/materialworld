package com.varlike.materialworld.compat.jei;

import com.varlike.materialworld.MaterialWorld;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public class MaterialWorldJEIPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(MaterialWorld.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        // 为原版工作台注册自定义 EMC 转移处理器
        registration.addRecipeTransferHandler(
                new EMCRecipeTransferHandler(registration.getTransferHelper()),
                RecipeTypes.CRAFTING
        );
    }
}
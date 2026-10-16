package com.varlike.MwConstructors.datagen;

import com.varlike.MwConstructors.MwConstructorsMod;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModLanguageProviderZH extends LanguageProvider {

    public ModLanguageProviderZH(PackOutput output) {
        super(output, MwConstructorsMod.MOD_ID, "zh_cn");
    }

    @Override
    protected void addTranslations() {
        this.add("itemGroup.examplemod", "物质世界：构建者");

        final String id = MwConstructorsMod.MOD_ID;
        this.add(id + ".configuration.title", "构建者配置");
        this.add(id + ".configuration.section." + id + ".common.toml", "构建者通用配置");
        this.add(id + ".configuration.section." + id + ".common.toml.title", "构建者通用配置");
    }
}
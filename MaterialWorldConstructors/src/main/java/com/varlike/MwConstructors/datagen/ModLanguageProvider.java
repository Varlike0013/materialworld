package com.varlike.MwConstructors.datagen;

import com.varlike.MwConstructors.MwConstructorsMod;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModLanguageProvider extends LanguageProvider {

    public ModLanguageProvider(PackOutput output) {
        super(output, MwConstructorsMod.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        // 创造模式标签（键要与 MwConstructorsMod 里的 Component.translatable 一致）
        this.add("itemGroup.examplemod", "Material World: Constructors");

        // 配置界面
        final String id = MwConstructorsMod.MOD_ID;
        this.add(id + ".configuration.title", "Constructors Configs");
        this.add(id + ".configuration.section." + id + ".common.toml", "Constructors Common Configs");
        this.add(id + ".configuration.section." + id + ".common.toml.title", "Constructors Common Configs");
    }
}
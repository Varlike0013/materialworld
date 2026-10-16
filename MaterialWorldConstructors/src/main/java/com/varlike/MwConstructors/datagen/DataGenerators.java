package com.varlike.MwConstructors.datagen;

import com.varlike.MwConstructors.MwConstructorsMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = MwConstructorsMod.MOD_ID)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        var output = generator.getPackOutput();
        var existingFileHelper = event.getExistingFileHelper();
        var lookupProvider = event.getLookupProvider();

        // 客户端资源
        generator.addProvider(event.includeClient(),
                new ModBlockStateProvider(output, existingFileHelper));
        generator.addProvider(event.includeClient(),
                new ModItemModelProvider(output, existingFileHelper));
        generator.addProvider(event.includeClient(),
                new ModLanguageProvider(output));
        generator.addProvider(event.includeClient(),
                new ModLanguageProviderZH(output));

        // 服务端资源
        generator.addProvider(event.includeServer(),
                new ModRecipeProvider(output, lookupProvider));
    }
}
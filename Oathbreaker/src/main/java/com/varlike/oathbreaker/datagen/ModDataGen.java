package com.varlike.oathbreaker.datagen;

import com.varlike.oathbreaker.Oathbreaker;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = Oathbreaker.MOD_ID)
public class ModDataGen {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        var output = generator.getPackOutput();

        // 语言文件是客户端资源
        generator.addProvider(event.includeClient(),
                new ModLanguageProvider(output, "en_us"));
        generator.addProvider(event.includeClient(),
                new ModLanguageProvider(output, "zh_cn"));
    }
}
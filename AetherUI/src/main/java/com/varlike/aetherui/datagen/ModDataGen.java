package com.varlike.aetherui.datagen;

import com.varlike.aetherui.AetherUIMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/** 数据生成器入口：生成 en_us 与 zh_cn 双语言文件。 */
@EventBusSubscriber(modid = AetherUIMod.MOD_ID)
public class ModDataGen {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        var output = generator.getPackOutput();

        generator.addProvider(event.includeClient(),
                new ModLanguageProvider(output, "en_us"));
        generator.addProvider(event.includeClient(),
                new ModLanguageProvider(output, "zh_cn"));
    }
}

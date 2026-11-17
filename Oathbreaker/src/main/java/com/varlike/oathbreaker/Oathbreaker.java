package com.varlike.oathbreaker;

import com.varlike.oathbreaker.core.attribute.ModAttributes;
import com.varlike.oathbreaker.core.player.PlayerClasses;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

@Mod(Oathbreaker.MOD_ID)
public class Oathbreaker {
    public static final String MOD_ID = "oathbreaker";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Oathbreaker(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Oathbreaker initializing...");
        ModAttributes.ATTRIBUTES.register(modEventBus);
        PlayerClasses.ATTACHMENT_TYPES.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.COMMON, OathbreakerConfig.SPEC);
    }

    /** 统一构造本 Mod 的命名空间 ID，避免各处重复写 modid。 */
    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}

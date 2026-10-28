package com.varlike.spiritworld.registry;

import com.varlike.spiritworld.SpiritWorld;
import com.varlike.spiritworld.client.SpiritCoreMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenus {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, SpiritWorld.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<SpiritCoreMenu>> SPIRIT_CORE =
            MENUS.register("spirit_core", () -> IMenuTypeExtension.create(SpiritCoreMenu::new));

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }
}
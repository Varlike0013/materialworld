package com.varlike.materialworld;

import com.mojang.brigadier.arguments.LongArgumentType;
import com.varlike.materialworld.emc.EMCManager;
import com.varlike.materialworld.emc.EMCRecipeCalculator;
import com.varlike.materialworld.emc.EMCStorage;
import com.varlike.materialworld.emc.EMCValues;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(MaterialWorld.MOD_ID)
public class MaterialWorld
{
    public static final String MOD_ID = "materialworld";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB, MOD_ID);

    // 示例方块（后续可删）
    public static final RegistryObject<Block> EXAMPLE_BLOCK = BLOCKS.register("example_block",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)));
    public static final RegistryObject<Item> EXAMPLE_BLOCK_ITEM = ITEMS.register("example_block",
            () -> new BlockItem(EXAMPLE_BLOCK.get(), new Item.Properties()));

    public MaterialWorld(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();

        modEventBus.addListener(this::commonSetup);
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addCreative);
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        // 新增：注册网络通道
        com.varlike.materialworld.network.NetworkHandler.register();
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        LOGGER.info("物质世界模组加载中...");

        // 1. 注册默认值
        EMCValues.registerDefaults();

        // 2. 初始化存储路径并加载自定义值（会覆盖默认值）
        EMCStorage.init(FMLPaths.CONFIGDIR.get());
        EMCStorage.load();

        LOGGER.info("EMC 系统初始化完成，共 {} 种物品", EMCManager.size());
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS)
            event.accept(EXAMPLE_BLOCK_ITEM);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        LOGGER.info("物质世界：服务器启动");
    }


    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            LOGGER.info("物质世界：客户端初始化");
        }
    }

    // 新增：Forge 事件总线（命令注册）
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeEvents {
        @SubscribeEvent
        public static void onServerStarted(ServerStartedEvent event) {
            int count = EMCRecipeCalculator.calculate(event.getServer());
            if (count > 0) {
                EMCStorage.save(); // 把推导结果写回文件
            }
        }
        @SubscribeEvent
        public static void onRegisterCommands(RegisterCommandsEvent event) {
            event.getDispatcher().register(
                    Commands.literal("emc")
                            .requires(source -> source.hasPermission(2))
                            // /emc hand —— 查询手上的物品 EMC
                            .then(Commands.literal("hand")
                                    .executes(ctx -> {
                                        var player = ctx.getSource().getPlayerOrException();
                                        var stack = player.getMainHandItem();
                                        if (stack.isEmpty()) {
                                            ctx.getSource().sendFailure(Component.literal("请手持一个物品"));
                                            return 0;
                                        }
                                        long emc = EMCManager.getEMC(stack.getItem());
                                        String name = stack.getItem().getDescription().getString();
                                        ctx.getSource().sendSuccess(
                                                () -> Component.literal(name + " 的 EMC = " + emc), false
                                        );
                                        return 1;
                                    })
                            )
                            // /emc set <值> —— 设置手上物品的 EMC 并保存
                            .then(Commands.literal("set")
                                    .then(Commands.argument("value", LongArgumentType.longArg(0))
                                            .executes(ctx -> {
                                                var player = ctx.getSource().getPlayerOrException();
                                                var stack = player.getMainHandItem();
                                                if (stack.isEmpty()) {
                                                    ctx.getSource().sendFailure(Component.literal("请手持一个物品"));
                                                    return 0;
                                                }
                                                long value = LongArgumentType.getLong(ctx, "value");
                                                Item item = stack.getItem();
                                                EMCManager.setEMC(item, value);
                                                EMCStorage.save();  // 立即存盘

                                                String name = item.getDescription().getString();
                                                ctx.getSource().sendSuccess(
                                                        () -> Component.literal("已将 " + name + " 的 EMC 设置为 " + value), true
                                                );
                                                return 1;
                                            })
                                    )
                            )
                            // ===== /emc reset =====
                            .then(Commands.literal("reset")
                                    .executes(ctx -> {
                                        var player = ctx.getSource().getPlayerOrException();
                                        var stack = player.getMainHandItem();
                                        if (stack.isEmpty()) {
                                            ctx.getSource().sendFailure(Component.literal("请手持一个物品"));
                                            return 0;
                                        }
                                        Item item = stack.getItem();
                                        String name = item.getDescription().getString();

                                        if (EMCValues.hasDefault(item)) {
                                            long def = EMCValues.getDefault(item);
                                            EMCManager.setEMC(item, def);
                                            EMCStorage.save();
                                            ctx.getSource().sendSuccess(
                                                    () -> Component.literal("已将 " + name + " 的 EMC 重置为默认值 " + def), true
                                            );
                                        } else {
                                            EMCManager.remove(item);
                                            EMCStorage.save();
                                            ctx.getSource().sendSuccess(
                                                    () -> Component.literal(name + " 没有默认 EMC，已从映射表中移除"), true
                                            );
                                        }
                                        return 1;
                                    })
                            )

                            // ===== /emc reload =====
                            .then(Commands.literal("reload")
                                    .executes(ctx -> {
                                        // 1. 清空当前映射
                                        EMCManager.clear();
                                        // 2. 重新应用默认值
                                        EMCValues.registerDefaults();
                                        // 3. 从文件重新加载自定义值（覆盖默认值）
                                        EMCStorage.load();

                                        int size = EMCManager.size();
                                        ctx.getSource().sendSuccess(
                                                () -> Component.literal("EMC 已重载，共 " + size + " 条数据"), true
                                        );
                                        return 1;
                                    })
                            )
                            .then(Commands.literal("recalc")
                                    .executes(ctx -> {
                                        int count = EMCRecipeCalculator.calculate(ctx.getSource().getServer());
                                        EMCStorage.save();
                                        ctx.getSource().sendSuccess(
                                                () -> Component.literal("§a从合成表推导出 " + count + " 个新的 EMC 值"), true);
                                        return 1;
                                    })
                            )
            );
        }
        @SubscribeEvent
        public static void onItemTooltip(ItemTooltipEvent event) {
            ItemStack stack = event.getItemStack();
            // 空物品不处理
            if (stack.isEmpty()) return;

            long emc = EMCManager.getEMC(stack.getItem());
            // 只显示有 EMC 值的物品
            if (emc <= 0) return;

            Component line = Component.translatable("tooltip.materialworld.emc",
                            Component.literal(String.valueOf(emc)).withStyle(ChatFormatting.GOLD))
                    .withStyle(ChatFormatting.GREEN);
            event.getToolTip().add(line);
        }
    }
}
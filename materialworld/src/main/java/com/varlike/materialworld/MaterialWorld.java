package com.varlike.materialworld;

import com.mojang.brigadier.arguments.LongArgumentType;
import com.varlike.materialworld.capability.ModCapabilities;
import com.varlike.materialworld.emc.EMCManager;
import com.varlike.materialworld.emc.EMCRecipeCalculator;
import com.varlike.materialworld.emc.EMCStorage;
import com.varlike.materialworld.emc.EMCValues;
import com.mojang.logging.LogUtils;
import com.varlike.materialworld.network.NetworkHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(MaterialWorld.MOD_ID)
public class MaterialWorld
{
    public static final String MOD_ID = "materialworld";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public MaterialWorld(IEventBus modEventBus, ModContainer modContainer)
    {
        modEventBus.addListener(this::commonSetup);

        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("物质世界模组加载中...");

        // 加载配置值（此时配置已完全就绪）
        Config.loadValues();

        // 1. 注册默认值
        EMCValues.registerDefaults();

        // 2. 初始化存储路径并加载自定义值
        EMCStorage.init(FMLPaths.CONFIGDIR.get());
        EMCStorage.load();

        LOGGER.info("EMC 系统初始化完成，共 {} 种物品", EMCManager.size());
    }
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        LOGGER.info("物质世界：服务器启动");
    }

    // NeoForge 游戏事件总线（命令注册）
    @EventBusSubscriber(modid = MOD_ID)
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
                                            ctx.getSource().sendFailure(Component.translatable("message.materialworld.hold_item"));
                                            return 0;
                                        }
                                        long emc = EMCManager.getEMC(stack.getItem());
                                        String name = stack.getItem().getDescription().getString();
                                        ctx.getSource().sendSuccess(
                                                () -> Component.translatable("message.materialworld.emc_query", name, emc),
                                                false);
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
                                                    ctx.getSource().sendFailure(Component.translatable("message.materialworld.hold_item"));
                                                    return 0;
                                                }
                                                long value = LongArgumentType.getLong(ctx, "value");
                                                Item item = stack.getItem();
                                                EMCManager.setEMC(item, value);
                                                EMCStorage.save();  // 立即存盘

                                                String name = item.getDescription().getString();
                                                ctx.getSource().sendSuccess(
                                                        () -> Component.translatable("message.materialworld.emc_set", name, value), true
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
                                            ctx.getSource().sendFailure(Component.translatable("message.materialworld.hold_item"));
                                            return 0;
                                        }
                                        Item item = stack.getItem();
                                        String name = item.getDescription().getString();

                                        if (EMCValues.hasDefault(item)) {
                                            long def = EMCValues.getDefault(item);
                                            EMCManager.setEMC(item, def);
                                            EMCStorage.save();
                                            ctx.getSource().sendSuccess(
                                                    () -> Component.translatable("message.materialworld.emc_reset_default", name, def), true
                                            );
                                        } else {
                                            EMCManager.remove(item);
                                            EMCStorage.save();
                                            ctx.getSource().sendSuccess(
                                                    () -> Component.translatable("message.materialworld.emc_no_default", name), true
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
                                                () -> Component.translatable("message.materialworld.emc_reloaded", size), true
                                        );
                                        return 1;
                                    })
                            )
                            .then(Commands.literal("recalc")
                                    .executes(ctx -> {
                                        int count = EMCRecipeCalculator.calculate(ctx.getSource().getServer());
                                        EMCStorage.save();
                                        ctx.getSource().sendSuccess(
                                                () -> Component.translatable("message.materialworld.emc_recalc", count), true);
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
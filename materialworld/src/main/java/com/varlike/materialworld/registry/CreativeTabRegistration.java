package com.varlike.materialworld.registry;

import com.varlike.materialworld.MaterialWorld;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber(modid = MaterialWorld.MOD_ID)
public class CreativeTabRegistration {

    public static final ResourceLocation ADDON_TAB_ID =
            ResourceLocation.fromNamespaceAndPath(MaterialWorld.MOD_ID, "addons");

    @SubscribeEvent
    public static void onRegister(RegisterEvent event) {
        event.register(Registries.CREATIVE_MODE_TAB, helper -> {

            if (AddonCreativeTab.isEmpty()) {
                MaterialWorld.LOGGER.info("未检测到附属物品，跳过附属创造标签的注册");
                return;
            }

            CreativeModeTab tab = CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.materialworld.addons"))
                    .icon(() -> {
                        var supplier = AddonCreativeTab.getIconSupplier();
                        return supplier != null ? supplier.get() : new ItemStack(Items.NETHER_STAR);
                    })
                    .displayItems((params, output) -> {
                        String currentGroup = null;
                        for (AddonCreativeTab.Entry entry : AddonCreativeTab.getAll()) {
                            String group = entry.groupName();

                            // 分组切换时插入一个标题锚点
                            if (group != null && !group.equals(currentGroup)) {
                                currentGroup = group;
                                output.accept(createGroupHeader(group));
                            } else if (group == null) {
                                currentGroup = null;
                            }

                            ItemStack stack = entry.supplier().get();
                            if (stack != null && !stack.isEmpty()) {
                                output.accept(stack);
                            }
                        }
                    })
                    .build();

            helper.register(ADDON_TAB_ID, tab);
            MaterialWorld.LOGGER.info("已注册附属创造标签，共 {} 项", AddonCreativeTab.getAll().size());
        });
    }

    /**
     * 创建分组标题锚点。
     * 使用纸张作为载体，加粗金色名称。
     * 若 groupName 含 "."，视为翻译键；否则视为字面文本。
     */
    private static ItemStack createGroupHeader(String groupName) {
        ItemStack stack = new ItemStack(Items.PAPER);
        Component name = (groupName.contains(".")
                ? Component.translatable(groupName)
                : Component.literal(groupName))
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
        stack.set(DataComponents.CUSTOM_NAME, name);
        return stack;
    }
}
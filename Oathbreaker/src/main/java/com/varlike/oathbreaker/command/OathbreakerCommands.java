package com.varlike.oathbreaker.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.varlike.oathbreaker.Oathbreaker;
import com.varlike.oathbreaker.combat.CombatStats;
import com.varlike.oathbreaker.combat.DamagePipeline;
import com.varlike.oathbreaker.combat.hit.HitEffect;
import com.varlike.oathbreaker.combat.hit.WeaponHitProvider;
import com.varlike.oathbreaker.core.classification.WeaponProfile;
import com.varlike.oathbreaker.core.player.PlayerClassData;
import com.varlike.oathbreaker.core.player.PlayerClasses;
import com.varlike.oathbreaker.core.weapon.WeaponDefinition;
import com.varlike.oathbreaker.core.weapon.WeaponPool;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * 开发调试命令。
 *
 * <p>P1 用它来验证职业武器适配与分类层，不需要先做 UI：</p>
 * <pre>
 * /oathbreaker class set oathbreaker:ranger
 * /oathbreaker class get
 * /oathbreaker class tag add oathbreaker:arcane
 * /oathbreaker profile
 * </pre>
 */
@EventBusSubscriber(modid = Oathbreaker.MOD_ID)
public final class OathbreakerCommands {
    private OathbreakerCommands() {}

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("oathbreaker")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("class")
                                .then(Commands.literal("set")
                                        .then(Commands.argument("class", ResourceLocationArgument.id())
                                                .executes(context -> setClass(context,
                                                        ResourceLocationArgument.getId(context, "class")))))
                                .then(Commands.literal("clear")
                                        .executes(OathbreakerCommands::clearClass))
                                .then(Commands.literal("get")
                                        .executes(OathbreakerCommands::getClass))
                                .then(Commands.literal("tag")
                                        .then(Commands.literal("add")
                                                .then(Commands.argument("tag", ResourceLocationArgument.id())
                                                        .executes(context -> addTag(context,
                                                                ResourceLocationArgument.getId(context, "tag")))))
                                        .then(Commands.literal("remove")
                                                .then(Commands.argument("tag", ResourceLocationArgument.id())
                                                        .executes(context -> removeTag(context,
                                                                ResourceLocationArgument.getId(context, "tag")))))
                                        .then(Commands.literal("clear")
                                                .executes(OathbreakerCommands::clearTags))))
                        .then(Commands.literal("profile")
                                .executes(OathbreakerCommands::profile))
                        .then(Commands.literal("stats")
                                .executes(OathbreakerCommands::stats))
                        .then(Commands.literal("pool")
                                .executes(OathbreakerCommands::pool)));
    }

    private static int setClass(CommandContext<CommandSourceStack> context, ResourceLocation classId)
            throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        PlayerClasses.set(player, PlayerClasses.get(player).withClass(classId));
        context.getSource().sendSuccess(() -> Component.literal("当前职业已设为 " + classId), true);
        return 1;
    }

    private static int clearClass(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        PlayerClasses.set(player, PlayerClasses.get(player).withoutClass());
        context.getSource().sendSuccess(() -> Component.literal("已清除当前职业"), true);
        return 1;
    }

    private static int getClass(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        PlayerClassData data = PlayerClasses.get(player);
        String classText = data.activeClass().map(ResourceLocation::toString).orElse("<未设置>");
        context.getSource().sendSuccess(
                () -> Component.literal("当前职业：" + classText + "；职业标签：" + data.classTags()), false);
        return 1;
    }

    private static int addTag(CommandContext<CommandSourceStack> context, ResourceLocation tag)
            throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        PlayerClasses.set(player, PlayerClasses.get(player).withTag(tag));
        context.getSource().sendSuccess(() -> Component.literal("已添加职业标签 " + tag), true);
        return 1;
    }

    private static int removeTag(CommandContext<CommandSourceStack> context, ResourceLocation tag)
            throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        PlayerClasses.set(player, PlayerClasses.get(player).withoutTag(tag));
        context.getSource().sendSuccess(() -> Component.literal("已移除职业标签 " + tag), true);
        return 1;
    }

    private static int clearTags(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        PlayerClasses.set(player, PlayerClasses.get(player).withTags(Set.of()));
        context.getSource().sendSuccess(() -> Component.literal("已清空职业标签"), true);
        return 1;
    }

    private static int profile(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ItemStack stack = player.getMainHandItem();
        PlayerClassData data = PlayerClasses.get(player);
        String classText = data.activeClass().map(ResourceLocation::toString).orElse("<未设置>");
        WeaponDefinition definition = WeaponPool.resolve(stack);
        WeaponProfile profile = WeaponPool.resolveProfile(stack);
        String poolText = definition != null ? definition.id().toString() : "<未入池>";
        HitEffect effect = definition != null ? definition.hitEffect() : null;
        if (effect == null && stack.getItem() instanceof WeaponHitProvider provider) {
            effect = provider.getWeaponHitEffect(stack);
        }
        String effectText = effect != null ? effect.getClass().getSimpleName() : "none";

        if (profile == null) {
            context.getSource().sendSuccess(() -> Component.literal(
                    "手持物品没有 weapon_profile；weapon=" + BuiltInRegistries.ITEM.getKey(stack.getItem())
                            + " pool=" + poolText + " playerClass=" + classText
                            + " tags=" + data.classTags()), false);
            return 1;
        }

        boolean usable = profile.isUsableBy(player);
        float panel = DamagePipeline.panelValue(player, stack);
        context.getSource().sendSuccess(() -> Component.literal(String.format(Locale.ROOT,
                "weapon=%s category=%s damage=%s rangedScale=%.2f requiredClasses=%s classTags=%s pool=%s effect=%s | "
                        + "playerClass=%s tags=%s usable=%s panel=%.2f",
                BuiltInRegistries.ITEM.getKey(stack.getItem()),
                profile.category(),
                profile.damage(),
                profile.rangedScale(),
                profile.requiredClasses(),
                profile.classTags(),
                poolText,
                effectText,
                classText,
                data.classTags(),
                usable,
                panel)), false);
        return 1;
    }

    private static int pool(CommandContext<CommandSourceStack> context) {
        List<WeaponDefinition> definitions = WeaponPool.all();
        String ids = definitions.stream()
                .limit(10)
                .map(definition -> definition.id().toString())
                .collect(Collectors.joining(", "));
        context.getSource().sendSuccess(() -> Component.literal(
                "武器池 " + definitions.size() + " 件：" + ids), false);
        return 1;
    }

    private static int stats(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        CombatStats stats = CombatStats.capture(player);
        context.getSource().sendSuccess(() -> Component.literal(String.format(Locale.ROOT,
                "HP %.1f/%.1f | Mana %.0f/%.0f | Food %d",
                stats.health(), stats.maxHealth(), stats.mana(), stats.maxMana(), stats.foodLevel())), false);
        context.getSource().sendSuccess(() -> Component.literal(String.format(Locale.ROOT,
                "AD %.1f | AP %.1f | Armor %.1f | MR %.1f",
                stats.attackDamage(), stats.abilityPower(), stats.armor(), stats.magicResist())), false);
        context.getSource().sendSuccess(() -> Component.literal(String.format(Locale.ROOT,
                "AS %.2f (LoL %.2f) | AH %.0f | Crit %.0f%% | CritDmg %.0f%%",
                stats.attackSpeed(), stats.attackSpeed() * 0.625F / 4.0F, stats.abilityHaste(),
                stats.critChance() * 100.0F, stats.critDamage() * 100.0F)), false);
        context.getSource().sendSuccess(() -> Component.literal(String.format(Locale.ROOT,
                "ArmorPen %.1f (%.0f%%) | MagicPen %.1f (%.0f%%)",
                stats.armorPenetrationFlat(), stats.armorPenetrationPercent() * 100.0F,
                stats.magicPenetrationFlat(), stats.magicPenetrationPercent() * 100.0F)), false);
        context.getSource().sendSuccess(() -> Component.literal(String.format(Locale.ROOT,
                "Regen HP %.1f/5s Mana %.1f/5s | Lifesteal %.0f%% | Omnivamp %.0f%%",
                stats.healthRegeneration(), stats.manaRegeneration(),
                stats.lifesteal() * 100.0F, stats.omnivamp() * 100.0F)), false);
        return 1;
    }
}

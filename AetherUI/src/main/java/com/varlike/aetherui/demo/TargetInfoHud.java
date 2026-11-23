package com.varlike.aetherui.demo;

import com.varlike.aetherui.Config;
import com.varlike.aetherui.api.AetherUI;
import com.varlike.aetherui.api.UIBuilder;
import com.varlike.aetherui.api.UIComponent;
import com.varlike.aetherui.api.UITree;
import com.varlike.aetherui.api.hud.BlockInfoProvider;
import com.varlike.aetherui.api.hud.RecipeInfo;
import com.varlike.aetherui.api.hud.RecipeInfoProvider;
import com.varlike.aetherui.api.lang.AetherUILang;
import com.varlike.aetherui.api.layout.Alignment;
import com.varlike.aetherui.api.layout.Size;
import com.varlike.aetherui.api.paint.CanvasContext;
import com.varlike.aetherui.api.paint.CanvasStyles;
import com.varlike.aetherui.api.target.ScreenAnchor;
import com.varlike.aetherui.api.target.ScreenRenderTarget;
import com.varlike.aetherui.core.registry.BlockInfoRegistry;
import com.varlike.aetherui.core.registry.RecipeInfoRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 右下角瞄准信息：实体显示护甲值；方块显示挖掘工具、所需等级、手中工具是否可挖。
 * 挖掘信息下方追加其它 mod 注册的方块信息（多行、支持颜色）。
 * 所有游戏模式均可显示，由 config 开关控制。
 */
public final class TargetInfoHud {

    private static final float W = 200f;
    private static final float H = 140f;
    private static final float LINE_H = 14f;
    private static final float RECIPE_ROW_H = 22f;
    private static final float ICON = 16f;
    private static final float GAP = 6f;
    private static final int PAD = 5;

    private static final ResourceLocation CONTAINER_SLOTS_ID =
            ResourceLocation.fromNamespaceAndPath("aetherui", "demo_container_slots");

    private static UITree tree;

    private TargetInfoHud() {}

    /** Demo 内置的容器格子 provider：瞄准任意 Container 显示已用格子数。 */
    private static final BlockInfoProvider CONTAINER_SLOTS = new BlockInfoProvider() {
        @Override public ResourceLocation id() { return CONTAINER_SLOTS_ID; }
        @Override public List<Component> getLines(Level level, BlockPos pos, BlockState state,
                                                  BlockEntity be, BlockHitResult hit, LocalPlayer player) {
            if (!(be instanceof Container container)) return List.of();
            int total = container.getContainerSize();
            int used = 0;
            for (int i = 0; i < total; i++) {
                if (!container.getItem(i).isEmpty()) used++;
            }
            return List.of(
                    Component.literal("格子: " + used + "/" + total + " 已用")
                            .withStyle(used == total ? net.minecraft.ChatFormatting.RED
                                    : net.minecraft.ChatFormatting.GREEN));
        }
    };

    public static void init() {
        if (tree != null) return;

        AetherUI.registerBlockInfoProvider(CONTAINER_SLOTS);
        DemoFurnaceRecipe.register();

        UIComponent root = UIBuilder.canvas(Size.fixed(W), Size.fixed(H))
                .canvasAnchor(Alignment.TOP_LEFT)
                .painter(ctx -> paint(ctx, Minecraft.getInstance().player))
                .build();

        tree = AetherUI.build(root,
                new ScreenRenderTarget(ScreenAnchor.bottomRight(10f, -12f))
                        .alignment(Alignment.BOTTOM_RIGHT)
                        .guiScale(1f),
                Config::targetInfoEnabled);
        AetherUI.register(tree);
    }

    public static void destroy() {
        if (tree == null) return;
        AetherUI.destroy(tree);
        AetherUI.unregisterBlockInfoProvider(CONTAINER_SLOTS_ID);
        DemoFurnaceRecipe.unregister();
        tree = null;
    }

    private sealed interface Row permits TextRow, RecipeRow {}
    private record TextRow(Component text, int color) implements Row {}
    private record RecipeRow(RecipeInfo info) implements Row {}

    private static void paint(CanvasContext ctx, LocalPlayer player) {
        Minecraft mc = Minecraft.getInstance();
        HitResult hit = mc.hitResult;
        if (hit == null || hit.getType() == HitResult.Type.MISS || mc.level == null) return;

        List<Row> rows = new ArrayList<>();

        if (hit.getType() == HitResult.Type.ENTITY) {
            Entity entity = ((EntityHitResult) hit).getEntity();
            rows.add(new TextRow(entity.getName(), 0xFFFFFFFF));
            if (entity instanceof LivingEntity living) {
                rows.add(new TextRow(Component.literal(tr(AetherUILang.HP) + " " + Math.round(living.getHealth())
                        + "/" + Math.round(living.getMaxHealth()) + "   " + tr(AetherUILang.ARMOR)
                        + " " + living.getArmorValue()), 0xFFC0C0C0));
            } else {
                rows.add(new TextRow(Component.translatable(AetherUILang.NON_LIVING), 0xFFC0C0C0));
            }
            rows.add(new TextRow(Component.literal(String.format("%.1f", entity.distanceTo(player)) + "m"), 0xFF888888));
        } else if (hit.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hit;
            BlockPos pos = blockHit.getBlockPos();
            BlockState state = mc.level.getBlockState(pos);
            rows.add(new TextRow(state.getBlock().getName(), 0xFFFFFFFF));

            String tool = toolName(state);
            String level = harvestLevel(state);
            rows.add(new TextRow(Component.literal(tr(AetherUILang.TOOL) + ": " + tool
                    + "   " + tr(AetherUILang.LEVEL_REQ) + ": " + level), 0xFFC0C0C0));

            ItemStack held = player.getMainHandItem();
            boolean canMine = held.isEmpty() ? !state.requiresCorrectToolForDrops()
                    : held.isCorrectToolForDrops(state);
            rows.add(new TextRow(Component.literal((canMine ? "✔ " + tr(AetherUILang.CAN_MINE) : "✘ " + tr(AetherUILang.CANNOT_MINE))
                    + "   " + pos.getX() + "," + pos.getY() + "," + pos.getZ()),
                    canMine ? 0xFF55FF55 : 0xFFFF5555));

            if (Config.showTargetBlockInfo()) {
                BlockEntity be = mc.level.getBlockEntity(pos);

                // 其它 mod 的方块信息（多行、带颜色）
                for (BlockInfoProvider provider : BlockInfoRegistry.all()) {
                    List<Component> extra = provider.getLines(mc.level, pos, state, be, blockHit, player);
                    if (extra == null || extra.isEmpty()) continue;
                    for (Component c : extra) {
                        if (c != null) rows.add(new TextRow(c, 0xFFFFFFFF));
                    }
                }

                // 配方信息：[材料] → [时间] → [产物]
                for (RecipeInfoProvider provider : RecipeInfoRegistry.all()) {
                    List<RecipeInfo> recipes = provider.getRecipes(mc.level, pos, state, be, blockHit, player);
                    if (recipes == null || recipes.isEmpty()) continue;
                    if (provider.title() != null) rows.add(new TextRow(provider.title(), 0xFFFFD700));
                    for (RecipeInfo recipe : recipes) {
                        if (recipe != null) rows.add(new RecipeRow(recipe));
                    }
                }
            }
        }

        if (rows.isEmpty()) return;

        Font font = Minecraft.getInstance().font;
        // 动态面板尺寸：宽度取最长行，高度按行类型累加
        float panelW = W;
        float panelH = PAD * 2f;
        for (Row row : rows) {
            if (row instanceof TextRow t) {
                panelW = Math.max(panelW, font.width(t.text()) + PAD * 2f);
                panelH += LINE_H;
            } else {
                RecipeInfo info = ((RecipeRow) row).info();
                panelW = Math.max(panelW, recipeRowWidth(info, font) + PAD * 2f);
                panelH += RECIPE_ROW_H;
            }
        }
        // 内容贴右下角：Canvas 锚点是 BOTTOM_RIGHT，面板从右上角往下排布
        float startY = Math.max(0f, ctx.height() - panelH);
        float startX = Math.max(0f, ctx.width() - panelW);

        frame(ctx, startX, startY, panelW, panelH);
        float y = startY + PAD;
        for (Row row : rows) {
            if (row instanceof TextRow t) {
                ctx.drawString(t.text(), startX + PAD, y, t.color(), false);
                y += LINE_H;
            } else {
                drawRecipeRow(ctx, ((RecipeRow) row).info(), startX + PAD, y, font);
                y += RECIPE_ROW_H;
            }
        }
    }

    /** 计算一行配方的逻辑宽度：材料图标 + 箭头 + 时间 + 箭头 + 产物图标。 */
    private static float recipeRowWidth(RecipeInfo info, Font font) {
        float w = 0f;
        int n = info.ingredients().size();
        if (n > 0) w += n * ICON + (n - 1) * 2f;
        w += GAP + font.width(">") + GAP;
        if (info.cookingSeconds() > 0) w += font.width(info.cookingSeconds() + "s") + GAP;
        w += font.width(">") + GAP;
        if (!info.result().isEmpty()) w += ICON;
        return w;
    }

    /** 绘制一行配方：[材料...] → [时间] → [产物]，箭头与时间用文字，物品用 renderItem。 */
    private static void drawRecipeRow(CanvasContext ctx, RecipeInfo info, float x, float y, Font font) {
        float cx = x;
        float iconY = y + (RECIPE_ROW_H - ICON) / 2f;
        for (ItemStack stack : info.ingredients()) {
            ctx.renderItem(stack, cx, iconY);
            cx += ICON + 2f;
        }
        if (info.ingredients().isEmpty()) cx = x;

        cx += GAP;
        ctx.drawString(Component.literal(">"), cx, y + 3f, 0xFF888888, false);
        cx += font.width(">") + GAP;

        if (info.cookingSeconds() > 0) {
            String time = info.cookingSeconds() + "s";
            ctx.drawString(Component.literal(time), cx, y + 3f, 0xFFFFD700, false);
            cx += font.width(time) + GAP;
        }

        ctx.drawString(Component.literal(">"), cx, y + 3f, 0xFF888888, false);
        cx += font.width(">") + GAP;

        if (!info.result().isEmpty()) ctx.renderItem(info.result(), cx, iconY);
    }

    private static String toolName(BlockState state) {
        if (state.is(BlockTags.MINEABLE_WITH_PICKAXE)) return tr(AetherUILang.TOOL_PICKAXE);
        if (state.is(BlockTags.MINEABLE_WITH_SHOVEL))  return tr(AetherUILang.TOOL_SHOVEL);
        if (state.is(BlockTags.MINEABLE_WITH_AXE))     return tr(AetherUILang.TOOL_AXE);
        if (state.is(BlockTags.MINEABLE_WITH_HOE))     return tr(AetherUILang.TOOL_HOE);
        return tr(AetherUILang.TOOL_HAND);
    }

    private static String harvestLevel(BlockState state) {
        if (state.is(BlockTags.NEEDS_DIAMOND_TOOL)) return tr(AetherUILang.TIER_DIAMOND);
        if (state.is(BlockTags.NEEDS_IRON_TOOL))    return tr(AetherUILang.TIER_IRON);
        if (state.is(BlockTags.NEEDS_STONE_TOOL))   return tr(AetherUILang.TIER_STONE);
        return tr(AetherUILang.TIER_NONE);
    }

    private static String tr(String key) {
        return Component.translatable(key).getString();
    }

    private static void frame(CanvasContext ctx, float x, float y, float w, float h) {
        CanvasStyles.panel(ctx, x, y, w, h);
    }
}

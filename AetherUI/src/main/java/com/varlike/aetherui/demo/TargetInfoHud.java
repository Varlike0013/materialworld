package com.varlike.aetherui.demo;

import com.varlike.aetherui.Config;
import com.varlike.aetherui.api.AetherUI;
import com.varlike.aetherui.api.UIBuilder;
import com.varlike.aetherui.api.UIComponent;
import com.varlike.aetherui.api.UITree;
import com.varlike.aetherui.api.hud.BlockInfoProvider;
import com.varlike.aetherui.api.lang.AetherUILang;
import com.varlike.aetherui.api.layout.Alignment;
import com.varlike.aetherui.api.layout.Size;
import com.varlike.aetherui.api.paint.CanvasContext;
import com.varlike.aetherui.api.target.ScreenAnchor;
import com.varlike.aetherui.api.target.ScreenRenderTarget;
import com.varlike.aetherui.core.registry.BlockInfoRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
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
    private static final int PAD = 5;

    private static UITree tree;

    private TargetInfoHud() {}

    public static void init() {
        if (tree != null) return;

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
        tree = null;
    }

    private record Line(Component text, int color) {}

    private static void paint(CanvasContext ctx, LocalPlayer player) {
        Minecraft mc = Minecraft.getInstance();
        HitResult hit = mc.hitResult;
        if (hit == null || hit.getType() == HitResult.Type.MISS || mc.level == null) return;

        List<Line> lines = new ArrayList<>();

        if (hit.getType() == HitResult.Type.ENTITY) {
            Entity entity = ((EntityHitResult) hit).getEntity();
            lines.add(new Line(entity.getName(), 0xFFFFFFFF));
            if (entity instanceof LivingEntity living) {
                lines.add(new Line(Component.literal(tr(AetherUILang.HP) + " " + Math.round(living.getHealth())
                        + "/" + Math.round(living.getMaxHealth()) + "   " + tr(AetherUILang.ARMOR)
                        + " " + living.getArmorValue()), 0xFFC0C0C0));
            } else {
                lines.add(new Line(Component.translatable(AetherUILang.NON_LIVING), 0xFFC0C0C0));
            }
            lines.add(new Line(Component.literal(String.format("%.1f", entity.distanceTo(player)) + "m"), 0xFF888888));
        } else if (hit.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHit = (BlockHitResult) hit;
            BlockPos pos = blockHit.getBlockPos();
            BlockState state = mc.level.getBlockState(pos);
            lines.add(new Line(state.getBlock().getName(), 0xFFFFFFFF));

            String tool = toolName(state);
            String level = harvestLevel(state);
            lines.add(new Line(Component.literal(tr(AetherUILang.TOOL) + ": " + tool
                    + "   " + tr(AetherUILang.LEVEL_REQ) + ": " + level), 0xFFC0C0C0));

            ItemStack held = player.getMainHandItem();
            boolean canMine = held.isEmpty() ? !state.requiresCorrectToolForDrops()
                    : held.isCorrectToolForDrops(state);
            lines.add(new Line(Component.literal((canMine ? "✔ " + tr(AetherUILang.CAN_MINE) : "✘ " + tr(AetherUILang.CANNOT_MINE))
                    + "   " + pos.getX() + "," + pos.getY() + "," + pos.getZ()),
                    canMine ? 0xFF55FF55 : 0xFFFF5555));

            // 其它 mod 的方块信息（多行、带颜色）
            BlockEntity be = mc.level.getBlockEntity(pos);
            for (BlockInfoProvider provider : BlockInfoRegistry.all()) {
                List<Component> extra = provider.getLines(mc.level, pos, state, be, blockHit, player);
                if (extra == null || extra.isEmpty()) continue;
                for (Component c : extra) {
                    if (c != null) lines.add(new Line(c, 0xFFFFFFFF));
                }
            }
        }

        if (lines.isEmpty()) return;

        // 动态面板尺寸：宽度取最长行，高度取行数
        var font = Minecraft.getInstance().font;
        float panelW = W;
        for (Line line : lines) {
            panelW = Math.max(panelW, font.width(line.text()) + PAD * 2f);
        }
        float panelH = lines.size() * LINE_H + PAD * 2f;
        // 内容贴右下角：Canvas 锚点是 BOTTOM_RIGHT，面板从右上角往下排布
        float startY = Math.max(0f, ctx.height() - panelH);
        float startX = Math.max(0f, ctx.width() - panelW);

        frame(ctx, startX, startY, panelW, panelH);
        float y = startY + PAD;
        for (Line line : lines) {
            ctx.drawString(line.text(), startX + PAD, y, line.color(), false);
            y += LINE_H;
        }
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
        // fill 的第 3/4 参数是宽和高（不是右下角坐标）
        // 半透明黑色背景（约 44% 不透明），够黑又能透出下方世界
        ctx.fill(x, y, w, h, 0x70000000);
        // 半透明灰白色边框，四边等宽 1px（参考 CustomHud.frame）
        int border = 0x99D0D0D0;
        ctx.fill(x, y, w, 1f, border);
        ctx.fill(x, y + h - 1f, w, 1f, border);
        ctx.fill(x, y, 1f, h, border);
        ctx.fill(x + w - 1f, y, 1f, h, border);
    }
}

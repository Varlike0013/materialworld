package com.varlike.aetherui.demo;

import com.varlike.aetherui.Config;
import com.varlike.aetherui.api.AetherUI;
import com.varlike.aetherui.api.UIBuilder;
import com.varlike.aetherui.api.UIComponent;
import com.varlike.aetherui.api.UITree;
import com.varlike.aetherui.api.hud.HudBar;
import com.varlike.aetherui.api.lang.AetherUILang;
import com.varlike.aetherui.api.layout.Alignment;
import com.varlike.aetherui.api.layout.Size;
import com.varlike.aetherui.api.paint.CanvasContext;
import com.varlike.aetherui.api.target.ScreenAnchor;
import com.varlike.aetherui.api.target.ScreenRenderTarget;
import com.varlike.aetherui.core.registry.HudBarRegistry;
import com.varlike.aetherui.core.render.OffsetCanvasContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 左下角 HUD，从上到下：装备/手持耐久行、经验条、饥饿条、血条（右侧护甲数值方块）、
 * 以及其它 mod 注册的自定义条（在饥饿条下方区域）。
 *
 * <p>空槽位与无耐久物品不显示；创造和旁观者模式不渲染。内容整体贴底对齐。
 */
public final class CustomHud {

    private static final float W = 142f;
    private static final float H = 220f;
    private static final float GAP = 2f;
    private static final float ROW_H = 20f;
    private static final float BAR_H = 12f;
    private static final float ARMOR_W = 24f;

    private static UITree tree;

    private CustomHud() {}

    public static void init() {
        if (tree != null) return;

        UIComponent root = UIBuilder.canvas(Size.fixed(W), Size.fixed(H))
                .canvasAnchor(Alignment.TOP_LEFT)
                .painter(CustomHud::paint)
                .build();

        tree = AetherUI.build(root,
                new ScreenRenderTarget(ScreenAnchor.bottomLeft(10f, -12f))
                        .alignment(Alignment.BOTTOM_LEFT)
                        .guiScale(1f),
                CustomHud::shouldRender);
        AetherUI.register(tree);
    }

    public static void destroy() {
        if (tree == null) return;
        AetherUI.destroy(tree);
        tree = null;
    }

    private static boolean shouldRender() {
        if (!Config.customHudEnabled()) return false;
        LocalPlayer p = player();
        return p != null && !p.isCreative() && !p.isSpectator();
    }

    private static LocalPlayer player() {
        return Minecraft.getInstance().player;
    }

    private static void paint(CanvasContext ctx) {
        LocalPlayer p = player();
        if (p == null) return;

        // 装备/手持耐久行
        List<ItemStack> items = new ArrayList<>();
        if (Config.showArmorDurability()) {
            addIfDamageable(items, armor(p, 3));
            addIfDamageable(items, armor(p, 2));
            addIfDamageable(items, armor(p, 1));
            addIfDamageable(items, armor(p, 0));
        }
        if (Config.showHeldDurability()) {
            addIfDamageable(items, p.getMainHandItem());
            addIfDamageable(items, p.getOffhandItem());
        }

        // 自定义条（仅可见的）
        List<HudBar> bars = new ArrayList<>();
        for (HudBar bar : HudBarRegistry.all()) {
            if (bar.visible(p)) bars.add(bar);
        }

        // 总高度：耐久行 + 经验/饥饿/血条 + 自定义条
        float customH = 0f;
        for (HudBar bar : bars) customH += bar.height();
        int rows = items.size() + 3 + bars.size();
        float totalH = items.size() * ROW_H + 3 * BAR_H + customH + GAP * Math.max(0, rows - 1);
        float y = Math.max(0f, ctx.height() - totalH);

        for (ItemStack stack : items) {
            drawSlot(ctx, y, stack);
            y += ROW_H + GAP;
        }

        // 经验条
        float exp = clamp(p.experienceProgress);
        drawBar(ctx, y, W, exp,
                Component.translatable(AetherUILang.LEVEL).getString() + " " + p.experienceLevel + "  " + Math.round(exp * 100) + "%", 0xFF00DD00);
        y += BAR_H + GAP;

        // 饥饿条
        float food = p.getFoodData().getFoodLevel() / 20f;
        drawBar(ctx, y, W, clamp(food),
                Component.translatable(AetherUILang.FOOD).getString() + " " + p.getFoodData().getFoodLevel() + "/20", 0xFFD49A3C);
        y += BAR_H + GAP;

        // 血条 + 右侧护甲数值方块
        float hp = p.getMaxHealth() > 0 ? p.getHealth() / p.getMaxHealth() : 0f;
        drawBar(ctx, y, W - ARMOR_W - GAP, clamp(hp),
                Component.translatable(AetherUILang.HP).getString() + " " + Math.round(p.getHealth()) + "/" + Math.round(p.getMaxHealth()), 0xFFE03333);
        drawArmorBox(ctx, y, p.getArmorValue());
        y += BAR_H + GAP;

        // 其它 mod 的自定义条（饥饿条下方区域）
        for (HudBar bar : bars) {
            float bw = bar.width();
            float bh = bar.height();
            CanvasContext local = new OffsetCanvasContext(ctx, 0f, y, bw, bh);
            bar.paint(local, p);
            y += bh + GAP;
        }
    }

    private static void addIfDamageable(List<ItemStack> out, ItemStack stack) {
        if (stack != null && !stack.isEmpty() && stack.isDamageableItem()) out.add(stack);
    }

    private static ItemStack armor(LocalPlayer p, int slot) {
        return p.getInventory().getArmor(slot);
    }

    private static void drawSlot(CanvasContext ctx, float y, ItemStack stack) {
        frame(ctx, 0f, y, W, ROW_H);
        ctx.renderItem(stack, 2f, y + 2f);
        ctx.drawString(stack.getHoverName(), 22f, y + 1f, 0xFFFFFFFF, false);

        int max = stack.getMaxDamage();
        int left = Math.max(0, max - stack.getDamageValue());
        float ratio = max > 0 ? (float) left / max : 0f;
        ctx.drawString(Component.literal(left + " / " + max), 22f, y + 11f, durabilityColor(ratio), false);
    }

    private static void drawBar(CanvasContext ctx, float y, float width, float ratio, String label, int color) {
        frame(ctx, 0f, y, width, BAR_H);
        if (ratio > 0f) ctx.fill(2f, y + 2f, Math.max(0f, (width - 4f) * ratio), BAR_H - 4f, color);
        ctx.drawString(Component.literal(label), 4f, y + 1f, 0xFFFFFFFF, true);
    }

    private static void drawArmorBox(CanvasContext ctx, float y, int armorValue) {
        float x = W - ARMOR_W;
        frame(ctx, x, y, ARMOR_W, BAR_H);
        ctx.drawString(Component.literal(String.valueOf(armorValue)),
                x + (ARMOR_W - 6f) / 2f, y + 2f, 0xFFC0C0C0, false);
    }

    private static void frame(CanvasContext ctx, float x, float y, float w, float h) {
        ctx.fill(x, y, w, h, 0xB0080808);
        ctx.fill(x, y, w, 1f, 0xFF5A5A5A);
        ctx.fill(x, y + h - 1f, w, 1f, 0xFF5A5A5A);
        ctx.fill(x, y, 1f, h, 0xFF5A5A5A);
        ctx.fill(x + w - 1f, y, 1f, h, 0xFF5A5A5A);
    }

    private static float clamp(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    private static int durabilityColor(float ratio) {
        return ratio > 0.6f ? 0xFF55FF55 : (ratio > 0.3f ? 0xFFFFFF55 : 0xFFFF5555);
    }
}

package com.varlike.aetherui.demo;

import com.varlike.aetherui.Config;
import com.varlike.aetherui.api.AetherUI;
import com.varlike.aetherui.api.UIBuilder;
import com.varlike.aetherui.api.UIComponent;
import com.varlike.aetherui.api.UITree;
import com.varlike.aetherui.api.hud.BuffIcon;
import com.varlike.aetherui.api.layout.Alignment;
import com.varlike.aetherui.api.layout.Size;
import com.varlike.aetherui.api.paint.CanvasContext;
import com.varlike.aetherui.api.target.ScreenAnchor;
import com.varlike.aetherui.api.target.ScreenRenderTarget;
import com.varlike.aetherui.core.registry.BuffIconRegistry;
import com.varlike.aetherui.core.render.OffsetCanvasContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.core.Holder;

import java.util.ArrayList;
import java.util.List;

/**
 * 物品栏上方的 buff 图标 HUD：图标从左到右排列，超出后换行向上。
 * 同时显示原版状态效果和其它 mod 注册的自定义 BuffIcon。
 */
public final class BuffHud {

    private static final float W = 216f;
    private static final float H = 96f;
    private static final float ICON = 24f;
    private static final float GAP = 2f;
    private static final int PER_ROW = 8;

    private static UITree tree;

    private BuffHud() {}

    public static void init() {
        if (tree != null) return;

        UIComponent root = UIBuilder.canvas(Size.fixed(W), Size.fixed(H))
                .canvasAnchor(Alignment.TOP_LEFT)
                .painter(ctx -> paint(ctx, Minecraft.getInstance().player))
                .build();

        // 物品栏约 22px 高，放在其上方
        tree = AetherUI.build(root,
                new ScreenRenderTarget(ScreenAnchor.bottomCenter(-32f))
                        .alignment(Alignment.BOTTOM_CENTER)
                        .guiScale(1f),
                BuffHud::shouldRender);
        AetherUI.register(tree);
    }

    public static void destroy() {
        if (tree == null) return;
        AetherUI.destroy(tree);
        tree = null;
    }

    private static boolean shouldRender() {
        if (!Config.buffHudEnabled()) return false;
        LocalPlayer p = Minecraft.getInstance().player;
        return p != null && !p.isSpectator();
    }

    private record BuffEntry(BuffIcon icon, Integer level) {}

    private static void paint(CanvasContext ctx, LocalPlayer player) {
        List<BuffEntry> icons = new ArrayList<>();

        // 原版状态效果：getAmplifier 从 0 开始，显示为 I / II / III ...
        for (MobEffectInstance inst : player.getActiveEffects()) {
            if (!inst.showIcon()) continue;
            Holder<MobEffect> holder = inst.getEffect();
            icons.add(new BuffEntry(
                    new VanillaEffectIcon(holder.value().getColor(), holder, inst.getAmplifier() + 1),
                    inst.getAmplifier() + 1));
        }

        // 其它 mod 注册的图标：未提供 level 时不绘制层数
        for (BuffIcon icon : BuffIconRegistry.all()) {
            if (icon.visible(player)) icons.add(new BuffEntry(icon, icon.level(player)));
        }

        int n = 0;
        for (BuffEntry entry : icons) {
            BuffIcon icon = entry.icon();
            int col = n % PER_ROW;
            int row = n / PER_ROW;
            float x = col * (ICON + GAP);
            float y = H - (row + 1) * (ICON + GAP); // 从下到上换行
            if (y < 0f) break;

            CanvasContext local = new OffsetCanvasContext(ctx, x, y, icon.width(), icon.height());
            icon.paint(local, player);
            drawLevel(local, entry.level(), icon.width(), icon.height());
            n++;
        }
    }

    /** 在图标右下角绘制罗马数字层数；没有层数或层数小于 1 时不绘制。 */
    private static void drawLevel(CanvasContext ctx, Integer level, float width, float height) {
        if (level == null || level < 1) return;

        Component text = Component.literal(toRoman(level));
        Font font = Minecraft.getInstance().font;
        float x = Math.max(0f, width - font.width(text) - 1f);
        float y = Math.max(0f, height - font.lineHeight - 1f);

        ctx.drawString(text, x + 1f, y + 1f, 0xFF000000, false);
        ctx.drawString(text, x, y, 0xFFFFFFFF, false);
    }

    /** 把正整数转成罗马数字；超过 3999 时用连续 M 表示。 */
    private static String toRoman(int value) {
        int[] values = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] symbols = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        StringBuilder sb = new StringBuilder();
        int remaining = Math.max(1, value);
        for (int i = 0; i < values.length; i++) {
            while (remaining >= values[i]) {
                sb.append(symbols[i]);
                remaining -= values[i];
            }
        }
        return sb.toString();
    }

    /** 把原版状态效果包装成 BuffIcon，使用 MobEffectTextures 的 atlas sprite。 */
    private static final class VanillaEffectIcon implements BuffIcon {
        private final int color;
        private final Holder<MobEffect> holder;
        private final int level;

        VanillaEffectIcon(int color, Holder<MobEffect> holder, int level) {
            this.color = color;
            this.holder = holder;
            this.level = level;
        }

        @Override public ResourceLocation id() {
            return holder.getKey().location();
        }
        @Override public String label() { return holder.value().getDisplayName().getString(); }
        @Override public float width()  { return ICON; }
        @Override public float height() { return ICON; }
        @Override public Integer level(LocalPlayer player) { return level; }

        @Override
        public void paint(CanvasContext ctx, LocalPlayer player) {
            TextureAtlasSprite sprite = Minecraft.getInstance().getMobEffectTextures().get(holder);
            if (sprite == null) return;
            ctx.blitAtlasSprite(sprite, 0f, 0f, ICON, ICON);
        }
    }
}

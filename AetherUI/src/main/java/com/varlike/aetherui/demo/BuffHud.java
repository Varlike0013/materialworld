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
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
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

    private static void paint(CanvasContext ctx, LocalPlayer player) {
        List<BuffIcon> icons = new ArrayList<>();

        // 原版状态效果
        for (MobEffectInstance inst : player.getActiveEffects()) {
            if (!inst.showIcon()) continue;
            Holder<MobEffect> holder = inst.getEffect();
            icons.add(new VanillaEffectIcon(holder.value().getColor(), holder));
        }

        // 其它 mod 注册的图标
        for (BuffIcon icon : BuffIconRegistry.all()) {
            if (icon.visible(player)) icons.add(icon);
        }

        int n = 0;
        for (BuffIcon icon : icons) {
            int col = n % PER_ROW;
            int row = n / PER_ROW;
            float x = col * (ICON + GAP);
            float y = H - (row + 1) * (ICON + GAP); // 从下到上换行
            if (y < 0f) break;
            CanvasContext local = new OffsetCanvasContext(ctx, x, y, icon.width(), icon.height());
            icon.paint(local, player);
            n++;
        }
    }

    /** 把原版状态效果包装成 BuffIcon，使用 MobEffectTextures 的 atlas sprite。 */
    private static final class VanillaEffectIcon implements BuffIcon {
        private final int color;
        private final Holder<MobEffect> holder;

        VanillaEffectIcon(int color, Holder<MobEffect> holder) {
            this.color = color;
            this.holder = holder;
        }

        @Override public ResourceLocation id() {
            return holder.getKey().location();
        }
        @Override public String label() { return holder.value().getDisplayName().getString(); }
        @Override public float width()  { return ICON; }
        @Override public float height() { return ICON; }

        @Override
        public void paint(CanvasContext ctx, LocalPlayer player) {
            TextureAtlasSprite sprite = Minecraft.getInstance().getMobEffectTextures().get(holder);
            if (sprite == null) return;
            ctx.blitAtlasSprite(sprite, 0f, 0f, ICON, ICON);
        }
    }
}

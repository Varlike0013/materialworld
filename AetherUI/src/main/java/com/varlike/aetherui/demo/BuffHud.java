package com.varlike.aetherui.demo;

import com.varlike.aetherui.Config;
import com.varlike.aetherui.api.AetherUI;
import com.varlike.aetherui.api.UIBuilder;
import com.varlike.aetherui.api.UIComponent;
import com.varlike.aetherui.api.UITree;
import com.varlike.aetherui.api.hud.BuffIcon;
import com.varlike.aetherui.api.layout.Alignment;
import com.varlike.aetherui.api.layout.MainAxisAlignment;
import com.varlike.aetherui.api.layout.Size;
import com.varlike.aetherui.api.paint.CanvasContext;
import com.varlike.aetherui.api.target.ScreenAnchor;
import com.varlike.aetherui.api.target.ScreenRenderTarget;
import com.varlike.aetherui.core.registry.BuffIconRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.ArrayList;
import java.util.List;

/**
 * 物品栏上方的 buff 图标 HUD。
 *
 * <p>图标由 {@code UIBuilder.table(PER_ROW)} 自动按“添加顺序 → 从左到右 → 超出换行”排列；
 * 每个子节点声明自己的真实宽高，根节点高度 WrapContent 并贴底对齐，因此无需手动计算 Y 偏移。
 */
public final class BuffHud {

    private static final float W = 216f;
    private static final float ICON = 24f;
    private static final float GAP = 2f;
    private static final int PER_ROW = 8;

    private static UITree tree;
    private static String signature = "";

    private BuffHud() {}

    public static void init() {
        update();
    }

    /** 每帧检查图标集合是否变化；变化时才重建组件树。 */
    public static void update() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (!shouldRender() || player == null) return;

        List<BuffEntry> icons = collectIcons(player);
        String nextSignature = signatureOf(icons);
        if (nextSignature.equals(signature)) return;

        if (tree != null) AetherUI.destroy(tree);
        signature = nextSignature;
        tree = AetherUI.build(buildRoot(icons), target(), BuffHud::shouldRender);
        AetherUI.register(tree);
    }

    public static void destroy() {
        if (tree != null) AetherUI.destroy(tree);
        tree = null;
        signature = "";
    }

    private static boolean shouldRender() {
        if (!Config.buffHudEnabled()) return false;
        LocalPlayer p = Minecraft.getInstance().player;
        return p != null && !p.isSpectator();
    }

    private static ScreenRenderTarget target() {
        // 物品栏约 22px 高，放在其上方；根节点高度随时间增加自动向上生长
        return new ScreenRenderTarget(ScreenAnchor.bottomCenter(-32f))
                .alignment(Alignment.BOTTOM_CENTER)
                .guiScale(1f);
    }

    private record BuffEntry(BuffIcon icon, Integer level) {}

    private static List<BuffEntry> collectIcons(LocalPlayer player) {
        List<BuffEntry> icons = new ArrayList<>();

        // 原版状态效果：getAmplifier 从 0 开始，显示为 I / II / III ...
        for (MobEffectInstance inst : player.getActiveEffects()) {
            if (!inst.showIcon()) continue;
            Holder<MobEffect> holder = inst.getEffect();
            int level = inst.getAmplifier() + 1;
            icons.add(new BuffEntry(
                    new VanillaEffectIcon(holder.value().getColor(), holder, level), level));
        }

        // 其它 mod 注册的图标：未提供 level 时不绘制层数
        for (BuffIcon icon : BuffIconRegistry.all()) {
            if (icon.visible(player)) icons.add(new BuffEntry(icon, icon.level(player)));
        }
        return icons;
    }

    private static String signatureOf(List<BuffEntry> icons) {
        StringBuilder sb = new StringBuilder();
        for (BuffEntry entry : icons) {
            BuffIcon icon = entry.icon();
            sb.append(icon.id()).append(':')
                    .append(entry.level()).append(':')
                    .append(icon.width()).append('x').append(icon.height())
                    .append('|');
        }
        return sb.toString();
    }

    private static UIComponent buildRoot(List<BuffEntry> icons) {
        UIBuilder table = UIBuilder.table(PER_ROW)
                .size(Size.fixed(W), Size.WRAP)
                .spacing(GAP)
                .mainAxisAlignment(MainAxisAlignment.CENTER);

        for (BuffEntry entry : icons) {
            BuffIcon icon = entry.icon();
            table.add(UIBuilder.canvas(Size.fixed(icon.width()), Size.fixed(icon.height()))
                    .canvasAnchor(Alignment.TOP_LEFT)
                    .painter(ctx -> {
                        LocalPlayer player = Minecraft.getInstance().player;
                        if (player != null) icon.paint(ctx, player);
                        drawLevel(ctx, entry.level(), icon.width(), icon.height());
                    }));
        }
        return table.build();
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

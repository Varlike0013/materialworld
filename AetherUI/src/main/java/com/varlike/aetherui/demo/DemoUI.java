package com.varlike.aetherui.demo;

import com.varlike.aetherui.api.AetherUI;
import com.varlike.aetherui.api.UIBuilder;
import com.varlike.aetherui.api.UITree;
import com.varlike.aetherui.api.UIComponent;
import com.varlike.aetherui.api.layout.Size;
import com.varlike.aetherui.api.target.ScreenAnchor;
import com.varlike.aetherui.api.target.ScreenRenderTarget;
import net.minecraft.network.chat.Component;

/** 最小的端到端演示：进入世界后在屏幕左上角显示一块面板。 */
public final class DemoUI {

    private static UITree tree;

    private DemoUI() {}

    public static void show() {
        if (tree != null) return;

        UIComponent root = UIBuilder.panel(Size.fixed(260f), Size.fixed(120f))
                .background(0xC0202020)
                .clipChildren(true)
                .add(UIBuilder.label(Component.literal("AetherUI Demo"))
                        .textColor(0xFFFF55FF)
                        .textScale(1.5f)
                        .x(12f).y(10f))
                .add(UIBuilder.row()
                        .spacing(8f)
                        .x(12f).y(42f)
                        .add(UIBuilder.label(Component.literal("Auto layout:")))
                        .add(UIBuilder.label(Component.literal("row")).textColor(0xFF55FF55)))
                .add(UIBuilder.label(Component.literal("Bindable + layout engine OK"))
                        .textColor(0xFFAAAAAA)
                        .x(12f).y(70f))
                .build();

        tree = AetherUI.build(root, new ScreenRenderTarget(ScreenAnchor.topLeft(20f, 20f)));
        AetherUI.register(tree);
    }

    public static void hide() {
        if (tree == null) return;
        AetherUI.destroy(tree);
        tree = null;
    }

    public static void toggle() {
        if (tree == null) show();
        else hide();
    }
}

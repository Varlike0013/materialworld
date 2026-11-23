package com.varlike.aetherui.api;

/**
 * 一棵组件树在两种渲染目标下的组合：屏幕树 + 世界树。
 * 两者共享同一个 {@link UIComponent} 实例，因此样式只声明一次，
 * 即可同时出现在屏幕 HUD 与世界空间（如 Boss 头顶）。
 */
public record DualTree(UITree screen, UITree world) {

    /** 注销这两个目标下的树（等价于分别对 screen / world 调用 destroy）。 */
    public void destroy() {
        AetherUI.destroy(screen);
        AetherUI.destroy(world);
    }
}

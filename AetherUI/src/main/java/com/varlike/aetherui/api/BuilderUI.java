package com.varlike.aetherui.api;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.phys.Vec2;

enum UIComponentType {panel,label,icon,row,table, column}

public class UIBuilder {
    // 当前正在构建的组件的配置
    private UIComponentType type;
    private Vec2 size;
    private final List<UIBuilder> children = new ArrayList<>();

    public UIBuilder type(UIComponentType type) {
        this.type = type;
        return this; // 关键：返回自身，支持继续链式调用
    }

    public UIBuilder size(Vec2 size) {
        this.size = size;
        return this;
    }

    public UIBuilder add(UIBuilder child) {
        this.children.add(child);
        return this;
    }

    public UIComponent build() {
        UIComponent component = new UIComponent(type, size);
        for (UIBuilder childBuilder : children) {
            component.addChild(childBuilder.build()); // 递归构建子节点
        }
        return component;
    }

    public void new_bd(){
        UIBuilder root = new UIBuilder()
                .type(UIComponentType.panel)
                .size(new Vec2(300, 200))
                .add(new UIBuilder()          // 第一个子节点：icon
                        .type(UIComponentType.icon)
                        .size(new Vec2(100, 30)))
                .add(new UIBuilder()          // 第二个子节点：Label
                        .type(UIComponentType.label)
                        .size(new Vec2(100, 30)));

        // 最终生成 UI 组件树
        UIComponent uiTree = root.build();
    }
}
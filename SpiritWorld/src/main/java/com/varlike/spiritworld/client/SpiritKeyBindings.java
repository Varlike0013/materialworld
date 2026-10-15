package com.varlike.spiritworld.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class SpiritKeyBindings {

    /**
     * 使用物质世界的按键分类，在控制设置里显示在物质世界下方。
     */
    public static final String CATEGORY = "key.categories.materialworld";

    public static final KeyMapping OPEN_SKILL_TREE = new KeyMapping(
            "key.spiritworld.open_skill_tree",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_K,
            CATEGORY
    );

    public static final KeyMapping OPEN_LOADOUT = new KeyMapping(
            "key.spiritworld.open_loadout",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_L,
            CATEGORY
    );

    /** 按下立刻打开选盘，无长按判定 */
    public static final KeyMapping OPEN_RADIAL = new KeyMapping(
            "key.spiritworld.open_radial",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            CATEGORY
    );

    public static final KeyMapping USE_SKILL = new KeyMapping(
            "key.spiritworld.use_skill",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            CATEGORY
    );
}
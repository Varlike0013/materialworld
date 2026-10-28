package com.varlike.spiritworld.api;

import net.minecraft.network.chat.Component;

/**
 * 单条灵视信息行。
 *
 * @param text                显示文本
 * @param requiredVisionLevel 显示所需的最低灵视等级
 */
public record SpiritInfoLine(Component text, int requiredVisionLevel) {

    public boolean isVisibleAt(int visionLevel) {
        return visionLevel >= requiredVisionLevel;
    }
}
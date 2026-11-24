package com.varlike.spiritworld.client.aether;

import com.varlike.aetherui.api.hud.BuffIcon;
import com.varlike.aetherui.api.paint.CanvasContext;
import com.varlike.spiritworld.client.ClientSpiritData;
import com.varlike.spiritworld.skill.SkillNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** 把已激活的被动技能显示为 AetherUI Buff 图标。 */
public final class SpiritPassiveIcon implements BuffIcon {

    private final SkillNode node;

    public SpiritPassiveIcon(SkillNode node) {
        this.node = node;
    }

    @Override
    public ResourceLocation id() {
        return node.getId();
    }

    @Override
    public String label() {
        return Component.translatable(node.getNameKey()).getString();
    }

    @Override
    public float width() {
        return 17f;
    }

    @Override
    public float height() {
        return 17f;
    }

    @Override
    public boolean visible(LocalPlayer player) {
        return ClientSpiritData.isPassiveActive(node.getId());
    }

    @Override
    public void paint(CanvasContext ctx, LocalPlayer player) {
        float cx = ctx.width() / 2f;
        float cy = ctx.height() / 2f;
        float r = Math.min(cx, cy) - 0.5f;
        if (r <= 1f) return;

        int borderColor = node.isAlwaysOn() ? 0xFF4CAF50 : 0xFFFFD54F;
        CanvasShapes.fillCircle(ctx, cx, cy, r, borderColor);
        CanvasShapes.fillCircle(ctx, cx, cy, r - 1f, 0xC0201030);

        Component icon = Component.literal(node.getIcon());
        float tw = Minecraft.getInstance().font.width(icon);
        ctx.drawString(icon, cx - tw / 2f, cy - 5f, 0xFFFFFFFF, false);
    }
}

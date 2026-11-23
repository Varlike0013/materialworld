package com.varlike.aetherui.core.hud;

import com.varlike.aetherui.AetherUIMod;
import com.varlike.aetherui.Config;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

/**
 * 3D 世界空间血条：怪物与动物头顶显示，始终面向玩家。
 * 复用玩家 HUD 的血条样式：HP 条（带数值）+ 右侧护甲数值方块（仅有护甲时绘制）。
 * 名字居中显示在上方。被方块遮挡时不渲染。
 */
@EventBusSubscriber(modid = AetherUIMod.MOD_ID, value = Dist.CLIENT)
public final class WorldHealthBarRenderer {

    private record Bar(float x, float y, float width, float height, float ratio,
                       Component name, int armor, int health, int maxHealth) {}

    private static final List<Bar> BARS = new ArrayList<>();

    private static final float BAR_W = 58f;       // 血条区宽度（容纳 HP 文字）
    private static final float BAR_H = 12f;       // 血条高度（与玩家 HUD 一致）
    private static final float ARMOR_W = 14f;     // 护甲方块宽度
    private static final float GAP = 2f;
    private static final float MAX_DISTANCE = 48f;

    private static final int PANEL_BG = 0x99000000;
    private static final int FRAME_BG = 0xB0080808;
    private static final int FRAME_LINE = 0xFF5A5A5A;
    private static final int HP_COLOR = 0xFFE03333;
    private static final int NAME_COLOR = 0xFFFFFFFF;
    private static final int ARMOR_COLOR = 0xFFC0C0C0;
    private static final int TEXT_COLOR = 0xFFFFFFFF;

    private WorldHealthBarRenderer() {}

    @SubscribeEvent
    static void onRenderLevel(RenderLevelStageEvent event) {
        BARS.clear();
        if (!Config.worldHealthBarEnabled()) return;
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;

        Matrix4f modelView = event.getModelViewMatrix();
        Matrix4f projection = event.getProjectionMatrix();
        Camera camera = event.getCamera();
        Vec3 camPos = camera.getPosition();
        int screenW = mc.getWindow().getWidth();
        int screenH = mc.getWindow().getHeight();
        float guiScale = (float) mc.getWindow().getGuiScale();

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (entity == player) continue;
            if (!entity.isAlive() || entity.isInvisibleTo(player)) continue;
            if (!shouldShow(living)) continue;

            double dist = entity.position().distanceTo(camPos);
            if (dist > MAX_DISTANCE || dist < 0.1) continue;

            Vec3 head = new Vec3(entity.getX(), entity.getY() + entity.getBbHeight() * 0.8f, entity.getZ());
            BlockHitResult hit = mc.level.clip(new ClipContext(
                    camPos, head, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, entity));
            if (hit.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK
                    && hit.getLocation().distanceTo(camPos) < dist - 0.5f) continue;

            Vector4f pos = new Vector4f(
                    (float) (entity.getX() - camPos.x),
                    (float) (entity.getY() + entity.getBbHeight() + 0.45f - camPos.y),
                    (float) (entity.getZ() - camPos.z), 1f);
            pos.mul(modelView).mul(projection);
            if (pos.w() <= 0f) continue;

            float ndcX = pos.x() / pos.w();
            float ndcY = pos.y() / pos.w();
            if (ndcX < -1f || ndcX > 1f || ndcY < -1f || ndcY > 1f) continue;

            float sx = (ndcX + 1f) * 0.5f * screenW / guiScale;
            float sy = (1f - ndcY) * 0.5f * screenH / guiScale;

            // 血条尺寸随距离轻微缩放
            float scale = Mth.clamp(10f / (float) dist, 0.6f, 1.6f);
            float w = BAR_W * scale;
            float h = BAR_H * scale;
            float ratio = Mth.clamp(living.getHealth() / Math.max(1f, living.getMaxHealth()), 0f, 1f);
            int armor = living.getArmorValue();
            int health = Math.round(living.getHealth());
            int maxHealth = Math.round(living.getMaxHealth());

            BARS.add(new Bar(sx, sy, w, h, ratio, living.getName(), armor, health, maxHealth));
        }
    }

    /** 由 ScreenRenderHandler 的 GUI layer 调用。 */
    public static void renderBars(GuiGraphics g) {
        if (!Config.worldHealthBarEnabled() || BARS.isEmpty()) return;
        var font = Minecraft.getInstance().font;

        for (Bar bar : BARS) {
            int cx = Math.round(bar.x());
            int cy = Math.round(bar.y());
            int hpW = Math.round(bar.width());
            int bh = Math.round(bar.height());
            boolean hasArmor = bar.armor() > 0;
            int armorW = hasArmor ? Math.round(ARMOR_W) : 0;
            int gap = hasArmor ? Math.round(GAP) : 0;
            int contentW = hpW + gap + armorW;

            // 面板宽度取名字与内容较大者
            int nameW = font.width(bar.name());
            int panelW = Math.max(contentW, nameW) + 6;
            int panelX = cx - panelW / 2;
            int nameY = cy - font.lineHeight - 2;
            int panelY = nameY - 2;
            int panelH = font.lineHeight + bh + 4;

            g.fill(panelX, panelY, panelX + panelW, panelY + panelH, PANEL_BG);

            // 名字居中
            g.drawString(font, bar.name(), panelX + (panelW - nameW) / 2, panelY + 1, NAME_COLOR, false);

            // 内容整体居中
            int contentX = cx - contentW / 2;
            int barY = panelY + font.lineHeight + 1;

            // 血条：复用玩家 HUD 样式（深色框 + 红色填充 + 数值文字）
            drawBar(g, contentX, barY, hpW, bh, bar.health(), bar.maxHealth(), bar.ratio());

            // 护甲方块：仅有护甲时绘制，复用玩家 HUD 样式
            if (hasArmor) {
                drawArmorBox(g, contentX + hpW + gap, barY, armorW, bh, bar.armor());
            }
        }
    }

    private static void drawBar(GuiGraphics g, int x, int y, int w, int h,
                                int health, int maxHealth, float ratio) {
        frame(g, x, y, w, h);
        int fw = Math.round((w - 4) * ratio);
        if (fw > 0) g.fill(x + 2, y + 2, x + 2 + fw, y + h - 2, HP_COLOR);
        String label = "HP " + health + "/" + maxHealth;
        g.drawString(Minecraft.getInstance().font, label, x + 3, y + 2, TEXT_COLOR, true);
    }

    private static void drawArmorBox(GuiGraphics g, int x, int y, int w, int h, int armor) {
        frame(g, x, y, w, h);
        String text = String.valueOf(armor);
        int tw = Minecraft.getInstance().font.width(text);
        g.drawString(Minecraft.getInstance().font, text, x + (w - tw) / 2, y + 2, ARMOR_COLOR, false);
    }

    private static void frame(GuiGraphics g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, FRAME_BG);
        g.fill(x, y, x + w, y + 1, FRAME_LINE);
        g.fill(x, y + h - 1, x + w, y + h, FRAME_LINE);
        g.fill(x, y, x + 1, y + h, FRAME_LINE);
        g.fill(x + w - 1, y, x + w, y + h, FRAME_LINE);
    }

    private static boolean shouldShow(LivingEntity living) {
        if (!(living instanceof Mob)) return false;
        boolean hostile = living instanceof Enemy;
        boolean animal = living instanceof Animal;
        boolean neutral = !hostile && !animal;
        if (hostile) return Config.showHostileHealthBar();
        if (animal)  return Config.showAnimalHealthBar();
        return Config.showNeutralHealthBar();
    }
}

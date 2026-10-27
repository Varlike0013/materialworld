package com.varlike.spiritworld.skill;

import com.varlike.spiritworld.Config;
import com.varlike.spiritworld.capability.PlayerSpirit;
import com.varlike.spiritworld.network.ClientboundScanResultPacket;
import com.varlike.spiritworld.network.ClientboundScanResultPacket.OreIcon;
import com.varlike.spiritworld.network.NetworkHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.*;

/**
 * 精神扫描服务端逻辑：
 * 遍历半径内方块 → 按类型分组 → BFS 合并连通分量 → 按距离排序截断 → 发包。
 * 矿物表查询委托给 {@link SpiritScanOreTable}。
 */
public final class SpiritScanHandler {

    private SpiritScanHandler() {}

    // ==================== 入口 ====================

    public static boolean handleSpiritScan(ServerPlayer player, PlayerSpirit data) {
        int level = data.getSkillLevel(ModSkills.SPIRIT_SCAN_ID);
        if (level <= 0) return false;

        int range = data.getScanRange();
        long cost = SkillValues.scanCostForRange(range);

        if (data.getSpirit() < cost) {
            player.displayClientMessage(
                    Component.translatable("message.spiritworld.spirit_scan.no_spirit", cost)
                            .withStyle(ChatFormatting.RED), true);
            return false;
        }

        Map<Block, Item> targets = resolveTargets(data, level);
        if (targets.isEmpty()) {
            player.displayClientMessage(
                    Component.translatable("message.spiritworld.spirit_scan.no_ore")
                            .withStyle(ChatFormatting.RED), true);
            return false;
        }

        List<OreIcon> icons = scanAround(player, range, targets);

        data.consumeSpirit(cost);
        data.markDirty();
        NetworkHandler.syncTo(player);

        if (icons.isEmpty()) {
            player.displayClientMessage(
                    Component.translatable("message.spiritworld.spirit_scan.not_found")
                            .withStyle(ChatFormatting.YELLOW), true);
            return true;
        }

        int duration = SkillValues.scanDurationSeconds(level);
        PacketDistributor.sendToPlayer(player,
                new ClientboundScanResultPacket(icons, duration));
        player.displayClientMessage(
                Component.translatable("message.spiritworld.spirit_scan.found", icons.size())
                        .withStyle(ChatFormatting.GREEN), true);
        return true;
    }

    // ==================== 目标解析 ====================

    private static Map<Block, Item> resolveTargets(PlayerSpirit data, int level) {
        Map<Block, Item> result = new HashMap<>();
        Set<ResourceLocation> selected = data.getScanOres();

        for (ResourceLocation oreId : selected) {
            if (SkillValues.scanOreUnlockLevel(oreId) > level) continue;

            if (oreId.equals(SkillValues.SCAN_OTHER)) {
                result.putAll(SpiritScanOreTable.collectOtherModOres());
            } else {
                List<Block> blocks = SpiritScanOreTable.blocksOf(oreId);
                Item rep = SpiritScanOreTable.representativeOf(oreId);
                if (rep == null || rep == Items.AIR || blocks.isEmpty()) continue;
                for (Block b : blocks) result.put(b, rep);
            }
        }
        return result;
    }

    // ==================== 扫描 + BFS ====================

    private static List<OreIcon> scanAround(ServerPlayer player, int range,
                                            Map<Block, Item> targets) {
        Level level = player.level();
        BlockPos center = player.blockPosition();
        int r = range;
        int r2 = r * r;

        Map<Block, Set<BlockPos>> found = new HashMap<>();

        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                int dy2 = dy * dy;
                for (int dz = -r; dz <= r; dz++) {
                    if (dx * dx + dy2 + dz * dz > r2) continue;
                    BlockPos pos = center.offset(dx, dy, dz);
                    if (!level.isLoaded(pos)) continue;
                    BlockState state = level.getBlockState(pos);
                    Block block = state.getBlock();
                    if (!targets.containsKey(block)) continue;
                    found.computeIfAbsent(block, k -> new HashSet<>()).add(pos.immutable());
                }
            }
        }

        List<OreIcon> icons = new ArrayList<>();
        for (var entry : found.entrySet()) {
            Block block = entry.getKey();
            Item rep = targets.get(block);
            if (rep == null || rep == Items.AIR) continue;
            Set<BlockPos> positions = entry.getValue();
            Set<BlockPos> visited = new HashSet<>();

            for (BlockPos start : positions) {
                if (visited.contains(start)) continue;

                List<BlockPos> cluster = new ArrayList<>();
                Deque<BlockPos> queue = new ArrayDeque<>();
                queue.add(start);
                visited.add(start);

                while (!queue.isEmpty()) {
                    BlockPos cur = queue.poll();
                    cluster.add(cur);
                    for (int ox = -1; ox <= 1; ox++)
                        for (int oy = -1; oy <= 1; oy++)
                            for (int oz = -1; oz <= 1; oz++) {
                                if (ox == 0 && oy == 0 && oz == 0) continue;
                                BlockPos next = cur.offset(ox, oy, oz);
                                if (visited.contains(next)) continue;
                                if (!positions.contains(next)) continue;
                                visited.add(next);
                                queue.add(next);
                            }
                }

                double cx = 0, cy = 0, cz = 0;
                for (BlockPos p : cluster) {
                    cx += p.getX() + 0.5;
                    cy += p.getY() + 0.5;
                    cz += p.getZ() + 0.5;
                }
                int n = cluster.size();
                icons.add(new OreIcon(cx / n, cy / n, cz / n, rep, n));
            }
        }

        double px = player.getX(), py = player.getY(), pz = player.getZ();
        icons.sort(Comparator.comparingDouble(i -> {
            double dx = i.x() - px;
            double dy = i.y() - py;
            double dz = i.z() - pz;
            return dx * dx + dy * dy + dz * dz;
        }));

        if (icons.size() > Config.scanMaxIcons) {
            icons = new ArrayList<>(icons.subList(0, Config.scanMaxIcons));
        }
        return icons;
    }
}
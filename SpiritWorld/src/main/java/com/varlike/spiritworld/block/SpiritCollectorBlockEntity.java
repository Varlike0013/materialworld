package com.varlike.spiritworld.block;

import com.varlike.spiritworld.api.SpiritInfoHolder;
import com.varlike.spiritworld.api.SpiritInfoLine;
import com.varlike.spiritworld.component.CrystalMana;
import com.varlike.spiritworld.item.ManaCrystalItem;
import com.varlike.spiritworld.registry.ModBlockEntities;
import com.varlike.spiritworld.skill.ManaType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class SpiritCollectorBlockEntity extends BlockEntity implements SpiritInfoHolder {

    /** 每 10 tick（0.5 秒）同步一次到客户端 */
    private static final int SYNC_INTERVAL = 10;
    private final Map<ManaType, Long> manaByType = new EnumMap<>(ManaType.class);
    private ManaType currentType = ManaType.NONE;
    private int envCheckTimer = 0;
    private int syncTimer = 0;

    public SpiritCollectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SPIRIT_COLLECTOR.get(), pos, state);
    }

    private ItemStack crystal = ItemStack.EMPTY;

    public ItemStack getCrystal() { return crystal; }

    public void setCrystal(ItemStack stack) {
        this.crystal = (stack == null || stack.isEmpty()) ? ItemStack.EMPTY : stack;
        setChanged();
    }
    /** 额外容量（来自水晶） */
    public long getCrystalCapacity() {
        if (crystal.isEmpty() || !(crystal.getItem() instanceof ManaCrystalItem item)) return 0L;
        return item.getCapacity();
    }
    /** 剩余可容量 */
    public long getRemainingCapacity() {
        return Math.max(0L, getBaseCapacity() + getCrystalCapacity() - getTotalMana());
    }

    private long getBaseCapacity() {
        return ((SpiritCollectorBlock) getBlockState().getBlock()).getCapacity();
    }
    public long getSpeed()    { return ((SpiritCollectorBlock) getBlockState().getBlock()).getSpeed(); }
    public long getCapacity() { return ((SpiritCollectorBlock) getBlockState().getBlock()).getCapacity(); }
    public int  getTier()     { return ((SpiritCollectorBlock) getBlockState().getBlock()).getTier(); }

    /** 当前聚集属性（可能随环境变化） */
    public ManaType getCurrentType() { return currentType; }

    /** 某属性的存量 */
    public long getMana(ManaType type) {
        return manaByType.getOrDefault(type, 0L);
    }

    /** 所有属性的存量快照（只读） */
    public Map<ManaType, Long> getAllMana() {
        return Map.copyOf(manaByType);
    }

    /** 总存量 */
    public long getTotalMana() {
        long sum = 0L;
        for (long v : manaByType.values()) sum += v;
        return sum;
    }

    /** 满充剩余秒数（基于当前速度） */
    public long getRemainingSeconds() {
        long speed = getSpeed();
        if (speed <= 0) return 0L;
        long remain = getRemainingCapacity();
        if (remain <= 0) return 0L;
        return (remain + speed - 1) / speed;
    }
    public static void serverTick(Level level, BlockPos pos, BlockState state, SpiritCollectorBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        // 1. 每 20 tick 检测环境
        be.envCheckTimer++;
        if (be.envCheckTimer >= 20) {
            be.envCheckTimer = 0;
            ManaType detected = ManaEnvironmentDetector.detect(level, pos);
            if (detected != be.currentType) {
                be.currentType = detected;
                be.setChanged();
                serverLevel.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
            }
        }

        // 2. 累加
        long remaining = be.getRemainingCapacity();
        if (remaining <= 0) return;

        long speed = be.getSpeed();
        if (speed <= 0) return;

        long add = Math.min(speed, remaining);
        be.manaByType.merge(be.currentType, add, Long::sum);
        be.setChanged();

        // 3. 节流同步
        be.syncTimer++;
        if (be.syncTimer >= SYNC_INTERVAL) {
            be.syncTimer = 0;
            serverLevel.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
    }
    /**
     * 玩家 shift+右键装入水晶。旧水晶（含魔力）退回背包。
     */
    public void insertCrystal(ItemStack newCrystal, Player player) {
        ItemStack old = this.crystal;
        if (old.isEmpty()) {
            this.crystal = newCrystal.copy();
            if (!player.isCreative()) newCrystal.shrink(1);
        } else {
            // 交换：旧水晶带走当前魔力
            ItemStack oldOut = extractCrystalWithMana();
            this.crystal = newCrystal.copy();
            if (!player.isCreative()) newCrystal.shrink(1);
            if (!player.getInventory().add(oldOut)) player.drop(oldOut, false);
        }
        setChanged();
    }

    /** 取出水晶：BE 的魔力灌入水晶（不超过水晶容量） */
    public ItemStack extractCrystalWithMana() {
        if (crystal.isEmpty()) return ItemStack.EMPTY;
        ItemStack out = crystal.copy();
        if (out.getItem() instanceof ManaCrystalItem item) {
            CrystalMana mana = ManaCrystalItem.getMana(out);
            long free = item.getCapacity() - mana.total();
            if (free > 0) {
                for (ManaType type : ManaType.values()) {
                    if (free <= 0) break;
                    long have = getMana(type);
                    if (have <= 0) continue;
                    long take = Math.min(have, free);
                    consumeMana(type, take);
                    mana = mana.with(type, mana.get(type) + take);
                    free -= take;
                }
                ManaCrystalItem.setMana(out, mana);
            }
        }
        crystal = ItemStack.EMPTY;
        setChanged();
        return out;
    }

    // ==================== 持久化 ====================

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        manaByType.clear();

        CompoundTag manaTag = tag.getCompound("Mana");
        for (String key : manaTag.getAllKeys()) {
            ManaType type = ManaType.byId(key);
            long v = manaTag.getLong(key);
            if (v > 0) manaByType.put(type, v);
        }
        crystal = ItemStack.EMPTY;
        if (tag.contains("Crystal")) crystal = ItemStack.parseOptional(registries, tag.getCompound("Crystal"));
        currentType = ManaType.byId(tag.getString("CurrentType"));
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        CompoundTag manaTag = new CompoundTag();
        for (Map.Entry<ManaType, Long> e : manaByType.entrySet()) {
            if (e.getValue() > 0) manaTag.putLong(e.getKey().id(), e.getValue());
        }
        tag.put("Mana", manaTag);
        if (!crystal.isEmpty()) tag.put("Crystal", crystal.save(registries));
        tag.putString("CurrentType", currentType.id());
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        CompoundTag manaTag = new CompoundTag();
        for (Map.Entry<ManaType, Long> e : manaByType.entrySet()) {
            if (e.getValue() > 0) manaTag.putLong(e.getKey().id(), e.getValue());
        }
        tag.put("Mana", manaTag);
        tag.putString("CurrentType", currentType.id());
        return tag;
    }
    /** 从指定属性扣除魔力，返回实际扣除量 */
    public long consumeMana(ManaType type, long amount) {
        long cur = manaByType.getOrDefault(type, 0L);
        long consumed = Math.min(cur, amount);
        if (consumed <= 0) return 0L;
        long remain = cur - consumed;
        if (remain <= 0) manaByType.remove(type);
        else manaByType.put(type, remain);
        setChanged();
        return consumed;
    }
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    @Override
    public List<SpiritInfoLine> getSpiritInfo() {
        List<SpiritInfoLine> list = new ArrayList<>();

        // Lv1：名称
        list.add(new SpiritInfoLine(
                Component.translatable(nameKey()).withStyle(ChatFormatting.GOLD), 1));

        // Lv2：等级 + 当前属性
        list.add(new SpiritInfoLine(
                Component.translatable("gui.spiritworld.collector.tier", getTier())
                        .withStyle(ChatFormatting.YELLOW), 2));
        list.add(new SpiritInfoLine(
                currentType.applyStyle(
                        Component.translatable("gui.spiritworld.collector.type", currentType.displayName())), 2));

        // Lv3：每种属性的存量（非零 + 当前属性）
        for (ManaType type : ManaType.values()) {
            long amt = getMana(type);
            if (amt <= 0 && type != currentType) continue;
            list.add(new SpiritInfoLine(
                        type.applyStyle(
                            Component.translatable("gui.spiritworld.collector.mana_entry",
                                    type.displayName(), amt, getCapacity())), 3));
        }

        // Lv3：总存量
        list.add(new SpiritInfoLine(
                Component.translatable("gui.spiritworld.collector.total",
                                getTotalMana(), getCapacity())
                        .withStyle(ChatFormatting.AQUA), 3));

        // Lv4：速度
        list.add(new SpiritInfoLine(
                Component.translatable("gui.spiritworld.collector.speed", getSpeed())
                        .withStyle(ChatFormatting.LIGHT_PURPLE), 4));

        // Lv5：百分比 + 剩余秒数
        long cap = getCapacity();
        long total = getTotalMana();
        float pct = cap > 0 ? (float) total / cap * 100f : 0f;
        list.add(new SpiritInfoLine(
                Component.translatable("gui.spiritworld.collector.percent", String.format("%.1f", pct))
                        .withStyle(ChatFormatting.GREEN), 5));

        long remain = getRemainingSeconds();
        list.add(new SpiritInfoLine(
                remain > 0
                        ? Component.translatable("gui.spiritworld.collector.remaining", remain)
                        .withStyle(ChatFormatting.GRAY)
                        : Component.translatable("gui.spiritworld.collector.full")
                        .withStyle(ChatFormatting.DARK_GREEN),
                5));

        return list;
    }

    private String nameKey() {
        return switch (getTier()) {
            case 1 -> "block.spiritworld.spirit_collector_basic";
            case 2 -> "block.spiritworld.spirit_collector_advanced";
            case 3 -> "block.spiritworld.spirit_collector_ultimate";
            default -> "block.spiritworld.spirit_collector_basic";
        };
    }
}
package com.varlike.spiritworld.block;

import com.varlike.spiritworld.api.SpiritInfoHolder;
import com.varlike.spiritworld.api.SpiritInfoLine;
import com.varlike.spiritworld.component.CrystalMana;
import com.varlike.spiritworld.item.ManaCrystalItem;
import com.varlike.spiritworld.registry.ModBlockEntities;
import com.varlike.spiritworld.skill.ManaCraftingRecipe;
import com.varlike.spiritworld.skill.ManaCraftingRecipes;
import com.varlike.spiritworld.skill.ManaType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

/**
 * 魔力核心：收集 9×9×9 聚集器魔力、维护容量加成、合成配方、3×3 输出容器。
 */
public class SpiritCoreBlockEntity extends BlockEntity implements Container, SpiritInfoHolder {

    // ==================== 常量 ====================
    public static final int SLOT_COUNT = 9;
    private static final long BASE_CAPACITY = 100L;
    private static final int COLLECT_INTERVAL = 20;
    private static final int RANGE = 4;
    private static final int SYNC_INTERVAL = 2;

    /** 容量加成 BFS 深度：1 = 只算直接相邻；2 = 再算相邻方块的相邻 */
    private static final int CAPACITY_BFS_DEPTH = 5;
    /** 每远一层，加成按此比例衰减（0.5 = 半额） */
    private static final double CAPACITY_DECAY = 0.75;

    // ==================== 状态 ====================
    private final Map<ManaType, Long> mana = new EnumMap<>(ManaType.class);
    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);

    private long capacityBonus = 0L;
    private int selectedIndex = -1;
    private int progress = 0;

    private int tickTimer = 0;
    private int syncTimer = 0;
    private int nearbyCollectors = 0;
    private int targetCount = 0;
    private int completedCount = 0;
    private boolean batchActive = false;
    private boolean infiniteMode = false;      // MAX 触发
    private int activeRecipeIndex = -1;        // 正在炼成的配方
    public static final int BTN_MIN    = 1000;
    public static final int BTN_MINUS5 = 1001;
    public static final int BTN_MINUS1 = 1002;
    public static final int BTN_PLUS1  = 1003;
    public static final int BTN_PLUS5  = 1004;
    public static final int BTN_MAX    = 1005;
    public static final int BTN_START  = 2000;
    public static final int BTN_CANCEL = 2001;

    public SpiritCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SPIRIT_CORE.get(), pos, state);
    }

    // ==================== 查询 ====================
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

    public long getTotalMana() {
        long s = 0;
        for (long v : mana.values()) s += v;
        return s;
    }
    public long getTotalCapacity() {
        return BASE_CAPACITY + getCrystalCapacity() + capacityBonus;
    }
    public long getRemainingCapacity() {
        return Math.max(0L, getBaseCapacity() + getCrystalCapacity() - getTotalMana());
    }

    private long getBaseCapacity() {
        return ((SpiritCollectorBlock) getBlockState().getBlock()).getCapacity();
    }
    public long getMana(ManaType type) { return mana.getOrDefault(type, 0L); }
    public long getCapacityBonus()     { return capacityBonus; }
    public int  getNearbyCollectors()  { return nearbyCollectors; }

    public int getSelectedIndex() { return selectedIndex; }
    public int getProgress()      { return progress; }
    public ManaCraftingRecipe getRecipe() {
        return selectedIndex < 0 ? null : ManaCraftingRecipes.get(selectedIndex);
    }
    public int getTargetCount()     { return targetCount; }
    public int getCompletedCount()  { return completedCount; }
    public boolean isBatchActive()  { return batchActive; }

    private int calcMaxByMana(ManaCraftingRecipe recipe) {
        int max = Integer.MAX_VALUE;
        for (var e : recipe.manaCost().entrySet()) {
            long v = e.getValue();
            if (v <= 0) continue;
            int can = (int) (getMana(e.getKey()) / v);
            max = Math.min(max, can);
        }
        return max == Integer.MAX_VALUE ? 0 : max;
    }

    private int calcMaxBySpace(ManaCraftingRecipe recipe) {
        ItemStack output = recipe.output();
        int per = output.getCount();
        if (per <= 0) return 0;
        int space = 0;
        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemStack s = items.get(i);
            if (s.isEmpty()) space += output.getMaxStackSize();
            else if (ItemStack.isSameItemSameComponents(s, output))
                space += s.getMaxStackSize() - s.getCount();
        }
        return space / per;
    }
    public int getActiveRecipeIndex() { return activeRecipeIndex; }
    public boolean isInfiniteMode()   { return infiniteMode; }
    public void setSelectedIndex(int i) {
        this.selectedIndex = i;
        if (!batchActive) {
            // 不在炼成中才允许重置计数
            this.targetCount = 0;
            this.completedCount = 0;
            this.progress = 0;
            this.infiniteMode = false;
            this.activeRecipeIndex = -1;
        }
        // 炼成中：只改 selectedIndex，其他不动
        setChanged();
    }
    public void adjustTarget(int action) {
        if (batchActive) return;               // 炼成中禁用
        if (selectedIndex < 0) return;

        // 换了配方就重置完成数
        if (activeRecipeIndex != selectedIndex && !batchActive) {
            completedCount = 0;
            progress = 0;
        }

        switch (action) {
            case BTN_MIN -> {
                infiniteMode = false;
                targetCount = Math.max(1, completedCount);
            }
            case BTN_MAX -> {
                infiniteMode = true;
                targetCount = 0;
            }
            case BTN_PLUS1 -> {
                if (!infiniteMode) targetCount = Math.max(1, targetCount + 1);
            }
            case BTN_PLUS5 -> {
                if (!infiniteMode) targetCount = Math.max(1, targetCount + 5);
            }
            case BTN_MINUS1 -> {
                if (!infiniteMode) targetCount = Math.max(1, targetCount - 1);
            }
            case BTN_MINUS5 -> {
                if (!infiniteMode) targetCount = Math.max(1, targetCount - 5);
            }
            default -> { return; }
        }
        setChanged();
    }
    public void startCrafting() {
        if (selectedIndex < 0) return;
        if (batchActive) return;               // 已在炼成中
        if (getRecipe() == null) return;

        // 新配方：重置计数
        if (activeRecipeIndex != selectedIndex) {
            completedCount = 0;
            progress = 0;
        }
        activeRecipeIndex = selectedIndex;

        if (!infiniteMode && targetCount <= completedCount) return;
        batchActive = true;
        progress = 0;
        setChanged();
    }
    public void cancelCrafting() {
        if (!batchActive) return;
        batchActive = false;
        progress = 0;
        if (!infiniteMode) {
            targetCount = completedCount;      // 已完成不回退，剩余作废
        } else {
            infiniteMode = false;
            targetCount = completedCount;
        }
        setChanged();
    }

    public void consumeMana(ManaType type, long amount) {
        long cur = mana.getOrDefault(type, 0L);
        long next = Math.max(0L, cur - amount);
        if (next <= 0) mana.remove(type);
        else mana.put(type, next);
        setChanged();
    }

    // ==================== Server Tick ====================

    public static void serverTick(Level level, BlockPos pos, BlockState state, SpiritCoreBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        be.tickTimer++;
        if (be.tickTimer >= COLLECT_INTERVAL) {
            be.tickTimer = 0;
            be.recalculateCapacity(level, pos);
            be.pullFromCollectors(level, pos);
        }

        be.tickCrafting();

        be.syncTimer++;
        if (be.syncTimer >= SYNC_INTERVAL) {
            be.syncTimer = 0;
            serverLevel.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
        be.setChanged();
    }

    private void tickCrafting() {
        if (activeRecipeIndex < 0 || !batchActive) {
            if (progress != 0) progress = 0;
            return;
        }
        ManaCraftingRecipe recipe = ManaCraftingRecipes.get(activeRecipeIndex);
        if (recipe == null) {
            batchActive = false;
            activeRecipeIndex = -1;
            progress = 0;
            return;
        }

        // 有限模式且已完成够 → 停止
        if (!infiniteMode && completedCount >= targetCount) {
            batchActive = false;
            progress = 0;
            return;
        }

        if (!hasMana(recipe) || !hasSpace(recipe)) return;

        progress++;
        if (progress >= recipe.craftingTicks()) {
            consumeRecipeMana(recipe);
            addOutput(recipe);
            completedCount++;
            progress = 0;
            if (!infiniteMode && completedCount >= targetCount) batchActive = false;
        }
    }

    private boolean hasMana(ManaCraftingRecipe recipe) {
        for (Map.Entry<ManaType, Long> e : recipe.manaCost().entrySet()) {
            if (getMana(e.getKey()) < e.getValue()) return false;
        }
        return true;
    }

    private void consumeRecipeMana(ManaCraftingRecipe recipe) {
        for (Map.Entry<ManaType, Long> e : recipe.manaCost().entrySet()) {
            consumeMana(e.getKey(), e.getValue());
        }
    }

    private boolean hasSpace(ManaCraftingRecipe recipe) {
        ItemStack output = recipe.output();
        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemStack s = items.get(i);
            if (s.isEmpty()) return true;
            if (ItemStack.isSameItemSameComponents(s, output)
                    && s.getCount() + output.getCount() <= s.getMaxStackSize()) return true;
        }
        return false;
    }

    private void addOutput(ManaCraftingRecipe recipe) {
        ItemStack output = recipe.output().copy();
        for (int i = 0; i < SLOT_COUNT && !output.isEmpty(); i++) {
            ItemStack s = items.get(i);
            if (s.isEmpty()) {
                items.set(i, output);
                output = ItemStack.EMPTY;
            } else if (ItemStack.isSameItemSameComponents(s, output)) {
                int space = s.getMaxStackSize() - s.getCount();
                int move = Math.min(space, output.getCount());
                s.grow(move);
                output.shrink(move);
            }
        }
    }

    // ==================== 容量加成：BFS 2 层 ====================

    /**
     * BFS 遍历核心周围 MAGIC 方块。
     * 距离 1（直接相邻）：全额。
     * 距离 2（通过另一个魔力方块到达）：×CAPACITY_DECAY。
     */
    private void recalculateCapacity(Level level, BlockPos pos) {
        long bonus = 0L;
        Set<BlockPos> visited = new HashSet<>();
        visited.add(pos);

        List<BlockPos> frontier = new ArrayList<>();
        frontier.add(pos);

        for (int depth = 1; depth <= CAPACITY_BFS_DEPTH; depth++) {
            double weight = Math.pow(CAPACITY_DECAY, depth - 1);   // depth=1 → 1.0；depth=2 → 0.5
            List<BlockPos> next = new ArrayList<>();

            for (BlockPos origin : frontier) {
                for (int dx = -1; dx <= 1; dx++)
                    for (int dy = -1; dy <= 1; dy++)
                        for (int dz = -1; dz <= 1; dz++) {
                            if (dx == 0 && dy == 0 && dz == 0) continue;
                            BlockPos p = origin.offset(dx, dy, dz);
                            if (visited.contains(p)) continue;
                            if (!level.isLoaded(p)) continue;
                            Block b = level.getBlockState(p).getBlock();
                            if (!(b instanceof ManaCapacityProvider provider)) continue;

                            visited.add(p);
                            next.add(p);
                            bonus += (long) (provider.getCapacityBonus() * weight);
                        }
            }
            frontier = next;
            if (frontier.isEmpty()) break;
        }

        this.capacityBonus = bonus;
    }

    // ==================== 从聚集器抽取 ====================

    private void pullFromCollectors(Level level, BlockPos pos) {
        int count = 0;
        for (int dx = -RANGE; dx <= RANGE; dx++)
            for (int dy = -RANGE; dy <= RANGE; dy++)
                for (int dz = -RANGE; dz <= RANGE; dz++) {
                    BlockPos p = pos.offset(dx, dy, dz);
                    if (!level.isLoaded(p)) continue;
                    BlockEntity be = level.getBlockEntity(p);
                    if (!(be instanceof SpiritCollectorBlockEntity collector)) continue;
                    count++;
                    pullFromCollector(collector);
                }
        this.nearbyCollectors = count;
    }

    private void pullFromCollector(SpiritCollectorBlockEntity collector) {
        for (ManaType type : ManaType.values()) {
            long remaining = getRemainingCapacity();
            if (remaining <= 0) return;
            long available = collector.getMana(type);
            if (available <= 0) continue;
            long transfer = Math.min(available, remaining);
            long actual = collector.consumeMana(type, transfer);
            if (actual > 0) mana.merge(type, actual, Long::sum);
        }
    }

    // ==================== 容器 ====================

    public void dropContents(Level level, BlockPos pos) {
        for (ItemStack s : items) {
            if (!s.isEmpty()) {
                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, s);
            }
        }
    }

    @Override public int getContainerSize() { return SLOT_COUNT; }
    @Override public boolean isEmpty() {
        for (ItemStack s : items) if (!s.isEmpty()) return false;
        return true;
    }
    @Override public ItemStack getItem(int slot) { return items.get(slot); }
    @Override public ItemStack removeItem(int slot, int amount) {
        ItemStack s = items.get(slot);
        if (s.isEmpty()) return ItemStack.EMPTY;
        ItemStack out = s.split(amount);
        if (!s.isEmpty()) setChanged();
        return out;
    }
    @Override public ItemStack removeItemNoUpdate(int slot) {
        ItemStack s = items.get(slot);
        items.set(slot, ItemStack.EMPTY);
        return s;
    }
    @Override public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > stack.getMaxStackSize()) stack.setCount(stack.getMaxStackSize());
        setChanged();
    }
    @Override public boolean stillValid(Player p) {
        return p.distanceToSqr(worldPosition.getX() + 0.5,
                worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0;
    }
    @Override public void clearContent() { items.clear(); }
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

    // ==================== NBT ====================

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        mana.clear();
        CompoundTag m = tag.getCompound("Mana");
        for (String k : m.getAllKeys()) {
            ManaType t = ManaType.byId(k);
            long v = m.getLong(k);
            if (v > 0) mana.put(t, v);
        }
        selectedIndex = tag.getInt("SelectedIndex");
        progress = tag.getInt("Progress");
        activeRecipeIndex = tag.getInt("ActiveRecipeIndex");
        targetCount = tag.getInt("TargetCount");
        completedCount = tag.getInt("CompletedCount");
        batchActive = tag.getBoolean("BatchActive");
        infiniteMode = tag.getBoolean("InfiniteMode");
        crystal = ItemStack.EMPTY;
        if (tag.contains("Crystal")) crystal = ItemStack.parseOptional(registries, tag.getCompound("Crystal"));
        items.clear();
        CompoundTag itemsTag = tag.getCompound("Items");
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (itemsTag.contains("s" + i)) {
                items.set(i, ItemStack.parseOptional(registries, itemsTag.getCompound("s" + i)));
            }
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        CompoundTag m = new CompoundTag();
        for (Map.Entry<ManaType, Long> e : mana.entrySet()) {
            if (e.getValue() > 0) m.putLong(e.getKey().id(), e.getValue());
        }
        tag.put("Mana", m);
        tag.putInt("SelectedIndex", selectedIndex);
        tag.putInt("Progress", progress);
        tag.putInt("TargetCount", targetCount);
        tag.putInt("CompletedCount", completedCount);
        tag.putBoolean("BatchActive", batchActive);
        if (!crystal.isEmpty()) tag.put("Crystal", crystal.save(registries));
        CompoundTag itemsTag = new CompoundTag();
        tag.putInt("ActiveRecipeIndex", activeRecipeIndex);
        tag.putBoolean("InfiniteMode", infiniteMode);

        for (int i = 0; i < SLOT_COUNT; i++) {
            if (!items.get(i).isEmpty()) itemsTag.put("s" + i, items.get(i).save(registries));
        }
        tag.put("Items", itemsTag);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider reg) {
        CompoundTag tag = new CompoundTag();
        CompoundTag m = new CompoundTag();
        for (Map.Entry<ManaType, Long> e : mana.entrySet()) {
            if (e.getValue() > 0) m.putLong(e.getKey().id(), e.getValue());
        }
        tag.put("Mana", m);
        tag.putLong("CapacityBonus", capacityBonus);
        tag.putInt("NearbyCollectors", nearbyCollectors);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // ==================== HUD ====================

    @Override
    public List<SpiritInfoLine> getSpiritInfo() {
        List<SpiritInfoLine> list = new ArrayList<>();
        list.add(new SpiritInfoLine(
                Component.translatable("block.spiritworld.spirit_core").withStyle(ChatFormatting.GOLD), 1));
        list.add(new SpiritInfoLine(
                Component.translatable("gui.spiritworld.spirit_core.capacity",
                                getTotalMana(), getTotalCapacity())
                        .withStyle(ChatFormatting.AQUA), 2));
        list.add(new SpiritInfoLine(
                Component.translatable("gui.spiritworld.spirit_core.bonus", getCapacityBonus())
                        .withStyle(ChatFormatting.YELLOW), 3));
        for (ManaType type : ManaType.values()) {
            long amt = getMana(type);
            if (amt <= 0) continue;
            list.add(new SpiritInfoLine(
                    type.applyStyle(Component.translatable(
                            "gui.spiritworld.spirit_core.mana_entry", type.displayName(), amt)),
                    3));
        }
        list.add(new SpiritInfoLine(
                Component.translatable("gui.spiritworld.spirit_core.collectors", getNearbyCollectors())
                        .withStyle(ChatFormatting.GRAY), 4));
        return list;
    }
}
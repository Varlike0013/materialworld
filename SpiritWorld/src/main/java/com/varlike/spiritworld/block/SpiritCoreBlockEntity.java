package com.varlike.spiritworld.block;

import com.varlike.spiritworld.api.SpiritInfoHolder;
import com.varlike.spiritworld.api.SpiritInfoLine;
import com.varlike.spiritworld.component.CrystalMana;
import com.varlike.spiritworld.item.ManaCrystalItem;
import com.varlike.spiritworld.network.*;
import com.varlike.spiritworld.registry.ModBlockEntities;
import com.varlike.spiritworld.skill.ManaCraftingRecipe;
import com.varlike.spiritworld.skill.ManaCraftingRecipes;
import com.varlike.spiritworld.skill.ManaType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
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
 * 魔力核心：收集网络内的聚集器魔力、维护容量加成、合成配方、3×3 输出容器。
 */
public class SpiritCoreBlockEntity extends AbstractNetworkNodeBlockEntity implements Container, SpiritInfoHolder {

    // ==================== 常量 ====================
    public static final int SLOT_COUNT = 9;
    private static final long BASE_CAPACITY = 100L;
    private static final int COLLECT_INTERVAL = 20;
    private static final int SYNC_INTERVAL = 2;

    /** 容量加成 BFS 深度 */
    private static final int CAPACITY_BFS_DEPTH = 5;
    /** 每远一层，加成按此比例衰减 */
    private static final double CAPACITY_DECAY = 0.75;

    public static final int BTN_MIN    = 1000;
    public static final int BTN_MINUS5 = 1001;
    public static final int BTN_MINUS1 = 1002;
    public static final int BTN_PLUS1  = 1003;
    public static final int BTN_PLUS5  = 1004;
    public static final int BTN_MAX    = 1005;
    public static final int BTN_START  = 2000;
    public static final int BTN_CANCEL = 2001;
    public static final int BTN_REBUILD_NETWORK = 3000; // UI 重构全网按钮

    // ==================== 状态 ====================
    private final Map<ManaType, Long> mana = new EnumMap<>(ManaType.class);
    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private ItemStack crystal = ItemStack.EMPTY;

    private long capacityBonus = 0L;
    private int selectedIndex = -1;
    private int progress = 0;
    private int tickTimer = 0;
    private int syncTimer = 0;
    private int targetCount = 0;
    private int completedCount = 0;
    private boolean batchActive = false;
    private boolean infiniteMode = false;
    private int activeRecipeIndex = -1;

    // 网络与明细数据
    private final Map<BlockPos, Double> connectedCollectors = new HashMap<>();
    private final Map<BlockPos, Long> lastPullDetails = new HashMap<>();
    private final Map<BlockPos, ManaType> lastPullTypes = new HashMap<>();

    public SpiritCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SPIRIT_CORE.get(), pos, state);
        this.totalEfficiency = 1.0; // 核心自身效率必定是 100%
    }

    // ==================== 查询与基础逻辑 ====================

    public long getTotalMana() {
        long s = 0;
        for (long v : mana.values()) s += v;
        return s;
    }

    public long getTotalCapacity() {
        return BASE_CAPACITY + getCrystalCapacity() + capacityBonus;
    }

    public long getRemainingCapacity() {
        return Math.max(0L, getTotalCapacity() - getTotalMana());
    }

    public long getCrystalCapacity() {
        if (crystal.isEmpty() || !(crystal.getItem() instanceof ManaCrystalItem item)) return 0L;
        return item.getCapacity();
    }

    public long getMana(ManaType type) { return mana.getOrDefault(type, 0L); }

    public void consumeMana(ManaType type, long amount) {
        long cur = mana.getOrDefault(type, 0L);
        long next = Math.max(0L, cur - amount);
        if (next <= 0) mana.remove(type);
        else mana.put(type, next);
        setChanged();
    }
    public long getCapacityBonus() {
        return capacityBonus;
    }

    public Map<BlockPos, Double> getConnectedCollectors() { return connectedCollectors; }
    public Map<BlockPos, Long> getLastPullDetails() { return lastPullDetails; }
    public Map<BlockPos, ManaType> getLastPullTypes() { return lastPullTypes; }

    public void registerCollector(BlockPos pos, double efficiency) {
        connectedCollectors.put(pos, efficiency);
        setChanged();
    }

    public void unregisterCollector(BlockPos pos) {
        connectedCollectors.remove(pos);
        setChanged();
    }

    // ==================== Tick 逻辑 ====================

    public static void serverTick(Level level, BlockPos pos, BlockState state, SpiritCoreBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel)) return;

        be.tickTimer++;
        if (be.tickTimer >= COLLECT_INTERVAL) {
            be.tickTimer = 0;
            be.recalculateCapacity(level, pos);

            be.lastPullDetails.clear();
            be.lastPullTypes.clear();
            be.pullMana();
        }

        be.tickCrafting();

        be.syncTimer++;
        if (be.syncTimer >= SYNC_INTERVAL) {
            be.syncTimer = 0;
            serverLevel.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        }
        be.setChanged();
    }

    /**
     * 核心抽水逻辑：直接遍历名单，不再递归。
     */
    public void pullMana() {
        Level level = this.level;
        if (level == null || level.isClientSide) return;

        long remainingCap = getRemainingCapacity();
        if (remainingCap <= 0) return;

        for (Map.Entry<BlockPos, Double> entry : new HashMap<>(connectedCollectors).entrySet()) {
            if (remainingCap <= 0) break;

            BlockPos collectorPos = entry.getKey();
            double eff = entry.getValue();

            long received = extractFromCollector(level, collectorPos, remainingCap, eff);
            remainingCap -= received;
        }
    }

    /**
     * 从单个聚集器抽水（兼容实时和离线影子数据）
     */
    private long extractFromCollector(Level level, BlockPos pos, long maxPull, double eff) {
        ManaType manaType = ManaType.NONE;
        long pulled = 0;

        if (level.isLoaded(pos)) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof SpiritCollectorBlockEntity collector) {
                // 实时抽取
                manaType = collector.getCurrentType();
                long available = collector.getMana(manaType);
                pulled = Math.min(maxPull, available);
                collector.consumeMana(manaType, pulled);

                // 同步影子数据
                OfflineCollectorData shadow = new OfflineCollectorData(
                        collector.getTotalMana(), collector.getCurrentType(),
                        level.getGameTime(), collector.getSpeed(), collector.getCapacity());
                NetworkManager.updateShadowData(pos, shadow);
            }
        } else {
            // 离线抽取
            OfflineCollectorData shadow = NetworkManager.getShadowData(pos);
            if (shadow != null) {
                long currentTick = level.getGameTime();
                shadow.mana += shadow.calculateOfflineGain(currentTick);
                shadow.mana = Math.min(shadow.mana, shadow.capacity);

                pulled = Math.min(maxPull, shadow.mana);
                shadow.mana -= pulled;
                shadow.lastUpdateTick = currentTick;
                manaType = shadow.type;

                NetworkManager.updateShadowData(pos, shadow);
            }
        }

        if (pulled > 0) {
            lastPullDetails.put(pos, pulled);
            lastPullTypes.put(pos, manaType);
        }

        // 实际入库 = 抽取量 * 效率
        long actualReceived = (long) (pulled * eff);
        if (actualReceived > 0) {
            this.mana.merge(manaType, actualReceived, Long::sum);
        }

        return actualReceived;
    }

    // ==================== 网络与方块逻辑 ====================

    @Override
    public NodeType getNodeType() { return NodeType.CORE; }

    @Override
    public int getMaxChildren() { return 8; }

    @Override
    public void findParent() {
        // 核心是根节点，永远不找父节点
    }
    /**
     * 当子节点（中继器或聚集器）断开时，核心需要同步清理名单
     */
    @Override
    public void onChildDisconnected(BlockPos childPos) {
        // 从直连子节点中移除
        children.remove(childPos);
        // 从聚集器抽水名单中移除
        connectedCollectors.remove(childPos);
        setChanged();
    }
    public void rebuildNetwork() {
        if (level == null || level.isClientSide) return;

        // 断开所有直连子节点
        disconnectChildren();
        connectedCollectors.clear();

        // 强制所有中继器和聚集器重新找父节点
        List<BlockPos> allNodes = new ArrayList<>(NetworkManager.getNodesInLevel(level).keySet());
        // 先让中继器找父节点
        for (BlockPos pos : allNodes) {
            if (level.getBlockEntity(pos) instanceof AbstractNetworkNodeBlockEntity node && node.getNodeType() != NodeType.CORE) {
                node.findParent();
            }
        }
        setChanged();
    }

    /** BFS 计算容量加成 */
    private void recalculateCapacity(Level level, BlockPos pos) {
        long bonus = 0L;
        Set<BlockPos> visited = new HashSet<>();
        visited.add(pos);
        List<BlockPos> frontier = new ArrayList<>();
        frontier.add(pos);

        for (int depth = 1; depth <= CAPACITY_BFS_DEPTH; depth++) {
            double weight = Math.pow(CAPACITY_DECAY, depth - 1);
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

    // ==================== 容器与水晶 ====================

    public ItemStack getCrystal() { return crystal; }
    public void setCrystal(ItemStack stack) {
        this.crystal = (stack == null || stack.isEmpty()) ? ItemStack.EMPTY : stack;
        setChanged();
    }

    public void insertCrystal(ItemStack newCrystal, Player player) {
        ItemStack old = this.crystal;
        if (old.isEmpty()) {
            this.crystal = newCrystal.copy();
            if (!player.isCreative()) newCrystal.shrink(1);
        } else {
            ItemStack oldOut = extractCrystalWithMana();
            this.crystal = newCrystal.copy();
            if (!player.isCreative()) newCrystal.shrink(1);
            if (!player.getInventory().add(oldOut)) player.drop(oldOut, false);
        }
        setChanged();
    }

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

    // ==================== 合成与容器 ====================

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

    // ==================== 按钮交互 ====================
    public void setSelectedIndex(int i) {
        this.selectedIndex = i;
        if (!batchActive) {
            this.targetCount = 0;
            this.completedCount = 0;
            this.progress = 0;
            this.infiniteMode = false;
            this.activeRecipeIndex = -1;
        }
        setChanged();
    }

    public void adjustTarget(int action) {
        if (batchActive || selectedIndex < 0) return;
        if (activeRecipeIndex != selectedIndex) {
            completedCount = 0;
            progress = 0;
        }

        switch (action) {
            case BTN_MIN -> { infiniteMode = false; targetCount = Math.max(1, completedCount); }
            case BTN_MAX -> { infiniteMode = true; targetCount = 0; }
            case BTN_PLUS1 -> { if (!infiniteMode) targetCount = Math.max(1, targetCount + 1); }
            case BTN_PLUS5 -> { if (!infiniteMode) targetCount = Math.max(1, targetCount + 5); }
            case BTN_MINUS1 -> { if (!infiniteMode) targetCount = Math.max(1, targetCount - 5); }
            case BTN_MINUS5 -> { if (!infiniteMode) targetCount = Math.max(1, targetCount - 5); }
            case BTN_REBUILD_NETWORK -> { rebuildNetwork(); return; }
            default -> { return; }
        }
        setChanged();
    }

    public void startCrafting() {
        if (selectedIndex < 0 || batchActive || getRecipe() == null) return;
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
            targetCount = completedCount;
        } else {
            infiniteMode = false;
            targetCount = completedCount;
        }
        setChanged();
    }

    public ManaCraftingRecipe getRecipe() { return selectedIndex < 0 ? null : ManaCraftingRecipes.get(selectedIndex); }
    public int getSelectedIndex() { return selectedIndex; }
    public int getProgress() { return progress; }
    public int getTargetCount() { return targetCount; }
    public int getCompletedCount() { return completedCount; }
    public boolean isBatchActive() { return batchActive; }
    public int getActiveRecipeIndex() { return activeRecipeIndex; }
    public boolean isInfiniteMode() { return infiniteMode; }

    // ==================== 容器接口实现 ====================
    @Override public int getContainerSize() { return SLOT_COUNT; }
    @Override public boolean isEmpty() { for (ItemStack s : items) if (!s.isEmpty()) return false; return true; }
    @Override public ItemStack getItem(int slot) { return items.get(slot); }
    @Override public ItemStack removeItem(int slot, int amount) {
        ItemStack s = items.get(slot);
        if (s.isEmpty()) return ItemStack.EMPTY;
        ItemStack out = s.split(amount);
        if (!s.isEmpty()) setChanged();
        return out;
    }
    @Override public ItemStack removeItemNoUpdate(int slot) { ItemStack s = items.get(slot); items.set(slot, ItemStack.EMPTY); return s; }
    @Override public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > stack.getMaxStackSize()) stack.setCount(stack.getMaxStackSize());
        setChanged();
    }
    @Override public boolean stillValid(Player p) { return p.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5) <= 64.0; }
    @Override public void clearContent() { items.clear(); }

    public void dropContents(Level level, BlockPos pos) {
        for (ItemStack s : items) {
            if (!s.isEmpty()) Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, s);
        }
    }

    // ==================== 序列化 ====================
    @Override public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
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
            if (itemsTag.contains("s" + i)) items.set(i, ItemStack.parseOptional(registries, itemsTag.getCompound("s" + i)));
        }

        // 反序列化网络名单
        connectedCollectors.clear();
        ListTag connectedList = tag.getList("ConnectedCollectors", Tag.TAG_COMPOUND);
        for (int i = 0; i < connectedList.size(); i++) {
            CompoundTag entry = connectedList.getCompound(i);
            connectedCollectors.put(
                    BlockPos.of(entry.getLong("Pos")),
                    entry.getDouble("Eff")
            );
        }
    }

    @Override public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        CompoundTag m = new CompoundTag();
        for (Map.Entry<ManaType, Long> e : mana.entrySet()) {
            if (e.getValue() > 0) m.putLong(e.getKey().id(), e.getValue());
        }
        tag.put("Mana", m);
        tag.putInt("SelectedIndex", selectedIndex);
        tag.putInt("Progress", progress);
        tag.putInt("ActiveRecipeIndex", activeRecipeIndex);
        tag.putInt("TargetCount", targetCount);
        tag.putInt("CompletedCount", completedCount);
        tag.putBoolean("BatchActive", batchActive);
        tag.putBoolean("InfiniteMode", infiniteMode);
        if (!crystal.isEmpty()) tag.put("Crystal", crystal.save(registries));

        CompoundTag itemsTag = new CompoundTag();
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (!items.get(i).isEmpty()) itemsTag.put("s" + i, items.get(i).save(registries));
        }
        tag.put("Items", itemsTag);

        // 序列化网络名单
        ListTag connectedList = new ListTag();
        for (Map.Entry<BlockPos, Double> entry : connectedCollectors.entrySet()) {
            CompoundTag e = new CompoundTag();
            e.putLong("Pos", entry.getKey().asLong());
            e.putDouble("Eff", entry.getValue());
            connectedList.add(e);
        }
        tag.put("ConnectedCollectors", connectedList);
    }

    @Override public CompoundTag getUpdateTag(HolderLookup.Provider reg) {
        CompoundTag tag = new CompoundTag();
        CompoundTag m = new CompoundTag();
        for (Map.Entry<ManaType, Long> e : mana.entrySet()) {
            if (e.getValue() > 0) m.putLong(e.getKey().id(), e.getValue());
        }
        tag.put("Mana", m);
        tag.putLong("CapacityBonus", capacityBonus);

        // 同步 UI 需要的明细列表给客户端
        ListTag detailList = new ListTag();
        for (Map.Entry<BlockPos, Long> entry : lastPullDetails.entrySet()) {
            CompoundTag d = new CompoundTag();
            d.putLong("Pos", entry.getKey().asLong());
            d.putLong("Amount", entry.getValue());
            ManaType type = lastPullTypes.getOrDefault(entry.getKey(), ManaType.NONE);
            d.putString("Type", type.id());
            detailList.add(d);
        }
        tag.put("PullDetails", detailList);
        return tag;
    }

    @Override public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // ==================== HUD ====================
    @Override
    public List<SpiritInfoLine> getSpiritInfo() {
        List<SpiritInfoLine> list = new ArrayList<>();
        list.add(new SpiritInfoLine(
                Component.translatable("block.spiritworld.spirit_core").withStyle(ChatFormatting.GOLD), 1));
        list.add(new SpiritInfoLine(
                Component.translatable("gui.spiritworld.spirit_core.capacity", getTotalMana(), getTotalCapacity())
                        .withStyle(ChatFormatting.AQUA), 2));
        list.add(new SpiritInfoLine(
                Component.translatable("gui.spiritworld.spirit_core.bonus", getCapacityBonus())
                        .withStyle(ChatFormatting.YELLOW), 3));
        for (ManaType type : ManaType.values()) {
            long amt = getMana(type);
            if (amt <= 0) continue;
            list.add(new SpiritInfoLine(type.applyStyle(Component.translatable("gui.spiritworld.spirit_core.mana_entry", type.displayName(), amt)), 3));
        }
        return list;
    }
}
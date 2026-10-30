package com.varlike.spiritworld.client;

import com.varlike.spiritworld.block.SpiritCoreBlockEntity;
import com.varlike.spiritworld.registry.ModMenus;
import com.varlike.spiritworld.skill.ManaCraftingRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SpiritCoreMenu extends AbstractContainerMenu {

    public static final int IDX_PROGRESS = 0;
    public static final int IDX_TOTAL    = 1;
    public static final int IDX_SELECTED = 2;
    public static final int IDX_TARGET     = 3;
    public static final int IDX_COMPLETED  = 4;
    public static final int IDX_BATCH      = 5;
    public static final int IDX_ACTIVE_RECIPE = 6;
    public static final int IDX_INFINITE      = 7;
    public static final int DATA_SIZE = 8;

    private final Container container;
    private final ContainerData data;
    private final BlockPos blockPos;

    public BlockPos getBlockPos() {return blockPos;}
    public int getActiveRecipeIndex() { return data.get(IDX_ACTIVE_RECIPE); }
    public boolean isInfiniteMode()   { return data.get(IDX_INFINITE) != 0; }

    /** 服务端构造 */
    public SpiritCoreMenu(int id, Inventory inv, SpiritCoreBlockEntity be) {
        this(id, inv, be, new ContainerData() {
            @Override public int get(int i) {
                return switch (i) {
                    case IDX_PROGRESS  -> be.getProgress();
                    case IDX_TOTAL     -> {
                        var r = be.getRecipe();
                        yield r == null ? 0 : r.craftingTicks();
                    }
                    case IDX_TARGET    -> be.getTargetCount();
                    case IDX_COMPLETED -> be.getCompletedCount();
                    case IDX_BATCH     -> be.isBatchActive() ? 1 : 0;
                    case IDX_SELECTED  -> be.getSelectedIndex();
                    case IDX_ACTIVE_RECIPE -> be.getActiveRecipeIndex();
                    case IDX_INFINITE  -> be.isInfiniteMode() ? 1 : 0;
                    default -> 0;
                };
            }
            @Override public void set(int i, int v) {}
            @Override public int getCount() { return DATA_SIZE; }
        }, be.getBlockPos());
    }

    /** 客户端构造 */
    public SpiritCoreMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, new SimpleContainer(SpiritCoreBlockEntity.SLOT_COUNT), new SimpleContainerData(DATA_SIZE), buf.readBlockPos());
    }

    private SpiritCoreMenu(int id, Inventory inv, Container container, ContainerData data,BlockPos pos) {
        super(ModMenus.SPIRIT_CORE.get(), id);
        this.container = container;
        this.data = data;
        this.blockPos = pos;

        // 3×3 输出容器（右侧）
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 3; col++)
                addSlot(new Slot(container, row * 3 + col, 181 + col * 18, 21 + row * 18));

        // 玩家背包 9×3
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                addSlot(new Slot(inv, col + row * 9 + 9, 60 + col * 18, 215 + row * 18));
        // 快捷栏
        for (int col = 0; col < 9; col++)
            addSlot(new Slot(inv, col, 60 + col * 18, 273));

        addDataSlots(data);
    }

    @Override
    public boolean clickMenuButton(Player player, int index) {
        if (!(container instanceof SpiritCoreBlockEntity be)) return false;

        if (index >= 0 && index < ManaCraftingRecipes.size()) {
            be.setSelectedIndex(index);
            return true;
        }

        switch (index) {
            case SpiritCoreBlockEntity.BTN_MIN    -> { be.adjustTarget(Integer.MIN_VALUE); return true; }
            case SpiritCoreBlockEntity.BTN_MINUS5 -> { be.adjustTarget(-5); return true; }
            case SpiritCoreBlockEntity.BTN_MINUS1 -> { be.adjustTarget(-1); return true; }
            case SpiritCoreBlockEntity.BTN_PLUS1  -> { be.adjustTarget(+1); return true; }
            case SpiritCoreBlockEntity.BTN_PLUS5  -> { be.adjustTarget(+5); return true; }
            case SpiritCoreBlockEntity.BTN_MAX    -> { be.adjustTarget(Integer.MAX_VALUE); return true; }
            case SpiritCoreBlockEntity.BTN_START  -> { be.startCrafting(); return true; }
            case SpiritCoreBlockEntity.BTN_CANCEL -> { be.cancelCrafting(); return true; }
        }
        return false;
    }

    public int getProgress()      { return data.get(IDX_PROGRESS); }
    public int getTotalTicks()    { return data.get(IDX_TOTAL); }
    public int getSelectedIndex() { return data.get(IDX_SELECTED); }
    public int getTargetCount()    { return data.get(IDX_TARGET); }
    public int getCompletedCount() { return data.get(IDX_COMPLETED); }
    public boolean isBatchActive() { return data.get(IDX_BATCH) != 0; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (index < 9) {
            if (!moveItemStackTo(stack, 9, slots.size(), true)) return ItemStack.EMPTY;
        } else {
            if (!moveItemStackTo(stack, 0, 9, false)) return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }
}
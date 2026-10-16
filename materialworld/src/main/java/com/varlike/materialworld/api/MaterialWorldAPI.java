package com.varlike.materialworld.api;

import com.varlike.materialworld.MaterialWorld;
import com.varlike.materialworld.capability.ModCapabilities;
import com.varlike.materialworld.capability.PlayerEMC;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * MaterialWorld 公开 API 主入口。
 * Public API entry point for MaterialWorld.
 *
 * <p>附属 mod 只需依赖本类即可访问 EMC 系统，无需引用内部实现类。</p>
 * <p>Addon mods only need to depend on this class to access the EMC system,
 * without referencing internal implementation classes.</p>
 */
public final class MaterialWorldAPI {

    /** 模组 ID / Mod ID */
    public static final String MOD_ID = MaterialWorld.MOD_ID;

    /** API 版本，附属 mod 可据此判断兼容性 / API version for compatibility check */
    public static final int API_VERSION = 2;

    private MaterialWorldAPI() {}

    // ==================== 物品 EMC 查询与设置 / Item EMC Query & Set ====================

    /** 获取物品的基础 EMC，不考虑数量与耐久。未定义时返回 0。 */
    public static long getEMC(Item item) {
        return EMCRegistry.getEMC(item);
    }

    // ===== 耐久折算 =====

    /** 单个物品当前有效 EMC（按剩余耐久折算）。 */
    public static long getCurrentEMC(ItemStack stack) {
        return EMCRegistry.getCurrentEMC(stack);
    }

    /** 整栈价值：有耐久按折算 × count，无耐久按基础 × count。 */
    public static long getStackEMC(ItemStack stack) {
        return EMCRegistry.getStackEMC(stack);
    }

    /**
     * 设置或覆盖物品的 EMC 值，负值会被归零。
     * Set or override an item's EMC value, negative values are clamped to 0.
     */
    public static void setEMC(Item item, long value) {
        EMCRegistry.setEMC(item, value);
    }

    /**
     * 判断物品是否已定义 EMC 值。
     * Check whether an item has a defined EMC value.
     */
    public static boolean hasEMC(Item item) {
        return EMCRegistry.hasEMC(item);
    }

    /**
     * 通过注册名获取物品的 EMC 值，物品不存在时返回 0。
     * Get EMC by registry name, returns 0 if item not found.
     */
    public static long getEMC(ResourceLocation itemId) {
        return EMCRegistry.getEMC(itemId);
    }

    // ==================== 玩家数据 / Player Data ====================

    /**
     * 获取玩家的 EMC 数据接口，玩家未初始化时返回 null。
     * Get the player's EMC data interface, returns null if not initialized.
     */
    @Nullable
    public static IPlayerEMC getPlayerEMC(Player player) {
        PlayerEMC data = player.getCapability(ModCapabilities.PLAYER_EMC);
        return data == null ? null : new IPlayerEMCImpl(data);
    }

    // ==================== 内部实现适配 / Internal Implementation Adapter ====================

    /**
     * IPlayerEMC 的内部实现，仅由 API 包使用。
     * Internal implementation of IPlayerEMC, only used by the API package.
     */
    private record IPlayerEMCImpl(PlayerEMC delegate) implements IPlayerEMC {

        @Override
        public long getBalance() {
            return delegate.getEmcBalance();
        }

        @Override
        public void setBalance(long value) {
            delegate.setEmcBalance(value);
        }

        @Override
        public void addBalance(long amount) {
            delegate.addEmc(amount);
        }

        @Override
        public boolean consumeBalance(long amount) {
            return delegate.consumeEmc(amount);
        }

        @Override
        public boolean isLearned(Item item) {
            return delegate.isLearned(item);
        }

        @Override
        public boolean learn(Item item) {
            return delegate.learn(item);
        }

        @Override
        public java.util.Set<ResourceLocation> getLearnedItems() {
            return delegate.getLearnedItems();
        }
    }
    /**
     * 向物质世界的附属创造标签添加一个物品。
     * 必须在附属 mod 的构造函数中调用（RegisterEvent 之前）。
     *
     * <p>如果没有任何附属 mod 调用此方法，该标签不会被注册，玩家界面上也看不到它。</p>
     *
     * @param stackSupplier 物品栈供应器，建议使用 {@code () -> new ItemStack(YOUR_ITEM.get())}
     */
    public static void addToAddonTab(Supplier<ItemStack> stackSupplier) {
        com.varlike.materialworld.registry.AddonCreativeTab.add(stackSupplier);
    }
    /**
     * 向物质世界的附属创造标签添加一个带分组的物品。
     * Add an item with a group to MaterialWorld's addon creative tab.
     *
     * <p>同一 groupName 的物品会连续排列，并在组前显示一个标题锚点。</p>
     * <p>Items with the same groupName are placed consecutively, with a header
     * anchor inserted before the group.</p>
     *
     * <p>必须在附属 mod 的构造函数中调用（在 RegisterEvent 之前）。</p>
     *
     * @param groupName     分组名。含 "." 时视为翻译键，否则视为字面文本。
     *                      Group name. Treated as a translation key if it contains ".",
     *                      otherwise as a literal string.
     * @param stackSupplier 物品栈供应器 / Item stack supplier
     */
    public static void addToAddonTab(String groupName, Supplier<ItemStack> stackSupplier) {
        com.varlike.materialworld.registry.AddonCreativeTab.add(groupName, stackSupplier);
    }
}
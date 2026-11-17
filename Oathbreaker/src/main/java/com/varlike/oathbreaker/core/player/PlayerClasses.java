package com.varlike.oathbreaker.core.player;

import com.varlike.oathbreaker.Oathbreaker;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * 玩家职业数据的 Attachment 注册与访问入口。
 *
 * <p>先用 Player Attachment 存“当前职业 + 职业标签”，因为它是局内状态、且需要跟随玩家重生。
 * 完整账户层（解锁职业、永久点数、契约、残片）后续单独放 SavedData，两者不要混用。</p>
 */
public final class PlayerClasses {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Oathbreaker.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerClassData>> CLASS_DATA =
            ATTACHMENT_TYPES.register("player_class", () -> AttachmentType
                    .builder(() -> PlayerClassData.EMPTY)
                    .serialize(PlayerClassData.CODEC)
                    .copyOnDeath()
                    .build());

    private PlayerClasses() {}

    public static PlayerClassData get(Player player) {
        return player.getData(CLASS_DATA);
    }

    public static void set(Player player, PlayerClassData data) {
        player.setData(CLASS_DATA, data);
    }
}

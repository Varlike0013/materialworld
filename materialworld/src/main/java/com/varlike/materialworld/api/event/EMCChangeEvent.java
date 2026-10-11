package com.varlike.materialworld.api.event;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;

/**
 * 玩家 EMC 余额变化事件，在游戏事件总线上发布。
 * Player EMC balance change event, posted on the game event bus.
 */
public class EMCChangeEvent extends Event {

    private final Player player;
    private final long oldBalance;
    private final long newBalance;

    public EMCChangeEvent(Player player, long oldBalance, long newBalance) {
        this.player = player;
        this.oldBalance = oldBalance;
        this.newBalance = newBalance;
    }

    public Player getPlayer() { return player; }
    public long getOldBalance() { return oldBalance; }
    public long getNewBalance() { return newBalance; }
    public long getDelta() { return newBalance - oldBalance; }
}
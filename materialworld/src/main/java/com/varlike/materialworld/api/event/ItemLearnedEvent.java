package com.varlike.materialworld.api.event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.Event;

/**
 * 玩家首次学习某物品时发布的事件。
 * Event posted when a player learns an item for the first time.
 */
public class ItemLearnedEvent extends Event {

    private final Player player;
    private final Item item;

    public ItemLearnedEvent(Player player, Item item) {
        this.player = player;
        this.item = item;
    }

    public Player getPlayer() { return player; }
    public Item getItem() { return item; }
}
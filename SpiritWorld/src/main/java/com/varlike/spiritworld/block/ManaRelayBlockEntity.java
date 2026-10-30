package com.varlike.spiritworld.block;

import com.varlike.spiritworld.api.SpiritInfoHolder;
import com.varlike.spiritworld.api.SpiritInfoLine;
import com.varlike.spiritworld.network.AbstractNetworkNodeBlockEntity;
import com.varlike.spiritworld.network.NodeType;
import com.varlike.spiritworld.registry.ModBlockEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class ManaRelayBlockEntity extends AbstractNetworkNodeBlockEntity implements SpiritInfoHolder {

    public ManaRelayBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MANA_RELAY.get(), pos, state);
    }

    @Override
    public NodeType getNodeType() {
        if (getBlockState().getBlock() instanceof ManaRelayBlock relayBlock) {
            return switch (relayBlock.getTier()) {
                case 1 -> NodeType.RELAY_1;
                case 2 -> NodeType.RELAY_2;
                case 3 -> NodeType.RELAY_3;
                default -> NodeType.RELAY_1;
            };
        }
        return NodeType.RELAY_1;
    }

    @Override
    public int getMaxChildren() {
        return switch (getNodeType()) {
            case RELAY_1 -> 4;
            case RELAY_2 -> 8;
            case RELAY_3 -> 16;
            default -> 4;
        };
    }
    /**
     * 默认的子节点断开处理：从子节点列表中移除
     * 如果有特殊需求（比如核心需要清理名单），子类可以重写。
     */
    @Override
    public void onChildDisconnected(BlockPos childPos) {
        this.removeChild(childPos);
        this.setChanged();
    }

    @Override
    public List<SpiritInfoLine> getSpiritInfo() {
        List<SpiritInfoLine> list = new ArrayList<>();
        NodeType type = getNodeType();
        String nameKey = switch (type) {
            case RELAY_1 -> "block.spiritworld.mana_relay_1";
            case RELAY_2 -> "block.spiritworld.mana_relay_2";
            case RELAY_3 -> "block.spiritworld.mana_relay_3";
            default -> "block.spiritworld.mana_relay_1";
        };

        list.add(new SpiritInfoLine(
                Component.translatable(nameKey).withStyle(ChatFormatting.GOLD), 1));
        list.add(new SpiritInfoLine(
                Component.translatable("gui.spiritworld.relay.efficiency", String.format("%.1f", totalEfficiency * 100))
                        .withStyle(ChatFormatting.AQUA), 2));
        list.add(new SpiritInfoLine(
                Component.translatable("gui.spiritworld.relay.children", children.size(), getMaxChildren())
                        .withStyle(ChatFormatting.YELLOW), 3));

        if (parentPos == null) {
            list.add(new SpiritInfoLine(
                    Component.translatable("gui.spiritworld.relay.disconnected")
                            .withStyle(ChatFormatting.RED), 3));
        }

        return list;
    }
}
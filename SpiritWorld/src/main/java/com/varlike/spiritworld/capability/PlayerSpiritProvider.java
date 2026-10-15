package com.varlike.spiritworld.capability;

import com.varlike.spiritworld.Config;
import com.varlike.spiritworld.skill.ModSkills;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerSpiritProvider
        implements ICapabilityProvider<Entity, Void, PlayerSpirit> {

    private final Map<UUID, PlayerSpirit> dataMap = new ConcurrentHashMap<>();

    @Override
    public PlayerSpirit getCapability(Entity entity, Void context) {
        return dataMap.computeIfAbsent(entity.getUUID(), uuid -> {
            PlayerSpirit data = new PlayerSpirit();
            long defaultMax = Config.defaultMaxSpirit > 0
                    ? Config.defaultMaxSpirit
                    : PlayerSpirit.DEFAULT_MAX_SPIRIT;

            // 上限按配置初始化，但当前精神力从 0 开始
            data.setMaxSpirit(defaultMax);
            data.setSpirit(0L);
            // 默认拥有 EMC 转化技能 lv1，并装配到 0 号槽位
            data.setSkillLevel(ModSkills.EMC_CONVERSION_ID, 1);
            data.setLoadoutSlot(0, ModSkills.EMC_CONVERSION_ID);

            data.markInitialized();
            data.clearDirty();
            return data;
        });
    }

    public void remove(UUID uuid) {
        dataMap.remove(uuid);
    }
}
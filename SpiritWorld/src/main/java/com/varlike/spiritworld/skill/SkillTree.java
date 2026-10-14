package com.varlike.spiritworld.skill;

import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class SkillTree {

    private static final Map<ResourceLocation, SkillNode> NODES = new LinkedHashMap<>();

    public static void register(SkillNode node) {
        NODES.put(node.getId(), node);
    }

    public static SkillNode get(ResourceLocation id) {
        return NODES.get(id);
    }

    public static boolean exists(ResourceLocation id) {
        return NODES.containsKey(id);
    }

    public static Collection<SkillNode> getAll() {
        return Collections.unmodifiableCollection(NODES.values());
    }

    public static void clear() {
        NODES.clear();
    }
}
package com.varlike.oathbreaker.core.weapon;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.varlike.oathbreaker.Oathbreaker;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

/**
 * 扫描 {@code data/<ns>/oathbreaker/weapons/*.json}，加载 JSON 武器池条目。
 *
 * <p>JSON 不包含 profile/命中效果，只增加池成员、权重、池标签；
 * 因此不会改变武器原有伤害或行为。</p>
 */
public final class WeaponPoolReloadListener extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().create();

    public WeaponPoolReloadListener() {
        super(GSON, "oathbreaker/weapons");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager resourceManager,
                         ProfilerFiller profiler) {
        List<WeaponDefinition> definitions = new ArrayList<>();
        files.forEach((id, json) -> WeaponDefinition.JSON_CODEC.parse(JsonOps.INSTANCE, json)
                .resultOrPartial(error -> Oathbreaker.LOGGER.error(
                        "Failed to parse weapon pool entry {}: {}", id, error))
                .ifPresent(definition -> definitions.add(definition.withId(id))));
        WeaponPool.acceptJson(definitions);
        Oathbreaker.LOGGER.info("Loaded {} weapon pool entries from JSON", definitions.size());
    }
}

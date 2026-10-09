package com.varlike.materialworld.emc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * EMC 持久化：把 EMCManager 中的映射读写到 config/materialworld/emc.json
 */
public class EMCStorage {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path filePath;

    /** 初始化路径，在模组加载时调用一次 */
    public static void init(Path configDir) {
        Path dir = configDir.resolve("materialworld");
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        filePath = dir.resolve("emc.json");
    }

    /** 从文件加载自定义 EMC 值（覆盖默认值） */
    public static void load() {
        if (filePath == null || !Files.exists(filePath)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(filePath)) {
            JsonElement root = JsonParser.parseReader(reader);
            if (!root.isJsonObject()) return;

            JsonObject obj = root.getAsJsonObject();
            int count = 0;
            for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
                try {
                    long value = entry.getValue().getAsLong();
                    EMCManager.setEMC(entry.getKey(), value);
                    count++;
                } catch (Exception e) {
                    // 单条数据损坏时跳过，不影响其他加载
                    System.err.println("[物质世界] 无法解析 EMC 条目: " + entry.getKey());
                }
            }
            System.out.println("[物质世界] 已从文件加载 " + count + " 条 EMC 数据");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** 把当前 EMCManager 里的所有值写回文件 */
    public static void save() {
        if (filePath == null) return;
        try (Writer writer = Files.newBufferedWriter(filePath)) {
            JsonObject obj = new JsonObject();
            EMCManager.forEach((item, value) -> {
                ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
                if (key != null) {
                    obj.addProperty(key.toString(), value);
                }
            });
            GSON.toJson(obj, writer);
            System.out.println("[物质世界] EMC 数据已保存到 " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
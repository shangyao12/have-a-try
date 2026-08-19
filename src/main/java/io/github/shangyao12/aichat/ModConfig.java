package io.github.shangyao12.aichat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 模组配置管理。
 * 配置文件位置：config/aichatbuddy.json
 */
public class ModConfig {

    private static final String CONFIG_FILE_NAME = "aichatbuddy.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    // 配置项
    public String apiKey = "sk-your-api-key-here";
    public String baseUrl = "https://api.deepseek.com/v1";
    public String model = "deepseek-chat";
    public String systemPrompt = "你是一个住在 Minecraft 世界里的友好 AI 助手。你了解 Minecraft 的各种知识，包括合成配方、生存技巧、红石电路等。请用简洁、有趣的方式回答玩家的问题。";
    public String triggerPrefix = "!ai";

    /**
     * 加载配置。如果配置文件不存在，会创建一个默认配置。
     */
    public static ModConfig load() {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE_NAME);

        if (!Files.exists(configPath)) {
            ModConfig defaultConfig = new ModConfig();
            defaultConfig.save();
            AiChatMod.LOGGER.info("已创建默认配置文件: {}", configPath);
            return defaultConfig;
        }

        try {
            String content = Files.readString(configPath);
            ModConfig config = GSON.fromJson(content, ModConfig.class);
            if (config == null) {
                config = new ModConfig();
            }
            return config;
        } catch (IOException e) {
            AiChatMod.LOGGER.error("读取配置文件失败，使用默认配置", e);
            return new ModConfig();
        }
    }

    /**
     * 保存配置到文件。
     */
    public void save() {
        Path configPath = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE_NAME);
        try {
            Files.createDirectories(configPath.getParent());
            Files.writeString(configPath, GSON.toJson(this));
        } catch (IOException e) {
            AiChatMod.LOGGER.error("保存配置文件失败", e);
        }
    }
}

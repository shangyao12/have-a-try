package io.github.shangyao12.aichat;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AI Chat Buddy 模组主类。
 * 玩家在聊天框输入 "!ai 你好"，AI 就会回复。
 */
public class AiChatMod implements ModInitializer {

    public static final String MOD_ID = "aichatbuddy";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static ModConfig config;
    private static LlmClient llmClient;

    @Override
    public void onInitialize() {
        LOGGER.info("AI Chat Buddy 正在初始化...");

        // 加载配置
        config = ModConfig.load();

        // 初始化 LLM 客户端
        llmClient = new LlmClient(
                config.baseUrl,
                config.apiKey,
                config.model,
                config.systemPrompt
        );

        // 注册聊天消息监听器
        ServerMessageEvents.CHAT_MESSAGE.register(this::onChatMessage);

        LOGGER.info("AI Chat Buddy 初始化完成！触发前缀: {}", config.triggerPrefix);
    }

    /**
     * 处理玩家聊天消息。
     */
    private void onChatMessage(SignedMessage message, ServerPlayerEntity sender, MessageType.Parameters params) {
        String content = message.getContent().getString();
        String prefix = config.triggerPrefix + " ";

        // 检查是否以触发前缀开头
        if (!content.startsWith(prefix)) {
            return;
        }

        // 提取用户实际输入的内容
        String userInput = content.substring(prefix.length()).trim();
        if (userInput.isEmpty()) {
            return;
        }

        MinecraftServer server = sender.getServer();
        if (server == null) {
            return;
        }

        // 特殊命令：清空对话历史
        if (userInput.equalsIgnoreCase("clear") || userInput.equalsIgnoreCase("重置")) {
            llmClient.clearHistory();
            sendServerMessage(server, "§b[AI]§r 对话历史已清空，我们重新开始吧！");
            return;
        }

        // 提示玩家 AI 正在思考
        sendServerMessage(server, "§7[AI] 正在思考...");

        // 在异步线程中调用 LLM，避免阻塞服务器主线程
        new Thread(() -> {
            String reply = llmClient.chat(userInput);
            // 回到服务器主线程发送消息
            server.execute(() -> {
                // AI 的回复可能很长，按行拆分发送
                String[] lines = reply.split("\n");
                for (String line : lines) {
                    if (!line.trim().isEmpty()) {
                        sendServerMessage(server, "§b[AI]§r " + line);
                    }
                }
            });
        }, "AI-Chat-Thread").start();
    }

    /**
     * 向服务器所有玩家发送一条系统消息。
     */
    private void sendServerMessage(MinecraftServer server, String text) {
        server.getPlayerManager().broadcast(Text.literal(text), false);
    }
}

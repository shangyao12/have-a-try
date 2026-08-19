package io.github.shangyao12.aichat;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * LLM 客户端：调用 OpenAI 兼容的 Chat Completions API。
 * 支持 DeepSeek、OpenAI、本地 Ollama 等任何兼容接口。
 */
public class LlmClient {

    private final HttpClient httpClient;
    private final Gson gson;
    private final String baseUrl;
    private final String apiKey;
    private final String model;
    private final String systemPrompt;
    private final List<JsonObject> conversationHistory;

    public LlmClient(String baseUrl, String apiKey, String model, String systemPrompt) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.gson = new Gson();
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        this.apiKey = apiKey;
        this.model = model;
        this.systemPrompt = systemPrompt;
        this.conversationHistory = new ArrayList<>();
    }

    /**
     * 发送一条用户消息，返回 AI 的回复。
     * 会保留对话历史，让 AI 有上下文记忆。
     */
    public String chat(String userMessage) {
        // 构建消息列表：系统提示 + 历史对话 + 当前消息
        JsonArray messages = new JsonArray();

        JsonObject systemMsg = new JsonObject();
        systemMsg.addProperty("role", "system");
        systemMsg.addProperty("content", systemPrompt);
        messages.add(systemMsg);

        for (JsonObject historyMsg : conversationHistory) {
            messages.add(historyMsg);
        }

        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", userMessage);
        messages.add(userMsg);

        // 构建请求体
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", model);
        requestBody.add("messages", messages);
        requestBody.addProperty("temperature", 0.7);
        requestBody.addProperty("max_tokens", 500);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                AiChatMod.LOGGER.error("LLM API 请求失败，状态码: {}, 响应: {}", response.statusCode(), response.body());
                return "AI 好像开小差了（API 返回错误：" + response.statusCode() + "）";
            }

            JsonObject responseJson = gson.fromJson(response.body(), JsonObject.class);
            String reply = responseJson.getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();

            // 把这轮对话存入历史
            conversationHistory.add(userMsg);
            JsonObject assistantMsg = new JsonObject();
            assistantMsg.addProperty("role", "assistant");
            assistantMsg.addProperty("content", reply);
            conversationHistory.add(assistantMsg);

            // 历史最多保留 20 条，防止 token 爆炸
            while (conversationHistory.size() > 20) {
                conversationHistory.remove(0);
            }

            return reply.trim();

        } catch (Exception e) {
            AiChatMod.LOGGER.error("调用 LLM API 时发生异常", e);
            return "AI 连接失败：" + e.getMessage();
        }
    }

    /**
     * 清空对话历史。
     */
    public void clearHistory() {
        conversationHistory.clear();
    }
}

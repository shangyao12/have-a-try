# AI Chat Buddy

一个把大语言模型（LLM）接入 Minecraft 的 Fabric 服务端模组。玩家在聊天框输入 `!ai 你的问题`，AI 就会在游戏里回复你。

## 功能

- 🤖 接入 DeepSeek、OpenAI、本地 Ollama 等任何 OpenAI 兼容 API
- 💬 保留对话历史，AI 有上下文记忆
- ⚙️ 可配置 API 地址、模型、系统提示词、触发前缀
- 🎮 纯服务端模组，客户端不需要安装
- 🔄 输入 `!ai clear` 或 `!ai 重置` 可清空对话历史

## 环境要求

- Minecraft 1.21.1
- Fabric Loader 0.19+
- Fabric API
- Java 21

## 安装

1. 下载 `build/libs/ai-chat-buddy-0.1.0.jar`
2. 放入服务器的 `mods/` 文件夹
3. 确保同时安装了 [Fabric API](https://modrinth.com/mod/fabric-api)
4. 启动服务器，会自动生成配置文件

## 配置

首次启动后，在服务器 `config/` 目录下会生成 `aichatbuddy.json`：

```json
{
  "apiKey": "sk-your-api-key-here",
  "baseUrl": "https://api.deepseek.com/v1",
  "model": "deepseek-chat",
  "systemPrompt": "你是一个住在 Minecraft 世界里的友好 AI 助手...",
  "triggerPrefix": "!ai"
}
```

| 配置项 | 说明 | 示例 |
|---|---|---|
| `apiKey` | API 密钥 | `sk-xxxxxxxx` |
| `baseUrl` | API 地址（OpenAI 兼容格式） | DeepSeek: `https://api.deepseek.com/v1`，本地 Ollama: `http://localhost:11434/v1` |
| `model` | 模型名称 | `deepseek-chat`、`gpt-4o-mini`、`llama3.1` |
| `systemPrompt` | AI 的系统提示词，设定角色和性格 | 自定义 |
| `triggerPrefix` | 触发 AI 的聊天前缀 | `!ai` |

## 使用

在游戏聊天框输入：

```
!ai 怎么合成钻石镐？
!ai 今晚吃什么好？
!ai clear
```

## 从源码构建

```bash
git clone https://github.com/shangyao12/have-a-try.git
cd have-a-try
./gradlew build
```

构建产物在 `build/libs/` 目录下。

## 项目结构

```
src/main/java/io/github/shangyao12/aichat/
├── AiChatMod.java      # 模组主类，入口点，注册聊天监听
├── LlmClient.java      # LLM API 调用客户端（OpenAI 兼容）
└── ModConfig.java      # 配置文件管理

src/main/resources/
├── fabric.mod.json     # 模组元数据
└── assets/aichatbuddy/ # 资源文件（图标等）
```

## 技术要点

- 使用 Fabric API 的 `ServerMessageEvents` 监听玩家聊天
- LLM 调用在异步线程执行，不阻塞服务器主线程
- 对话历史最多保留 20 条，防止 token 溢出
- 使用 Java 自带 `HttpClient`，无额外依赖

## License

MIT

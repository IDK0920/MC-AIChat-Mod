package com.example.aichat.server;

import com.example.aichat.AIChatMod;
import com.example.aichat.config.ModConfig;
import com.example.aichat.network.NetworkHandler;
import com.example.aichat.network.packets.AIChatResponsePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = AIChatMod.MOD_ID)
public class ServerEventHandler {
    private static final Logger LOGGER = LogManager.getLogger(ServerEventHandler.class);
    private static HttpClient httpClient;
    
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(ModConfig.TIMEOUT.get()))
                .build();
        LOGGER.info("HTTP Client initialized for Ollama API");
        LOGGER.info("System prompt loaded: {}", ModConfig.SYSTEM_PROMPT.get());
    }
    
    public static void handleAIChat(ServerPlayer player, String question, String playerName) {
        sendToOllama(question, player, playerName);
    }
    
    private static void sendToOllama(String question, ServerPlayer player, String playerName) {
        try {
            String url = ModConfig.OLLAMA_URL.get();
            String model = ModConfig.OLLAMA_MODEL.get();
            boolean disableThinking = ModConfig.DISABLE_THINKING.get();
            String systemPrompt = ModConfig.SYSTEM_PROMPT.get();
            
            // 构建包含系统提示词的 JSON
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{");
            jsonBuilder.append("\"model\":\"").append(escapeJson(model)).append("\",");
            jsonBuilder.append("\"messages\":[");
            
            // 添加系统提示词
            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                jsonBuilder.append("{\"role\":\"system\",\"content\":\"").append(escapeJson(systemPrompt)).append("\"},");
            }
            
            // 添加用户消息
            jsonBuilder.append("{\"role\":\"user\",\"content\":\"").append(escapeJson(question)).append("\"}");
            jsonBuilder.append("],");
            
            if (disableThinking) {
                jsonBuilder.append("\"think\":false,");
            }
            jsonBuilder.append("\"stream\":false");
            jsonBuilder.append("}");
            
            String jsonBody = jsonBuilder.toString();
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(ModConfig.TIMEOUT.get()))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();
            
            CompletableFuture<HttpResponse<String>> future = httpClient.sendAsync(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );
            
            future.thenAccept(response -> {
                if (response.statusCode() == 200) {
                    try {
                        String content = extractContentFromJson(response.body());
                        if (content != null && !content.isEmpty()) {
                            sendResponseToPlayer(player, content, playerName);
                        } else {
                            sendResponseToPlayer(player, "😅 哎呀，我走神了，再说一遍呗？", playerName);
                        }
                    } catch (Exception e) {
                        LOGGER.error("Failed to parse Ollama response: ", e);
                        sendResponseToPlayer(player, "😅 出了点小问题，再试试？", playerName);
                    }
                } else {
                    LOGGER.error("Ollama API error: {}", response.statusCode());
                    sendResponseToPlayer(player, "😅 我暂时连不上网，等等再聊！", playerName);
                }
            }).exceptionally(e -> {
                LOGGER.error("Failed to connect to Ollama: ", e);
                sendResponseToPlayer(player, "😅 网络不太好，晚点再找我玩吧！", playerName);
                return null;
            });
            
        } catch (Exception e) {
            LOGGER.error("Error sending to Ollama: ", e);
            sendResponseToPlayer(player, "😅 出错了，重启一下试试？", playerName);
        }
    }
    
    // 手动解析 JSON 提取 content
    private static String extractContentFromJson(String json) {
        int messageIndex = json.indexOf("\"message\"");
        if (messageIndex == -1) return null;
        
        int contentIndex = json.indexOf("\"content\"", messageIndex);
        if (contentIndex == -1) return null;
        
        int startQuote = json.indexOf('"', contentIndex + 9);
        if (startQuote == -1) return null;
        
        int endQuote = startQuote + 1;
        while (endQuote < json.length()) {
            char c = json.charAt(endQuote);
            if (c == '"' && json.charAt(endQuote - 1) != '\\') {
                break;
            }
            endQuote++;
        }
        
        if (endQuote >= json.length()) return null;
        
        String content = json.substring(startQuote + 1, endQuote);
        return unescapeJson(content);
    }
    
    // JSON 转义
    private static String escapeJson(String text) {
        if (text == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '/': sb.append("\\/"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default: sb.append(c);
            }
        }
        return sb.toString();
    }
    
    // JSON 反转义
    private static String unescapeJson(String text) {
        if (text == null) return "";
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (c == '\\' && i + 1 < text.length()) {
                char next = text.charAt(i + 1);
                switch (next) {
                    case '"': sb.append('"'); i += 2; break;
                    case '\\': sb.append('\\'); i += 2; break;
                    case '/': sb.append('/'); i += 2; break;
                    case 'b': sb.append('\b'); i += 2; break;
                    case 'f': sb.append('\f'); i += 2; break;
                    case 'n': sb.append('\n'); i += 2; break;
                    case 'r': sb.append('\r'); i += 2; break;
                    case 't': sb.append('\t'); i += 2; break;
                    default: sb.append(c); i++;
                }
            } else {
                sb.append(c);
                i++;
            }
        }
        return sb.toString();
    }
    
    private static void sendResponseToPlayer(ServerPlayer player, String response, String playerName) {
        AIChatResponsePacket packet = new AIChatResponsePacket(response, playerName);
        NetworkHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), packet);
    }
}
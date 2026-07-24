package com.example.aichat.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

public class ModConfig {
    public static final ForgeConfigSpec SPEC;
    
    public static ConfigValue<String> OLLAMA_URL;
    public static ConfigValue<String> OLLAMA_MODEL;
    public static ConfigValue<Integer> TIMEOUT;
    public static ConfigValue<Boolean> DISABLE_THINKING;
    public static ConfigValue<String> SYSTEM_PROMPT;
    
    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        
        builder.comment("AI Chat Mod Configuration").push("general");
        
        OLLAMA_URL = builder
                .comment("Ollama API URL")
                .define("ollamaUrl", "http://localhost:11434/api/chat");
        
        OLLAMA_MODEL = builder
                .comment("Ollama model to use")
                .define("ollamaModel", "qwen3");
        
        TIMEOUT = builder
                .comment("Timeout in seconds for Ollama API calls")
                .define("timeout", 30);
        
        DISABLE_THINKING = builder
                .comment("Disable thinking mode for models that support it")
                .define("disableThinking", true);
        
        SYSTEM_PROMPT = builder
                .comment("System prompt for the AI assistant")
                .define("systemPrompt", 
                    "你是一个Minecraft游戏里的AI助手。\n" +
                    "你的性格：友好、活泼、幽默，喜欢帮助玩家。\n" +
                    "你的任务：\n" +
                    "1. 陪玩家闲聊，就像游戏里的朋友一样\n" +
                    "2. 解答玩家关于Minecraft的问题\n" +
                    "3. 回答要简短精炼\n" +
                    "4. 用轻松愉快的语气说话\n" +
                    "5. 适当使用表情符号(emoji)让对话更有趣\n" +
                    "记住：你是Minecraft世界里的一个好朋友！"
                );
        
        SPEC = builder.build();
    }
}
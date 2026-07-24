package com.example.aichat.client;

import com.example.aichat.AIChatMod;
import com.example.aichat.config.ModConfig;
import com.example.aichat.network.NetworkHandler;
import com.example.aichat.network.packets.AIChatPacket;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AIChatMod.MOD_ID, value = Dist.CLIENT)
public class ClientEventHandler {
    private static String lastResponse = "";
    private static String lastPlayerName = "";
    
    @SubscribeEvent
    public static void onClientChat(ClientChatEvent event) {
        String message = event.getMessage();
        
        // Check if message starts with @AI
        if (message.trim().toLowerCase().startsWith("@ai")) {
            event.setCanceled(true);
            
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) return;
            
            // Remove @AI prefix
            String question = message.substring(3).trim();
            if (question.isEmpty()) {
                player.sendSystemMessage(Component.literal("请提出你的问题！").withStyle(ChatFormatting.RED));
                return;
            }
            
            // Send to server
            NetworkHandler.INSTANCE.sendToServer(new AIChatPacket(question, player.getName().getString()));
            
            // Show processing message
            player.sendSystemMessage(Component.literal("🤖 AI 正在思考...").withStyle(ChatFormatting.YELLOW));
        }
    }
    
    public static void handleAIChatResponse(String response, String playerName) {
        lastResponse = response;
        lastPlayerName = playerName;
        
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            Component message = Component.literal("[AI] ")
                    .withStyle(ChatFormatting.GOLD)
                    .append(Component.literal(playerName + ": ")
                            .withStyle(ChatFormatting.AQUA))
                    .append(Component.literal(response)
                            .withStyle(ChatFormatting.WHITE));
            minecraft.player.sendSystemMessage(message);
        }
    }
    
    @SubscribeEvent
    public static void registerCommands(RegisterClientCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        
        dispatcher.register(Commands.literal("ai")
                .then(Commands.literal("reload")
                        .executes(context -> {
                            // Reload config command (client side)
                            Minecraft.getInstance().player.sendSystemMessage(
                                    Component.literal("配置重载功能请在服务器端使用").withStyle(ChatFormatting.YELLOW));
                            return 1;
                        }))
                .then(Commands.literal("test")
                        .executes(context -> {
                            // Test connection
                            Minecraft.getInstance().player.sendSystemMessage(
                                    Component.literal("测试连接到 Ollama...").withStyle(ChatFormatting.YELLOW));
                            // Send test message to server
                            NetworkHandler.INSTANCE.sendToServer(
                                    new AIChatPacket("你好，测试连接", Minecraft.getInstance().player.getName().getString()));
                            return 1;
                        }))
        );
    }
}
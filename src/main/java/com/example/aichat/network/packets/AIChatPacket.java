package com.example.aichat.network.packets;

import com.example.aichat.server.ServerEventHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class AIChatPacket {
    private final String message;
    private final String playerName;
    
    public AIChatPacket(String message, String playerName) {
        this.message = message;
        this.playerName = playerName;
    }
    
    public static void encode(AIChatPacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.message);
        buf.writeUtf(packet.playerName);
    }
    
    public static AIChatPacket decode(FriendlyByteBuf buf) {
        return new AIChatPacket(buf.readUtf(32767), buf.readUtf(32767));
    }
    
    public static void handle(AIChatPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player != null) {
                // Send to Ollama and broadcast response
                ServerEventHandler.handleAIChat(player, packet.message, packet.playerName);
            }
        });
        context.setPacketHandled(true);
    }
    
    public String getMessage() { return message; }
    public String getPlayerName() { return playerName; }
}
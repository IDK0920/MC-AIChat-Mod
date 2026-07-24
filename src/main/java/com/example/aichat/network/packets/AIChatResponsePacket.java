package com.example.aichat.network.packets;

import com.example.aichat.client.ClientEventHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class AIChatResponsePacket {
    private final String response;
    private final String playerName;
    
    public AIChatResponsePacket(String response, String playerName) {
        this.response = response;
        this.playerName = playerName;
    }
    
    public static void encode(AIChatResponsePacket packet, FriendlyByteBuf buf) {
        buf.writeUtf(packet.response);
        buf.writeUtf(packet.playerName);
    }
    
    public static AIChatResponsePacket decode(FriendlyByteBuf buf) {
        return new AIChatResponsePacket(buf.readUtf(32767), buf.readUtf(32767));
    }
    
    public static void handle(AIChatResponsePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ClientEventHandler.handleAIChatResponse(packet.response, packet.playerName);
        });
        context.setPacketHandled(true);
    }
    
    public String getResponse() { return response; }
    public String getPlayerName() { return playerName; }
}
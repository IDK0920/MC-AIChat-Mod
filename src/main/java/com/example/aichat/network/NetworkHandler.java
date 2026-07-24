package com.example.aichat.network;

import com.example.aichat.AIChatMod;
import com.example.aichat.network.packets.AIChatPacket;
import com.example.aichat.network.packets.AIChatResponsePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(AIChatMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    
    private static int id = 0;
    
    public static void register() {
        INSTANCE.registerMessage(id++, AIChatPacket.class,
                AIChatPacket::encode,
                AIChatPacket::decode,
                AIChatPacket::handle);
        
        INSTANCE.registerMessage(id++, AIChatResponsePacket.class,
                AIChatResponsePacket::encode,
                AIChatResponsePacket::decode,
                AIChatResponsePacket::handle);
    }
}
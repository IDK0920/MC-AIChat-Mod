package com.example.aichat;

import com.example.aichat.config.ModConfig;
import com.example.aichat.network.NetworkHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(AIChatMod.MOD_ID)
public class AIChatMod {
    public static final String MOD_ID = "aichatmod";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    
    public AIChatMod() {
        ModLoadingContext.get().registerConfig(Type.COMMON, ModConfig.SPEC, "aichatmod-common.toml");
        
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    private void setup(final FMLCommonSetupEvent event) {
        NetworkHandler.register();
        LOGGER.info("AI Chat Mod loaded successfully!");
        LOGGER.info("Ollama URL: {}", ModConfig.OLLAMA_URL.get());
        LOGGER.info("Ollama Model: {}", ModConfig.OLLAMA_MODEL.get());
    }
}
package com.chedd.authy;

import com.chedd.authy.config.AuthyConfig;
import com.chedd.authy.network.AuthyPackets;
import com.chedd.authy.server.AuthHandler;
import com.chedd.authy.server.AuthyCommands;
import com.chedd.authy.server.LimboManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Authy.MOD_ID)
public class Authy {
    public static final String MOD_ID = "authy";
    public static final Logger LOGGER = LogManager.getLogger();

    public Authy() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, AuthyConfig.CLIENT_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, AuthyConfig.SERVER_SPEC);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new LimboManager());
        MinecraftForge.EVENT_BUS.register(new AuthHandler());
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(AuthyPackets::register);
        LOGGER.info("Authy authentication system initialized");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        AuthHandler.onServerStart(event.getServer());
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        AuthHandler.onServerStop();
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        AuthyCommands.register(event.getDispatcher());
    }
}

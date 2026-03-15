package com.chedd.authy.client;

import com.chedd.authy.Authy;
import com.chedd.authy.network.AuthyPackets;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Authy.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ClientIdentity.init();
        AuthyPackets.clientHandler = ClientPacketHandler::handleAuthResult;
    }
}

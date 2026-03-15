package com.chedd.authy.client;

import com.chedd.authy.Authy;
import com.chedd.authy.network.AuthyPackets;
import com.chedd.authy.network.IdentityPacket;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Authy.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientLoginHandler {

    @SubscribeEvent
    public static void onPlayerLoggedIn(ClientPlayerNetworkEvent.LoggingIn event) {
        Authy.LOGGER.info("Sending identity to server...");
        AuthyPackets.CHANNEL.sendToServer(
                new IdentityPacket(ClientIdentity.getClientUUID(), ClientIdentity.getHwidHash())
        );
    }
}

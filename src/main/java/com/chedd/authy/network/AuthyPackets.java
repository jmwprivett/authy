package com.chedd.authy.network;

import com.chedd.authy.Authy;
import com.chedd.authy.server.AuthHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Consumer;

public class AuthyPackets {
    private static final String PROTOCOL_VERSION = "1";

    // Set by the client side only — stays null on dedicated server
    public static Consumer<AuthResultPacket> clientHandler = null;

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Authy.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;

        CHANNEL.messageBuilder(IdentityPacket.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(IdentityPacket::encode)
                .decoder(IdentityPacket::new)
                .consumerMainThread((msg, ctx) -> {
                    ServerPlayer player = ctx.get().getSender();
                    if (player != null) {
                        AuthHandler.handleIdentity(player, msg);
                    }
                    ctx.get().setPacketHandled(true);
                })
                .add();

        CHANNEL.messageBuilder(AuthResultPacket.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(AuthResultPacket::encode)
                .decoder(AuthResultPacket::new)
                .consumerMainThread((msg, ctx) -> {
                    if (clientHandler != null) {
                        clientHandler.accept(msg);
                    }
                    ctx.get().setPacketHandled(true);
                })
                .add();
    }

    public static void sendToPlayer(ServerPlayer player, AuthResultPacket packet) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}

package com.chedd.authy.server;

import com.chedd.authy.Authy;
import com.chedd.authy.config.AuthyConfig;
import com.chedd.authy.data.IdentityManager;
import com.chedd.authy.data.PlayerIdentity;
import com.chedd.authy.network.AuthResultPacket;
import com.chedd.authy.network.AuthyPackets;
import com.chedd.authy.network.IdentityPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class AuthHandler {
    private static IdentityManager identityManager;

    public static void onServerStart(MinecraftServer server) {
        identityManager = new IdentityManager(server.getWorldPath(LevelResource.ROOT), server.getServerDirectory().toPath());
        Authy.LOGGER.info("Authy identity manager loaded");
    }

    public static void onServerStop() {
        if (identityManager != null) {
            identityManager.save();
        }
        LimboManager.clear();
    }

    public static IdentityManager getIdentityManager() {
        return identityManager;
    }

    public static void handleIdentity(ServerPlayer player, IdentityPacket packet) {
        String playerUuid = player.getStringUUID();
        String username = player.getGameProfile().getName();
        String hwidHash = packet.getHwidHash();
        String clientUUID = packet.getClientUUID();
        String ip = player.getIpAddress();

        PlayerIdentity existing = identityManager.getByUuid(playerUuid);

        if (existing == null) {
            // New player — register and give recovery code
            String recoveryCode = identityManager.registerNewPlayer(playerUuid, username, hwidHash, clientUUID, ip);
            String prefix = AuthyConfig.MSG_PREFIX.get();

            MutableComponent msg = Component.literal("")
                    .append(Component.literal(prefix).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                    .append(Component.literal(AuthyConfig.MSG_WELCOME.get()).withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal(recoveryCode).withStyle(Style.EMPTY
                            .withColor(ChatFormatting.GREEN)
                            .withBold(true)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, recoveryCode))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to copy")))))
                    .append(Component.literal("\n"))
                    .append(Component.literal(prefix).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                    .append(Component.literal(AuthyConfig.MSG_SAVE_CODE.get()).withStyle(ChatFormatting.RED));

            player.sendSystemMessage(msg);
            AuthyPackets.sendToPlayer(player, new AuthResultPacket(AuthResultPacket.Result.NEW_PLAYER, recoveryCode));
            Authy.LOGGER.info("New player registered: {} ({})", username, playerUuid);

        } else if (existing.hasDevice(hwidHash, clientUUID)) {
            // Known device — silent auth
            existing.setUsername(username);
            identityManager.updateLastSeen(playerUuid, hwidHash, clientUUID);
            AuthyPackets.sendToPlayer(player, new AuthResultPacket(AuthResultPacket.Result.AUTHENTICATED, null));
            Authy.LOGGER.info("Player authenticated: {} (known device)", username);

        } else if (existing.hasDeviceByHwid(hwidHash)) {
            // Same hardware but different client UUID (e.g. packwiz update nuked authy_id)
            existing.setUsername(username);
            existing.updateDeviceClientUUID(hwidHash, clientUUID);
            identityManager.save();
            AuthyPackets.sendToPlayer(player, new AuthResultPacket(AuthResultPacket.Result.AUTHENTICATED, null));
            Authy.LOGGER.info("Player authenticated: {} (same hardware, updated client UUID)", username);

        } else {
            // Known player, unknown device — limbo
            LimboManager.addToLimbo(player.getUUID(), hwidHash, clientUUID, ip);
            String prefix = AuthyConfig.MSG_PREFIX.get();

            MutableComponent msg = Component.literal("")
                    .append(Component.literal(prefix).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
                    .append(Component.literal(AuthyConfig.MSG_UNRECOGNIZED_DEVICE.get()).withStyle(ChatFormatting.RED));

            player.sendSystemMessage(msg);
            AuthyPackets.sendToPlayer(player, new AuthResultPacket(AuthResultPacket.Result.LIMBO, null));
            Authy.LOGGER.info("Player {} joined from unknown device — placed in limbo", username);
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        LimboManager.removeFromLimbo(event.getEntity().getUUID());
    }
}

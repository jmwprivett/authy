package com.chedd.authy.client;

import com.chedd.authy.Authy;
import com.chedd.authy.client.toast.AuthyToast;
import com.chedd.authy.config.AuthyConfig;
import com.chedd.authy.network.AuthResultPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class ClientPacketHandler {
    public static void handleAuthResult(AuthResultPacket msg) {
        String title = AuthyConfig.TOAST_TITLE.get();

        switch (msg.getResult()) {
            case NEW_PLAYER -> {
                String welcomeMsg = String.format(AuthyConfig.TOAST_WELCOME.get(), msg.getRecoveryCode());
                Minecraft.getInstance().getToasts().addToast(
                        new AuthyToast(Component.literal(title), Component.literal(welcomeMsg), AuthyToast.Type.SUCCESS));
            }
            case AUTHENTICATED -> Minecraft.getInstance().getToasts().addToast(
                        new AuthyToast(Component.literal(title), Component.literal(AuthyConfig.TOAST_AUTHENTICATED.get()), AuthyToast.Type.SUCCESS));
            case LIMBO -> Minecraft.getInstance().getToasts().addToast(
                        new AuthyToast(Component.literal(title), Component.literal(AuthyConfig.TOAST_LIMBO.get()), AuthyToast.Type.WARNING));
        }
    }
}

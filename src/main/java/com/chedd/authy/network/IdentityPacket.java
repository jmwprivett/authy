package com.chedd.authy.network;

import net.minecraft.network.FriendlyByteBuf;

public class IdentityPacket {
    private final String clientUUID;
    private final String hwidHash;

    public IdentityPacket(String clientUUID, String hwidHash) {
        this.clientUUID = clientUUID;
        this.hwidHash = hwidHash;
    }

    public IdentityPacket(FriendlyByteBuf buf) {
        this.clientUUID = buf.readUtf(64);
        this.hwidHash = buf.readUtf(128);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(clientUUID, 64);
        buf.writeUtf(hwidHash, 128);
    }

    public String getClientUUID() {
        return clientUUID;
    }

    public String getHwidHash() {
        return hwidHash;
    }
}

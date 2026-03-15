package com.chedd.authy.network;

import net.minecraft.network.FriendlyByteBuf;

public class AuthResultPacket {

    public enum Result {
        AUTHENTICATED,
        LIMBO,
        NEW_PLAYER
    }

    private final Result result;
    private final String recoveryCode; // only set for NEW_PLAYER

    public AuthResultPacket(Result result, String recoveryCode) {
        this.result = result;
        this.recoveryCode = recoveryCode != null ? recoveryCode : "";
    }

    public AuthResultPacket(FriendlyByteBuf buf) {
        this.result = buf.readEnum(Result.class);
        this.recoveryCode = buf.readUtf(32);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(result);
        buf.writeUtf(recoveryCode, 32);
    }

    public Result getResult() {
        return result;
    }

    public String getRecoveryCode() {
        return recoveryCode;
    }
}

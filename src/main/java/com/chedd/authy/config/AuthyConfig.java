package com.chedd.authy.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class AuthyConfig {
    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final ForgeConfigSpec SERVER_SPEC;

    // Toast messages (CLIENT)
    public static final ForgeConfigSpec.ConfigValue<String> TOAST_TITLE;
    public static final ForgeConfigSpec.ConfigValue<String> TOAST_WELCOME;
    public static final ForgeConfigSpec.ConfigValue<String> TOAST_AUTHENTICATED;
    public static final ForgeConfigSpec.ConfigValue<String> TOAST_LIMBO;
    public static final ForgeConfigSpec.ConfigValue<String> TOAST_RECOVERED;

    // Toast appearance (CLIENT)
    public static final ForgeConfigSpec.IntValue TOAST_WIDTH;
    public static final ForgeConfigSpec.IntValue TOAST_HEIGHT;
    public static final ForgeConfigSpec.IntValue TOAST_DURATION;

    // Chat messages (SERVER)
    public static final ForgeConfigSpec.ConfigValue<String> MSG_PREFIX;
    public static final ForgeConfigSpec.ConfigValue<String> MSG_WELCOME;
    public static final ForgeConfigSpec.ConfigValue<String> MSG_SAVE_CODE;
    public static final ForgeConfigSpec.ConfigValue<String> MSG_UNRECOGNIZED_DEVICE;
    public static final ForgeConfigSpec.ConfigValue<String> MSG_DEVICE_VERIFIED;
    public static final ForgeConfigSpec.ConfigValue<String> MSG_ALREADY_VERIFIED;
    public static final ForgeConfigSpec.ConfigValue<String> MSG_INVALID_CODE;
    public static final ForgeConfigSpec.ConfigValue<String> MSG_ADMIN_APPROVED;
    public static final ForgeConfigSpec.ConfigValue<String> MSG_DEVICES_REVOKED;
    public static final ForgeConfigSpec.ConfigValue<String> MSG_LIMBO_ACTION_BLOCKED;
    public static final ForgeConfigSpec.ConfigValue<String> MSG_NOT_IN_LIMBO;
    public static final ForgeConfigSpec.ConfigValue<String> MSG_PLAYER_NOT_FOUND;
    public static final ForgeConfigSpec.ConfigValue<String> MSG_NEW_CODE_GENERATED;

    static {
        // === CLIENT CONFIG (authy-client.toml) ===
        ForgeConfigSpec.Builder client = new ForgeConfigSpec.Builder();

        client.comment("Toast popup messages (shown in the top-right corner)").push("toast");
        TOAST_TITLE = client
                .comment("Title shown on all Authy toast popups")
                .define("title", "Authy");
        TOAST_WELCOME = client
                .comment("Toast message for new players. Use %s for the recovery code.")
                .define("welcome", "WELCOME GAYMER!!!!");
        TOAST_AUTHENTICATED = client
                .comment("Toast message when a known device is verified")
                .define("authenticated", "so.... you come here often?");
        TOAST_LIMBO = client
                .comment("Toast message when joining from an unknown device")
                .define("limbo", "HACKER ALERT!!!!!!!!!!!");
        TOAST_RECOVERED = client
                .comment("Toast message after successfully recovering")
                .define("recovered", "Verified! Welcome back gaymer.");
        TOAST_WIDTH = client
                .comment("Width of the toast popup in pixels")
                .defineInRange("width", 195, 160, 400);
        TOAST_HEIGHT = client
                .comment("Height of the toast popup in pixels")
                .defineInRange("height", 40, 32, 100);
        TOAST_DURATION = client
                .comment("How long the toast is shown in milliseconds")
                .defineInRange("duration", 10000, 2000, 30000);
        client.pop();

        CLIENT_SPEC = client.build();

        // === SERVER CONFIG (authy-server.toml) ===
        ForgeConfigSpec.Builder server = new ForgeConfigSpec.Builder();

        server.comment("Chat messages sent to players").push("messages");
        MSG_PREFIX = server
                .comment("Prefix for all chat messages")
                .define("prefix", "[Authy] ");
        MSG_WELCOME = server
                .comment("Message shown to new players. Use %s for the recovery code.")
                .define("welcome", "Your recovery code is: ");
        MSG_SAVE_CODE = server
                .comment("Warning to save the recovery code")
                .define("saveCode", "SAVE THIS CODE, you'll automatically login on this computer, but you can use the code to login on another one.");
        MSG_UNRECOGNIZED_DEVICE = server
                .comment("Message when joining from an unknown device")
                .define("unrecognizedDevice", "Uh Oh!!! new phone who is dis..... use /authy recover <code> or ask for help if you lost it ;P");
        MSG_DEVICE_VERIFIED = server
                .comment("Message after successful device verification")
                .define("deviceVerified", "YOU DID IT! You're now ready to craft or whatever.....");
        MSG_ALREADY_VERIFIED = server
                .comment("Message when player is already verified")
                .define("alreadyVerified", "You are already verified.....");
        MSG_INVALID_CODE = server
                .comment("Message when an invalid recovery code is entered")
                .define("invalidCode", "ERROR!!!!!!!!!! ARE YOU A HACKER??????? or just made a typo.....");
        MSG_ADMIN_APPROVED = server
                .comment("Message sent to player when an admin approves them")
                .define("adminApproved", "Ash or Josh logged you in, now you're big chilling");
        MSG_DEVICES_REVOKED = server
                .comment("Message sent to player when their devices are revoked")
                .define("devicesRevoked", "REVOKED LOL SEE YA CHUD. Use your code to re-verify, unless you dont have it.....");
        MSG_LIMBO_ACTION_BLOCKED = server
                .comment("Action bar message shown when a limbo player tries to interact")
                .define("limboActionBlocked", "yousuckyousuckyousuckyousuck..... do /authy recover <code> or ask for help");
        MSG_NOT_IN_LIMBO = server
                .comment("Admin message when target player is not in limbo")
                .define("notInLimbo", "%s is not in limbo.");
        MSG_PLAYER_NOT_FOUND = server
                .comment("Admin message when player is not in the Authy database")
                .define("playerNotFound", "Player not found in Authy database.");
        MSG_NEW_CODE_GENERATED = server
                .comment("Admin message when a new recovery code is generated. Use %s for player name.")
                .define("newCodeGenerated", "New recovery code for %s: ");
        server.pop();

        SERVER_SPEC = server.build();
    }
}

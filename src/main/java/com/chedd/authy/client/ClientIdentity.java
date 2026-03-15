package com.chedd.authy.client;

import com.chedd.authy.Authy;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.net.NetworkInterface;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.UUID;

public class ClientIdentity {
    private static final String LEGACY_ID_FILE_NAME = "authy_id";
    private static final String CLIENT_ID_FILE_NAME = "client-id";
    private static String clientUUID;
    private static String hwidHash;

    public static void init() {
        clientUUID = loadOrGenerateClientUUID();
        hwidHash = generateHwidHash();
        Authy.LOGGER.info("Client identity initialized");
    }

    public static String getClientUUID() {
        return clientUUID;
    }

    public static String getHwidHash() {
        return hwidHash;
    }

    private static String loadOrGenerateClientUUID() {
        Path idFile = getPersistentClientIdFile();
        Path legacyIdFile = Minecraft.getInstance().gameDirectory.toPath().resolve(LEGACY_ID_FILE_NAME);
        try {
            Files.createDirectories(idFile.getParent());

            String stored = readStoredClientId(idFile);
            if (stored != null) {
                return stored;
            }

            String migrated = migrateLegacyClientId(legacyIdFile, idFile);
            if (migrated != null) {
                return migrated;
            }

            String newId = UUID.randomUUID().toString();
            Files.writeString(idFile, newId);
            Authy.LOGGER.info("Created client identity at {}", idFile);
            return newId;
        } catch (IOException e) {
            Authy.LOGGER.error("Failed to load/save client UUID, generating ephemeral one", e);
            return UUID.randomUUID().toString();
        }
    }

    private static Path getPersistentClientIdFile() {
        return getUserDataDirectory().resolve(Authy.MOD_ID).resolve(CLIENT_ID_FILE_NAME);
    }

    private static Path getUserDataDirectory() {
        String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        Path userHome = Path.of(System.getProperty("user.home", "."));

        if (osName.contains("win")) {
            String appData = System.getenv("APPDATA");
            if (appData != null && !appData.isBlank()) {
                return Path.of(appData);
            }
        } else if (osName.contains("mac")) {
            return userHome.resolve("Library").resolve("Application Support");
        } else {
            String xdgDataHome = System.getenv("XDG_DATA_HOME");
            if (xdgDataHome != null && !xdgDataHome.isBlank()) {
                return Path.of(xdgDataHome);
            }
            return userHome.resolve(".local").resolve("share");
        }

        return userHome.resolve("." + Authy.MOD_ID);
    }

    private static String readStoredClientId(Path path) throws IOException {
        if (!Files.exists(path)) {
            return null;
        }

        String stored = Files.readString(path).trim();
        return stored.isEmpty() ? null : stored;
    }

    private static String migrateLegacyClientId(Path legacyIdFile, Path newIdFile) throws IOException {
        String legacyId = readStoredClientId(legacyIdFile);
        if (legacyId == null) {
            return null;
        }

        Files.writeString(newIdFile, legacyId);
        Files.deleteIfExists(legacyIdFile);
        Authy.LOGGER.info("Migrated legacy client identity from {} to {}", legacyIdFile, newIdFile);
        return legacyId;
    }

    private static String generateHwidHash() {
        StringBuilder hwInfo = new StringBuilder();
        hwInfo.append(System.getProperty("os.name", "unknown"));
        hwInfo.append(System.getProperty("user.name", "unknown"));

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                byte[] mac = ni.getHardwareAddress();
                if (mac != null) {
                    StringBuilder macStr = new StringBuilder();
                    for (byte b : mac) {
                        macStr.append(String.format("%02X", b));
                    }
                    hwInfo.append(macStr);
                }
            }
        } catch (Exception e) {
            Authy.LOGGER.warn("Could not read network interfaces for HWID", e);
        }

        return sha256(hwInfo.toString());
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}

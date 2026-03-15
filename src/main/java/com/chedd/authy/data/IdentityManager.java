package com.chedd.authy.data;

import com.chedd.authy.Authy;
import com.google.gson.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IdentityManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path dataFile;
    private final Map<String, PlayerIdentity> playersByUuid = new ConcurrentHashMap<>();

    public IdentityManager(Path dataRootDir, Path legacyRootDir) {
        Path authyDir = dataRootDir.resolve("authy");
        try {
            Files.createDirectories(authyDir);
            migrateLegacyData(legacyRootDir.resolve("authy").resolve("players.json"), authyDir.resolve("players.json"));
        } catch (IOException e) {
            Authy.LOGGER.error("Failed to create authy data directory", e);
        }
        this.dataFile = authyDir.resolve("players.json");
        load();
    }

    public void load() {
        playersByUuid.clear();
        if (!Files.exists(dataFile)) return;

        try {
            String json = Files.readString(dataFile);
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            JsonArray players = root.getAsJsonArray("players");
            for (JsonElement el : players) {
                PlayerIdentity identity = new PlayerIdentity(el.getAsJsonObject());
                playersByUuid.put(identity.getUuid(), identity);
            }
            Authy.LOGGER.info("Loaded {} player identities", playersByUuid.size());
        } catch (Exception e) {
            Authy.LOGGER.error("Failed to load player data", e);
        }
    }

    public void save() {
        try {
            JsonObject root = new JsonObject();
            JsonArray players = new JsonArray();
            for (PlayerIdentity identity : playersByUuid.values()) {
                players.add(identity.toJson());
            }
            root.add("players", players);
            Files.writeString(dataFile, GSON.toJson(root));
        } catch (IOException e) {
            Authy.LOGGER.error("Failed to save player data", e);
        }
    }

    private void migrateLegacyData(Path legacyDataFile, Path newDataFile) throws IOException {
        if (Files.exists(newDataFile) || !Files.exists(legacyDataFile)) {
            return;
        }

        Files.copy(legacyDataFile, newDataFile);
        Authy.LOGGER.info("Migrated Authy player data from {} to {}", legacyDataFile, newDataFile);
    }

    public PlayerIdentity getByUuid(String uuid) {
        return playersByUuid.get(uuid);
    }

    public PlayerIdentity getByUsername(String username) {
        for (PlayerIdentity identity : playersByUuid.values()) {
            if (identity.getUsername().equalsIgnoreCase(username)) {
                return identity;
            }
        }
        return null;
    }

    public boolean isKnownDevice(String playerUuid, String hwidHash, String clientUUID) {
        PlayerIdentity identity = playersByUuid.get(playerUuid);
        return identity != null && identity.hasDevice(hwidHash, clientUUID);
    }

    public String registerNewPlayer(String playerUuid, String username, String hwidHash, String clientUUID, String ip) {
        PlayerIdentity identity = new PlayerIdentity(playerUuid, username);
        identity.addDevice(hwidHash, clientUUID, ip);
        playersByUuid.put(playerUuid, identity);
        save();
        return identity.getRecoveryCode();
    }

    public void addDevice(String playerUuid, String hwidHash, String clientUUID, String ip) {
        PlayerIdentity identity = playersByUuid.get(playerUuid);
        if (identity != null) {
            identity.addDevice(hwidHash, clientUUID, ip);
            save();
        }
    }

    public void updateLastSeen(String playerUuid, String hwidHash, String clientUUID) {
        PlayerIdentity identity = playersByUuid.get(playerUuid);
        if (identity != null) {
            identity.updateDeviceLastSeen(hwidHash, clientUUID);
            save();
        }
    }

    public boolean validateRecoveryCode(String playerUuid, String code) {
        PlayerIdentity identity = playersByUuid.get(playerUuid);
        return identity != null && identity.getRecoveryCode().equalsIgnoreCase(code);
    }

    public String regenerateRecoveryCode(String playerUuid) {
        PlayerIdentity identity = playersByUuid.get(playerUuid);
        if (identity != null) {
            String newCode = identity.regenerateRecoveryCode();
            save();
            return newCode;
        }
        return null;
    }

    public void revokeDevices(String playerUuid) {
        PlayerIdentity identity = playersByUuid.get(playerUuid);
        if (identity != null) {
            identity.clearDevices();
            save();
        }
    }
}

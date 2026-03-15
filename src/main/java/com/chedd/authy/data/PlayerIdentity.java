package com.chedd.authy.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;


public class PlayerIdentity {
    private final String uuid;
    private String username;
    private String recoveryCode;
    private final List<DeviceEntry> devices;

    public PlayerIdentity(String uuid, String username) {
        this.uuid = uuid;
        this.username = username;
        this.recoveryCode = generateRecoveryCode();
        this.devices = new ArrayList<>();
    }

    public PlayerIdentity(JsonObject json) {
        this.uuid = json.get("uuid").getAsString();
        this.username = json.get("username").getAsString();
        this.recoveryCode = json.get("recoveryCode").getAsString();
        this.devices = new ArrayList<>();
        JsonArray devArr = json.getAsJsonArray("devices");
        for (JsonElement el : devArr) {
            devices.add(new DeviceEntry(el.getAsJsonObject()));
        }
    }

    public boolean hasDevice(String hwidHash, String clientUUID) {
        for (DeviceEntry device : devices) {
            if (device.matches(hwidHash, clientUUID)) {
                return true;
            }
        }
        return false;
    }

    public void addDevice(String hwidHash, String clientUUID, String ip) {
        devices.add(new DeviceEntry(hwidHash, clientUUID, ip));
    }

    public boolean hasDeviceByHwid(String hwidHash) {
        for (DeviceEntry device : devices) {
            if (device.getHwidHash().equals(hwidHash)) {
                return true;
            }
        }
        return false;
    }

    public void updateDeviceClientUUID(String hwidHash, String newClientUUID) {
        for (DeviceEntry device : devices) {
            if (device.getHwidHash().equals(hwidHash)) {
                device.setClientUUID(newClientUUID);
                device.updateLastSeen();
                return;
            }
        }
    }

    public void updateDeviceLastSeen(String hwidHash, String clientUUID) {
        for (DeviceEntry device : devices) {
            if (device.matches(hwidHash, clientUUID)) {
                device.updateLastSeen();
                return;
            }
        }
    }

    public void clearDevices() {
        devices.clear();
    }

    public String getUuid() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRecoveryCode() {
        return recoveryCode;
    }

    public String regenerateRecoveryCode() {
        this.recoveryCode = generateRecoveryCode();
        return this.recoveryCode;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("uuid", uuid);
        json.addProperty("username", username);
        json.addProperty("recoveryCode", recoveryCode);
        JsonArray devArr = new JsonArray();
        for (DeviceEntry device : devices) {
            devArr.add(device.toJson());
        }
        json.add("devices", devArr);
        return json;
    }

    private static String generateRecoveryCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // no I/O/0/1 to avoid confusion
        StringBuilder code = new StringBuilder();
        java.util.Random random = new java.security.SecureRandom();
        for (int i = 0; i < 12; i++) {
            if (i > 0 && i % 4 == 0) code.append('-');
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }
}

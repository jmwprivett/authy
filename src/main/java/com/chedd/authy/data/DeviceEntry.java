package com.chedd.authy.data;

import com.google.gson.JsonObject;

public class DeviceEntry {
    private final String hwidHash;
    private String clientUUID;
    private final String ip;
    private final long firstSeen;
    private long lastSeen;

    public DeviceEntry(String hwidHash, String clientUUID, String ip) {
        this.hwidHash = hwidHash;
        this.clientUUID = clientUUID;
        this.ip = ip;
        this.firstSeen = System.currentTimeMillis();
        this.lastSeen = this.firstSeen;
    }

    public DeviceEntry(JsonObject json) {
        this.hwidHash = json.get("hwidHash").getAsString();
        this.clientUUID = json.get("clientUUID").getAsString();
        this.ip = json.get("ip").getAsString();
        this.firstSeen = json.get("firstSeen").getAsLong();
        this.lastSeen = json.get("lastSeen").getAsLong();
    }

    public boolean matches(String hwidHash, String clientUUID) {
        return this.hwidHash.equals(hwidHash) && this.clientUUID.equals(clientUUID);
    }

    public void updateLastSeen() {
        this.lastSeen = System.currentTimeMillis();
    }

    public String getHwidHash() {
        return hwidHash;
    }

    public String getClientUUID() {
        return clientUUID;
    }

    public void setClientUUID(String clientUUID) {
        this.clientUUID = clientUUID;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("hwidHash", hwidHash);
        json.addProperty("clientUUID", clientUUID);
        json.addProperty("ip", ip);
        json.addProperty("firstSeen", firstSeen);
        json.addProperty("lastSeen", lastSeen);
        return json;
    }
}

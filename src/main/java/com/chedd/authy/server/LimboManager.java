package com.chedd.authy.server;

import com.chedd.authy.config.AuthyConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LimboManager {
    private static final Set<UUID> limboPlayers = ConcurrentHashMap.newKeySet();

    // Store pending device info so we can register it after recovery
    private static final Map<UUID, PendingDevice> pendingDevices = new ConcurrentHashMap<>();

    public static void addToLimbo(UUID playerUuid, String hwidHash, String clientUUID, String ip) {
        limboPlayers.add(playerUuid);
        pendingDevices.put(playerUuid, new PendingDevice(hwidHash, clientUUID, ip));
    }

    public static void removeFromLimbo(UUID playerUuid) {
        limboPlayers.remove(playerUuid);
        pendingDevices.remove(playerUuid);
    }

    public static boolean isInLimbo(UUID playerUuid) {
        return limboPlayers.contains(playerUuid);
    }

    public static PendingDevice getPendingDevice(UUID playerUuid) {
        return pendingDevices.get(playerUuid);
    }

    public static void clear() {
        limboPlayers.clear();
        pendingDevices.clear();
    }

    private static boolean cancelIfLimbo(Player player) {
        if (isInLimbo(player.getUUID())) {
            player.displayClientMessage(Component.literal(AuthyConfig.MSG_LIMBO_ACTION_BLOCKED.get()), true);
            return true;
        }
        return false;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (cancelIfLimbo(event.getPlayer())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (cancelIfLimbo(player)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (cancelIfLimbo(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (cancelIfLimbo(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (cancelIfLimbo(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (cancelIfLimbo(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onAttackEntity(AttackEntityEvent event) {
        if (cancelIfLimbo(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onItemToss(ItemTossEvent event) {
        if (cancelIfLimbo(event.getPlayer())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onChat(ServerChatEvent event) {
        if (isInLimbo(event.getPlayer().getUUID())) {
            event.getPlayer().displayClientMessage(Component.literal(AuthyConfig.MSG_LIMBO_ACTION_BLOCKED.get()), false);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onContainerOpen(PlayerContainerEvent.Open event) {
        if (isInLimbo(event.getEntity().getUUID())) {
            event.getEntity().closeContainer();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerAttacked(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (isInLimbo(player.getUUID())) {
                event.setCanceled(true);
            }
        }
    }

    public record PendingDevice(String hwidHash, String clientUUID, String ip) {}
}

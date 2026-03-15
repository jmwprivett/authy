package com.chedd.authy.server;

import com.chedd.authy.config.AuthyConfig;
import com.chedd.authy.data.IdentityManager;
import com.chedd.authy.data.PlayerIdentity;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;

public class AuthyCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("authy")
                .then(Commands.literal("recover")
                        .then(Commands.argument("code", StringArgumentType.string())
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();
                                    String code = StringArgumentType.getString(ctx, "code");
                                    return handleRecover(player, code);
                                })
                        )
                )
                .then(Commands.literal("approve")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> {
                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                    return handleApprove(ctx.getSource(), target);
                                })
                        )
                )
                .then(Commands.literal("revoke")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> {
                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                    return handleRevoke(ctx.getSource(), target);
                                })
                        )
                )
                .then(Commands.literal("regen")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> {
                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
                                    return handleRegen(ctx.getSource(), target);
                                })
                        )
                )
        );
    }

    private static MutableComponent prefix() {
        return Component.literal(AuthyConfig.MSG_PREFIX.get()).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
    }

    private static int handleRecover(ServerPlayer player, String code) {
        if (!LimboManager.isInLimbo(player.getUUID())) {
            player.sendSystemMessage(prefix()
                    .append(Component.literal(AuthyConfig.MSG_ALREADY_VERIFIED.get()).withStyle(ChatFormatting.GREEN)));
            return 0;
        }

        IdentityManager manager = AuthHandler.getIdentityManager();
        if (manager.validateRecoveryCode(player.getStringUUID(), code)) {
            LimboManager.PendingDevice pending = LimboManager.getPendingDevice(player.getUUID());
            if (pending != null) {
                manager.addDevice(player.getStringUUID(), pending.hwidHash(), pending.clientUUID(), pending.ip());
            }
            LimboManager.removeFromLimbo(player.getUUID());

            player.sendSystemMessage(prefix()
                    .append(Component.literal(AuthyConfig.MSG_DEVICE_VERIFIED.get()).withStyle(ChatFormatting.GREEN)));
            return 1;
        } else {
            player.sendSystemMessage(prefix()
                    .append(Component.literal(AuthyConfig.MSG_INVALID_CODE.get()).withStyle(ChatFormatting.RED)));
            return 0;
        }
    }

    private static int handleApprove(CommandSourceStack source, ServerPlayer target) {
        if (!LimboManager.isInLimbo(target.getUUID())) {
            source.sendSuccess(() -> prefix()
                    .append(Component.literal(String.format(AuthyConfig.MSG_NOT_IN_LIMBO.get(), target.getGameProfile().getName())).withStyle(ChatFormatting.YELLOW)), false);
            return 0;
        }

        IdentityManager manager = AuthHandler.getIdentityManager();
        LimboManager.PendingDevice pending = LimboManager.getPendingDevice(target.getUUID());
        if (pending != null) {
            manager.addDevice(target.getStringUUID(), pending.hwidHash(), pending.clientUUID(), pending.ip());
        }
        LimboManager.removeFromLimbo(target.getUUID());

        target.sendSystemMessage(prefix()
                .append(Component.literal(AuthyConfig.MSG_ADMIN_APPROVED.get()).withStyle(ChatFormatting.GREEN)));

        source.sendSuccess(() -> prefix()
                .append(Component.literal("Approved " + target.getGameProfile().getName() + ".").withStyle(ChatFormatting.GREEN)), true);
        return 1;
    }

    private static int handleRevoke(CommandSourceStack source, ServerPlayer target) {
        IdentityManager manager = AuthHandler.getIdentityManager();
        PlayerIdentity identity = manager.getByUuid(target.getStringUUID());
        if (identity == null) {
            source.sendSuccess(() -> prefix()
                    .append(Component.literal(AuthyConfig.MSG_PLAYER_NOT_FOUND.get()).withStyle(ChatFormatting.RED)), false);
            return 0;
        }

        manager.revokeDevices(target.getStringUUID());
        LimboManager.addToLimbo(target.getUUID(), "", "", "");

        target.sendSystemMessage(prefix()
                .append(Component.literal(AuthyConfig.MSG_DEVICES_REVOKED.get()).withStyle(ChatFormatting.RED)));

        source.sendSuccess(() -> prefix()
                .append(Component.literal("Revoked all devices for " + target.getGameProfile().getName() + ".").withStyle(ChatFormatting.YELLOW)), true);
        return 1;
    }

    private static int handleRegen(CommandSourceStack source, ServerPlayer target) {
        IdentityManager manager = AuthHandler.getIdentityManager();
        PlayerIdentity identity = manager.getByUuid(target.getStringUUID());
        if (identity == null) {
            source.sendSuccess(() -> prefix()
                    .append(Component.literal(AuthyConfig.MSG_PLAYER_NOT_FOUND.get()).withStyle(ChatFormatting.RED)), false);
            return 0;
        }

        String newCode = manager.regenerateRecoveryCode(target.getStringUUID());

        MutableComponent codeMsg = prefix()
                .append(Component.literal(String.format(AuthyConfig.MSG_NEW_CODE_GENERATED.get(), target.getGameProfile().getName())).withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(newCode).withStyle(Style.EMPTY
                        .withColor(ChatFormatting.GREEN)
                        .withBold(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, newCode))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to copy")))));

        source.sendSuccess(() -> codeMsg, false);
        return 1;
    }
}

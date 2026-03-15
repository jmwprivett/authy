package com.chedd.authy.client.toast;

import com.chedd.authy.Authy;
import com.chedd.authy.config.AuthyConfig;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AuthyToast implements Toast {
    private static final int PADDING = 7;
    private static final int BG_COLOR = 0xFF2D2D2D;      // dark gray background
    private static final int BORDER_COLOR = 0xFF505050;    // lighter gray border

    private final Component title;
    private final Component message;
    private final Type type;
    private long firstRender = -1;

    public enum Type {
        SUCCESS(0xFF55FF55),   // green
        WARNING(0xFFFFAA00),   // gold
        ERROR(0xFFFF5555),     // red
        INFO(0xFF55FFFF);      // aqua

        public final int color;
        Type(int color) { this.color = color; }
    }

    public AuthyToast(Component title, Component message, Type type) {
        this.title = title;
        this.message = message;
        this.type = type;
    }

    @Override
    public int width() {
        return AuthyConfig.TOAST_WIDTH.get();
    }

    @Override
    public int height() {
        return AuthyConfig.TOAST_HEIGHT.get();
    }

    @Override
    public @NotNull Visibility render(@NotNull GuiGraphics graphics, @NotNull ToastComponent toastComponent, long timeSinceLastVisible) {
        if (firstRender == -1) {
            firstRender = timeSinceLastVisible;
            Authy.LOGGER.debug("Toast showing - title: '{}', message: '{}'", title.getString(), message.getString());
        }

        int w = width();
        int h = height();
        Font font = toastComponent.getMinecraft().font;

        // Solid background
        graphics.fill(0, 0, w, h, BG_COLOR);
        // Outer border using type color (green/gold/etc), 1px
        graphics.fill(0, 0, w, 1, type.color);       // top
        graphics.fill(0, h - 1, w, h, type.color);   // bottom
        graphics.fill(0, 0, 1, h, type.color);        // left
        graphics.fill(w - 1, 0, w, h, type.color);    // right

        // Title at top, scaled 1.3x
        float titleScale = 1.3f;
        graphics.pose().pushPose();
        graphics.pose().scale(titleScale, titleScale, 1.0f);
        int titleX = (int)(PADDING / titleScale);
        int titleY = (int)(PADDING / titleScale);
        graphics.drawString(font, title, titleX, titleY, type.color, false);
        graphics.pose().popPose();

        // Message below title
        int titleHeight = (int)(font.lineHeight * titleScale);
        List<FormattedCharSequence> lines = font.split(message, w - PADDING * 2);
        int y = PADDING + titleHeight + 5;
        for (FormattedCharSequence line : lines) {
            graphics.drawString(font, line, PADDING, y, 0xFFFFFFFF, false);
            y += font.lineHeight + 1;
        }

        return timeSinceLastVisible - firstRender >= AuthyConfig.TOAST_DURATION.get() ? Visibility.HIDE : Visibility.SHOW;
    }
}

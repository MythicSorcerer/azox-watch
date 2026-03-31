package com.azox.watch.broadcast.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public final class MiniMessageUtil {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private MiniMessageUtil() {
    }

    public static Component deserialize(final String input) {
        if (input == null) {
            return Component.empty();
        }
        return MINI_MESSAGE.deserialize(input);
    }
}

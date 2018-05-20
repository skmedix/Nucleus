/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.traits;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.messages.MessageProvider;
import org.spongepowered.api.text.Text;

public interface MessageProviderTrait {

    default MessageProvider getMessageProvider() {
        return Nucleus.getNucleus().getMessageProvider();
    }

    default Text getMessage(String key) {
        return getMessageProvider().getTextMessageWithFormat(key);
    }

    default Text getMessage(String key, String... replacements) {
        return getMessageProvider().getTextMessageWithFormat(key, replacements);
    }

    default Text getMessage(String key, Text... replacements) {
        return getMessageProvider().getTextMessageWithTextFormat(key, replacements);
    }

    default Text getMessage(String key, Object... replacements) {
        return getMessageProvider().getTextMessageWithFormat(key, replacements);
    }
}

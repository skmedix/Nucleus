/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.messages;

import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

public class ResourceMessageProvider extends MessageProvider {

    public static final String messagesBundle = "assets.nucleus.messages";
    public static final String commandMessagesBundle = "assets.nucleus.commands";
    final ResourceBundle rb;

    ResourceMessageProvider(ResourceBundle resource) {
        rb = resource;
    }

    public ResourceMessageProvider(String resource) {
        this(resource, Locale.getDefault());
    }

    public ResourceMessageProvider(String resource, Locale locale) {
        rb = ResourceBundle.getBundle(resource, locale, new UTF8Control());
    }

    @Override
    public Optional<String> getMessageFromKey(String key) {
        if (rb.containsKey(key)) {
            return Optional.of(rb.getString(key));
        }

        return Optional.empty();
    }

    public Set<String> getKeys() {
        return rb.keySet();
    }
}

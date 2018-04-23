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
        this.rb = resource;
    }

    public ResourceMessageProvider(String resource) {
        this.rb = ResourceBundle.getBundle(resource, Locale.getDefault(), new UTF8Control());
    }

    @Override
    public Optional<String> getMessageFromKey(String key) {
        if (this.rb.containsKey(key)) {
            return Optional.of(this.rb.getString(key));
        }

        return Optional.empty();
    }

    public Set<String> getKeys() {
        return this.rb.keySet();
    }
}

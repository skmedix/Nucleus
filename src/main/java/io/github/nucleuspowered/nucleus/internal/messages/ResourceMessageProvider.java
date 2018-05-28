/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.messages;

import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

// TODO: Gut and replace for 1.5/2.0
public class ResourceMessageProvider extends MessageProvider {

    public static final String messagesBundle = "assets.nucleus.messages";
    public static final String commandMessagesBundle = "assets.nucleus.commands";
    private String resource; // effectively final, but the compiler doesn't like this construct.
    ResourceBundle rb;

    ResourceMessageProvider(ResourceBundle resource) {
        this.rb = resource;
    }

    public ResourceMessageProvider(String resource) {
        this(resource, Locale.getDefault());
    }

    public ResourceMessageProvider(String resource, String locale) {
        this(resource, Locale.forLanguageTag(locale));
    }

    ResourceMessageProvider(String resource, Locale locale) {
        this.resource = resource;
        setLocale(locale);
    }

    @Override
    public Optional<String> getMessageFromKey(String key) {
        if (this.rb.containsKey(key)) {
            return Optional.of(this.rb.getString(key));
        }

        return Optional.empty();
    }

    @Override
    public Locale setLocale(Locale locale) {
        this.rb = ResourceBundle.getBundle(this.resource, locale, new UTF8Control());
        return this.rb.getLocale();
    }

    @Override
    public Locale getLocale() {
        Locale locale = this.rb.getLocale();
        if (locale.toLanguageTag().equalsIgnoreCase("und")) {
            return Locale.UK; // that's the base package.
        }

        return locale;
    }

    public Set<String> getKeys() {
        return this.rb.keySet();
    }
}

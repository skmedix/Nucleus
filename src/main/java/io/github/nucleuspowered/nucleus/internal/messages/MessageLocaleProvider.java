/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.messages;

import com.google.common.collect.Maps;
import com.typesafe.config.ConfigException;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.client.ClientMessageReciever;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfigAdapter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.format.TextColors;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public final class MessageLocaleProvider implements Reloadable {

    private final static MessageLocaleProvider MESSAGES_INSTANCE = new MessageLocaleProvider(ResourceMessageProvider.messagesBundle, "messages.conf");
    private final static MessageLocaleProvider COMMAND_INSTANCE = new MessageLocaleProvider(ResourceMessageProvider.commandMessagesBundle,
            "command-help-messages.conf");

    public static void reload() {
        MESSAGES_INSTANCE.onReload();
        COMMAND_INSTANCE.onReload();
    }

    public static MessageProvider messages() {
        return messages(Locale.getDefault());
    }

    public static MessageProvider messages(Locale locale) {
        return MESSAGES_INSTANCE.locale(locale);
    }

    public static MessageProvider messages(CommandSource source) {
        return messages(source.getLocale());
    }

    public static MessageProvider commands() {
        return commands(Locale.getDefault());
    }

    public static MessageProvider commands(Locale locale) {
        return COMMAND_INSTANCE.locale(locale);
    }

    public static MessageProvider commands(CommandSource source) {
        return commands(source.getLocale());
    }

    private boolean useConfig = false;
    private final Map<Locale, MessageProvider> localeMap = Maps.newHashMap();
    private final String bundleName;
    private final ResourceMessageProvider baseBundle;
    private final String filename;

    private MessageLocaleProvider(String bundleName, String s) {
        this.bundleName = bundleName;
        this.baseBundle = new ResourceMessageProvider(ResourceBundle.getBundle(this.bundleName));
        this.filename = s;
        Nucleus.getNucleus().registerReloadable(this);
    }

    private MessageProvider locale(Locale locale) {
        if (this.useConfig) {
            return this.localeMap.computeIfAbsent(locale, l -> {
                ResourceBundle bundle = ResourceBundle.getBundle(this.bundleName, locale, new UTF8Control());
                if (bundle == baseBundle.rb) {
                    return this.baseBundle;
                }

                return new ResourceMessageProvider(this.bundleName, l);
            });
        } else {
            return this.localeMap.computeIfAbsent(Locale.getDefault(), x ->
            {
                try {
                    return new ConfigMessageProvider(Nucleus.getNucleus().getConfigDirPath().resolve(this.filename), this.bundleName);
                } catch (Throwable exception) {
                    // On error, fallback.
                    // Blegh, relocations
                    if (exception instanceof IOException && exception.getCause().getClass().getName().contains(ConfigException.class.getSimpleName())) {
                        MessageReceiver s;
                        if (Sponge.getGame().isServerAvailable()) {
                            s = Sponge.getServer().getConsole();
                        } else {
                            s = new ClientMessageReciever();
                        }

                        exception = exception.getCause();
                        s.sendMessage(Text.of(TextColors.RED, "It appears that there is an error in your " + this.filename + " file! The error is: "));
                        s.sendMessage(Text.of(TextColors.RED, exception.getMessage()));
                        s.sendMessage(Text.of(TextColors.RED, "Please correct this - then run ", TextColors.YELLOW, "/nucleus reload"));
                        s.sendMessage(Text.of(TextColors.RED, "Ignoring " + this.filename + " for now."));
                        if (Nucleus.getNucleus().isDebugMode()) {
                            exception.printStackTrace();
                        }
                    } else {
                        Nucleus.getNucleus().getLogger().warn("Could not load custom messages file. Falling back.");
                        exception.printStackTrace();
                    }

                    return new ResourceMessageProvider(this.bundleName, Locale.getDefault());
                }
            });
        }
    }

    @Override
    public void onReload() {
        boolean custom = Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(CoreConfigAdapter.class)
                .getNodeOrDefault().isCustommessages();
        if (this.useConfig != custom) {
            this.localeMap.clear();
            this.useConfig = custom;
        }
    }
}

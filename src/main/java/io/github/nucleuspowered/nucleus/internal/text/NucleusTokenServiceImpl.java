/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.text;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.PluginInfo;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.exceptions.NucleusException;
import io.github.nucleuspowered.nucleus.api.exceptions.PluginAlreadyRegisteredException;
import io.github.nucleuspowered.nucleus.api.service.NucleusMessageTokenService;
import io.github.nucleuspowered.nucleus.api.text.NucleusTextTemplate;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.Tuple;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

public class NucleusTokenServiceImpl implements NucleusMessageTokenService {

    private static final Pattern suffixPattern = Pattern.compile(":([sp]+)$", Pattern.CASE_INSENSITIVE);
    private final Map<String, TokenParser> tokenStore = Maps.newHashMap();
    private final Map<String, Tuple<TokenParser, String>> primaryTokenStore = Maps.newHashMap();
    private final NucleusPlugin plugin;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType") private final Optional<Text> EMPTY = Optional.empty();

    public NucleusTokenServiceImpl(NucleusPlugin plugin) {
        this.plugin = plugin;
        try {
            PluginContainer pluginContainer = plugin.getPluginContainer();
            register(pluginContainer, Tokens.INSTANCE);
            Tokens.INSTANCE.getTokenNames().forEach(x -> registerPrimaryToken(x.toLowerCase(), pluginContainer, x.toLowerCase()));
        } catch (PluginAlreadyRegisteredException e) {
            e.printStackTrace();
        }
    }

    @Override public void register(PluginContainer pluginContainer, TokenParser textFunction) throws PluginAlreadyRegisteredException {
        Preconditions.checkNotNull(pluginContainer);
        Preconditions.checkNotNull(textFunction);

        if (this.tokenStore.containsKey(pluginContainer.getId())) {
            throw new PluginAlreadyRegisteredException(pluginContainer);
        }

        this.tokenStore.put(pluginContainer.getId(), textFunction);
    }

    @Override public boolean unregister(PluginContainer pluginContainer) {
        Preconditions.checkNotNull(pluginContainer, "pluginContainer");
        Preconditions.checkState(!pluginContainer.getId().equalsIgnoreCase(PluginInfo.ID), "Cannot remove Nucleus tokens");
        TokenParser parser = this.tokenStore.remove(pluginContainer.getId());
        if (parser != null) {
            this.primaryTokenStore.entrySet().removeIf(x -> x.getValue().getFirst().equals(parser));
            return true;
        }

        return false;
    }

    @Override public boolean registerPrimaryToken(String primaryIdentifier, PluginContainer registeringPlugin, String identiferToMapTo) {
        Preconditions.checkArgument(!primaryIdentifier.matches("^.*[\\s|{}:].*$"), "Token cannot contain spaces or \":|{}\"");
        if (this.tokenStore.containsKey(registeringPlugin.getId()) && !this.primaryTokenStore.containsKey(primaryIdentifier.toLowerCase())) {
            // Register!
            this.primaryTokenStore.put(primaryIdentifier.toLowerCase(), Tuple.of(this.tokenStore.get(registeringPlugin.getId()),
                    identiferToMapTo.toLowerCase()));
            return true;
        }

        return false;
    }

    @Override public Optional<TokenParser> getTokenParser(String plugin) {
        Preconditions.checkNotNull(plugin, "pluginContainer");
        return Optional.ofNullable(this.tokenStore.get(plugin.toLowerCase()));
    }

    @Override public Optional<Tuple<TokenParser, String>> getPrimaryTokenParserAndIdentifier(String primaryToken) {
        return Optional.ofNullable(this.primaryTokenStore.get(primaryToken.toLowerCase()));
    }

    @Override public List<String> getPrimaryTokens() {
        return ImmutableList.copyOf(this.primaryTokenStore.keySet());
    }

    @Override public Optional<Text> parseToken(String token, CommandSource source, @Nullable Map<String, Object> variables) {
        return getTextFromToken(token, source, variables);
    }

    @Override public boolean registerTokenFormat(String tokenStart, String tokenEnd, String replacement) throws IllegalArgumentException {
        return NucleusTextTemplateFactory.INSTANCE.registerTokenTranslator(tokenStart, tokenEnd, replacement);
    }

    @Override public NucleusTextTemplate createFromString(String string) throws NucleusException {
        try {
            return NucleusTextTemplateFactory.INSTANCE.create(string);
        } catch (Throwable throwable) {
            throw new NucleusException(Text.of("Error creating template."), throwable, NucleusException.ExceptionType.UNKNOWN_ERROR);
        }
    }

    private Optional<Text> getTextFromToken(String token, CommandSource source, Map<String, Object> variables) {
        token = token.toLowerCase().trim().replace("{{", "").replace("}}", "");
        Matcher m = suffixPattern.matcher(token);
        boolean addSpace = false;
        boolean prependSpace = false;
        if (m.find(0)) {
            String match = m.group(1).toLowerCase();
            addSpace = match.contains("s");
            prependSpace = match.contains("p");

            token = token.replaceAll(":[sp]+$", "");
        }

        try {
            Optional<Text> toReturn;
            if (token.startsWith("pl:") || token.startsWith("p:")) {
                // Plugin identifiers are of the form pl:<pluginid>:<identifier>
                String[] tokSplit = token.split(":", 3);
                if (tokSplit.length < 3) {
                    return this.EMPTY;
                }

                toReturn = applyToken(tokSplit[1], tokSplit[2], source, variables);
            } else if (token.startsWith("o:")) { // Option identifier.
                toReturn = getTextFromOption(source, token.substring(2));
            } else {
                // Standard.
                toReturn = applyPrimaryToken(token, source, variables);
            }

            if (addSpace) {
                toReturn = toReturn.map(x -> x.isEmpty() ? x : Text.join(x, Util.SPACE));
            }

            if (prependSpace) {
                toReturn = toReturn.map(x -> x.isEmpty() ? x : Text.join(Util.SPACE, x));
            }

            return toReturn;
        } catch (Exception e) {
            if (this.plugin.isDebugMode()) {
                e.printStackTrace();
            }

            return this.EMPTY;
        }
    }

    private Optional<Text> getTextFromOption(CommandSource cs, String option) {
        if (cs instanceof Player) {
            return Util.getOptionFromSubject(cs, option).map(TextSerializers.FORMATTING_CODE::deserialize);
        }

        return Optional.empty();
    }

    public Tokens getNucleusTokenParser() {
        return ((Tokens) this.tokenStore.get("nucleus"));
    }
}

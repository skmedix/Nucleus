/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.config;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.config.bases.AbstractStandardNodeConfig;
import io.github.nucleuspowered.nucleus.internal.messages.ResourceMessageProvider;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

public class MessageConfig extends AbstractStandardNodeConfig<CommentedConfigurationNode, HoconConfigurationLoader> {

    private static final Pattern keys = Pattern.compile("\\{(\\d+)}");
    private final ResourceMessageProvider fallback;

    public MessageConfig(Path file, ResourceMessageProvider fallback) throws Exception {
        super(file, Maps.newHashMap(), false);
        Preconditions.checkNotNull(fallback);
        this.fallback = fallback;
        load();
    }

    @Override
    protected CommentedConfigurationNode getDefaults() {
        CommentedConfigurationNode ccn = SimpleCommentedConfigurationNode.root();
        this.fallback.getKeys().forEach(x -> ccn.getNode((Object[])x.split("\\.")).setValue(this.fallback.getMessageFromKey(x).get()));

        return ccn;
    }

    @Override
    protected HoconConfigurationLoader getLoader(Path file) {
        return HoconConfigurationLoader.builder().setPath(file).build();
    }

    public Optional<String> getKey(@Nonnull String key) {
        Preconditions.checkNotNull(key);
        Object[] obj = key.split("\\.");
        return Optional.ofNullable(this.node.getNode(obj).getString());
    }

    public List<String> walkThroughForMismatched() {
        Matcher keyMatcher = keys.matcher("");
        final List<String> keysToFix = Lists.newArrayList();
        this.fallback.getKeys().forEach(x -> {
            String resKey = this.fallback.getMessageFromKey(x).get();
            Optional<String> msgKey = getKey(x);
            if (msgKey.isPresent() && getTokens(resKey, keyMatcher) != getTokens(msgKey.get(), keyMatcher)) {
                keysToFix.add(x);
            }
        });

        return keysToFix;
    }

    public void fixMistmatched(List<String> toFix) throws IOException {
        Preconditions.checkNotNull(toFix);
        toFix.forEach(x -> {
            String resKey = this.fallback.getMessageFromKey(x).get();
            Optional<String> msgKey = getKey(x);

            Object[] nodeKey = x.split("\\.");
            CommentedConfigurationNode cn = this.node.getNode(nodeKey).setValue(resKey);
            msgKey.ifPresent(cn::setComment);
        });

        save();
    }

    private int getTokens(String message, Matcher matcher) {
        int result = -1;

        matcher.reset(message);
        while (matcher.find()) {
            result = Math.max(result, Integer.parseInt(matcher.group(1)));
        }

        return result;
    }
}

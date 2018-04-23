/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.configurate.settingprocessor;

import io.github.nucleuspowered.neutrino.settingprocessor.SettingProcessor;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.util.Tuple;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Sets all keys to their lowercase equivalent.
 */
public class LowercaseMapKeySettingProcessor implements SettingProcessor {

    @Override public void onGet(ConfigurationNode input) {
        Map<Object, ? extends ConfigurationNode> nodes = input.getChildrenMap().entrySet()
                .stream()
                .map(x -> new Tuple<Object, ConfigurationNode>(x.getKey().toString().toLowerCase(), x.getValue()))
                .collect(Collectors.toMap(Tuple::getFirst, Tuple::getSecond));
        input.setValue(nodes);
    }

    @Override
    public void process(ConfigurationNode configurationNode) {}
}

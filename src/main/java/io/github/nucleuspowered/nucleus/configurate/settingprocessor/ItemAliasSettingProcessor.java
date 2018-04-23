/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.configurate.settingprocessor;

import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.neutrino.settingprocessor.SettingProcessor;
import io.github.nucleuspowered.nucleus.configurate.datatypes.ItemDataNode;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * A {@link SettingProcessor} that will make all entries in the item alias list lowercase, and discard invalid
 * entries (replacing spaces with underscores)
 */
public class ItemAliasSettingProcessor implements SettingProcessor {

    private final TypeToken<Set<String>> ttListString = new TypeToken<Set<String>>(String.class) {};
    private final TypeToken<String> ttString = TypeToken.of(String.class);

    @Override
    public void process(ConfigurationNode cn) throws ObjectMappingException {
        if (cn.isVirtual()) {
            return;
        }

        cn.setValue(this.ttListString, cn.getList(this.ttString).stream().map(x -> x.toLowerCase().replace(" ", "_"))
                .filter(x -> ItemDataNode.ALIAS_PATTERN.matcher(x).matches()).collect(Collectors.toSet()));
    }
}

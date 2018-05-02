/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.services;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.PluginInfo;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandMapping;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CommandRemapperService {

    private final Map<String, String> commandremap = new HashMap<>();

    public void addMapping(String newCommand, String remapped) {
        if (this.commandremap.containsKey(newCommand.toLowerCase())) {
            throw new IllegalArgumentException("command already in use");
        }

        this.commandremap.put(newCommand.toLowerCase(), remapped);
    }

    public void activate() {
        for (Map.Entry<String, String> entry : this.commandremap.entrySet()) {
            if (!Sponge.getCommandManager().get(entry.getKey()).isPresent()) {
                Sponge.getCommandManager().get(entry.getValue()).ifPresent(x -> {
                    Sponge.getCommandManager().register(Nucleus.getNucleus(), x.getCallable(), entry.getKey());
                });
            }
        }
    }

    public void deactivate() {
        for (Map.Entry<String, String> entry : this.commandremap.entrySet()) {
            Optional<? extends CommandMapping> mappingOptional = Sponge.getCommandManager().get(entry.getKey());
            if (mappingOptional.isPresent() &&
                    Sponge.getCommandManager().getOwner(mappingOptional.get()).map(x -> x.getId().equals(PluginInfo.ID)).orElse(false)) {
                Sponge.getCommandManager().removeMapping(mappingOptional.get());
            }
        }
    }
}

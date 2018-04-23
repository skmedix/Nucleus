/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.api;

import org.slf4j.Logger;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.plugin.Plugin;

import javax.inject.Inject;

@Plugin(id = NucleusAPITokens.ID, name = NucleusAPITokens.NAME, version = NucleusAPITokens.VERSION, description = NucleusAPITokens.DESCRIPTION)
public final class NucleusAPIMod {

    private final Logger logger;

    @Inject
    public NucleusAPIMod(Logger logger) {
        this.logger = logger;
    }

    @Listener
    public void onPreInit(GamePreInitializationEvent event) {
        this.logger.info("Loading " + NucleusAPITokens.NAME + " for Nucleus version " + NucleusAPITokens.VERSION);
        NucleusAPITokens.onPreInit(this);
    }
}

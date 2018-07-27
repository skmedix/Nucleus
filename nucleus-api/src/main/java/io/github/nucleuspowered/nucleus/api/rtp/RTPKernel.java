/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.api.rtp;

import io.github.nucleuspowered.nucleus.api.service.NucleusRTPService;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.registry.util.PluginProvidedRegistryModule;
import org.spongepowered.api.util.annotation.CatalogedBy;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

import javax.annotation.Nullable;

/**
 * An {@link RTPKernel} provides the instructions for how to select
 * a location when using RTP.
 *
 * <p>RTPKernels can be registered in the appropriate registry.</p>
 *
 * <p>Kernels <em>can</em> base logic on a supplied location, but
 * are expected to handle cases where a {@code null} or invalid
 * location is supplied.</p>
 */
@PluginProvidedRegistryModule
@CatalogedBy(RTPKernels.class)
public interface RTPKernel extends CatalogType {

    /**
     * Gets a new location.
     *
     * <p>Note that this may fail if the search times out. This does not
     * indicate a failing routine.</p>
     *
     * @param currentLocation The current location of the entity to teleport, if appropriate
     * @param target The target world for teleport
     * @param options The options to consider when teleporting
     * @return The location to teleport to, if any.
     */
    Optional<Location<World>> getLocation(
            @Nullable Location<World> currentLocation,
            World target,
            NucleusRTPService.RTPOptions options);
}

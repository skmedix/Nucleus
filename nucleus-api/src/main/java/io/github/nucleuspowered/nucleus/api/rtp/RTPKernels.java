/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.api.rtp;

import org.spongepowered.api.util.generator.dummy.DummyObjectProvider;

/**
 * Nucleus supplied {@link RTPKernel}s.
 *
 * <p><strong>A word of warning</strong>, if the RTP module has not been initialised,
 * this objects will remain as dummy objects and will not function.</p>
 *
 * <p>Check that the {@link io.github.nucleuspowered.nucleus.api.service.NucleusRTPService}
 * exists first before attempting to use these kernels.</p>
 */
public final class RTPKernels {

    private RTPKernels() {} // No instantiation please!

    /**
     * The default Nucleus RTP kernel, adjusted to centre around the player,
     * not the world border centre.
     *
     * <p>This has an ID of {@code nucleus:around_player}</p>
     */
    public final static RTPKernel AROUND_PLAYER = DummyObjectProvider.createFor(RTPKernel.class, "AROUND_PLAYER");

    /**
     * The default Nucleus RTP kernel, adjusted to centre around the player,
     * not the world border centre, and surface only
     *
     * <p>This has an ID of {@code nucleus:around_player_surface}</p>
     */
    public final static RTPKernel AROUND_PLAYER_SURFACE = DummyObjectProvider.createFor(RTPKernel.class, "AROUND_PLAYER_SURFACE");

    /**
     * The default Nucleus RTP kernel.
     *
     * <p>This has an ID of {@code nucleus:default}</p>
     */
    public final static RTPKernel DEFAULT = DummyObjectProvider.createFor(RTPKernel.class, "DEFAULT");

    /**
     * The default Nucleus RTP kernel, adjusted to ensure locations are surface only.
     *
     * <p>This has an ID of {@code nucleus:surface_only}</p>
     */
    public final static RTPKernel SURFACE_ONLY = DummyObjectProvider.createFor(RTPKernel.class, "SURFACE_ONLY");

}

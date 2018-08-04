/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.api.nucleusdata;

import java.time.Duration;
import java.util.Optional;

/**
 * Indicates that there might be a time limit on this entry.
 */
public interface TimedEntry {

    /**
     * The amount of time remaining before this entry expires, if applicable.
     *
     * @return The remaining amount of time.
     */
    Optional<Duration> getRemainingTime();

    /**
     * Returns whether this entry can be considered expired.
     *
     * @return if expired.
     */
    boolean expired();

    /**
     * Denotes whether the timer is currently ticking down (that is, if {@link #getRemainingTime()} should be decreasing with
     * each call.
     *
     * @return <code>true</code> if so.
     */
    boolean isCurrentlyTicking();
}

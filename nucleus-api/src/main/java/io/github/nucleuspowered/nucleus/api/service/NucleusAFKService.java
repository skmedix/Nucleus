/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.api.service;

import io.github.nucleuspowered.nucleus.api.util.NoExceptionAutoClosable;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.plugin.PluginContainer;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Optional;

/**
 * Allows plugins to see a player's AFK status.
 */
public interface NucleusAFKService {

    /**
     * Returns whether the {@link User} in question can go AFK.
     *
     * @param user The {@link User} in question
     * @return Whether or not the player can go AFK.
     */
    boolean canGoAFK(User user);

    /**
     * Returns a collection of players who are currently AFK.
     *
     * @return A {@link Collection} of {@link Player}s
     */
    Collection<Player> getAfk();

    /**
     * Returns whether a player is AFK
     *
     * @param player The player in question.
     * @return Whether the player is AFK.
     */
    boolean isAFK(Player player);

    /**
     * Sets a player's AFK status, if the player can go AFK.
     *
     * @param cause The cause for going AFK. The root cause must be a {@link PluginContainer}.
     * @param player The player to set the status of.
     * @param isAfk Whether the player should go AFK.
     *
     * @return <code>true</code> if successful, otherwise <code>false</code>, usually because the player is exempt from going AFK.
     */
    boolean setAFK(Cause cause, Player player, boolean isAfk);

    /**
     * Returns whether a {@link User} can be kicked for inactivity.
     *
     * @param user The {@link User} in question.
     * @return Whether the player can be kicked.
     */
    boolean canBeKicked(User user);

    /**
     * Returns the last recorded active moment of the player.
     *
     * @param player The player in question
     * @return The {@link Instant}
     */
    Instant lastActivity(Player player);

    /**
     * Returns the {@link Duration} since last recorded active moment of the player.
     *
     * @param player The player in question
     * @return The {@link Instant}
     */
    default Duration timeSinceLastActivity(Player player) {
        return Duration.between(lastActivity(player), Instant.now());
    }

    /**
     * Returns how long the specified {@link User} has to be inactive before going AFK.
     *
     * @param user The {@link User} in question.
     * @return The {@link Duration}, or {@link Optional#empty()} if the player cannot go AFK.
     */
    Optional<Duration> timeForInactivity(User user);

    /**
     * Returns how long the specified {@link User} has to be inactive before being kicked.
     *
     * @param user The {@link User} in question.
     * @return The {@link Duration}, or {@link Optional#empty()} if the player cannot go AFK.
     */
    Optional<Duration> timeForKick(User user);

    /**
     * Invalidates cached permissions, used to resync a player's exemption status.
     */
    void invalidateCachedPermissions();

    /**
     * Forces an activity tracking update for a {@link Player}, such that Nucleus
     * thinks that the player has recently been active and resets their AFK timer.
     *
     * @param player The player to update the activity of.
     */
    void updateActivityForUser(Player player);

    /**
     * Disables activity tracking for the specified {@link Player} for the next tick. See {@link #disableTrackingFor(Player, int)} for more
     * information on how to use this method.
     *
     * @param player Player to disable tracking for.
     * @return The {@link AutoCloseable} that will re-enable the tracking when done.
     * @deprecated Use {@link #disableTrackingForPlayer(Player, int)} instead.
     *
     * @see #disableTrackingFor(Player, int)
     */
    @Deprecated
    default AutoCloseable disableTrackingFor(Player player) {
        return disableTrackingForPlayer(player, 1);
    }

    /**
     * Disables activity tracking for the specified {@link Player} for the next tick. See {@link #disableTrackingFor(Player, int)} for more
     * information on how to use this method.
     *
     * @param player Player to disable tracking for.
     * @return The {@link AutoCloseable} that will re-enable the tracking when done.
     *
     * @see #disableTrackingFor(Player, int)
     */
    default NoExceptionAutoClosable disableTrackingForPlayer(Player player) {
        return disableTrackingForPlayer(player, 1);
    }

    /**
     * Disables activity tracking for the specified {@link Player} for up to the number of ticks specified.
     *
     * <p>
     *     This method returns an {@link AutoCloseable}, and as such, the recommended way of using this method is using "try with resources":
     * </p>
     * <pre>
     *     try (AutoClosable dummy = disableTrackingFor(player, 1)){
     *         // perform actions here, most likely something like:
     *         player.setLocation(location);
     *
     *         // Any actions here will not disable the AFK timer.
     *     }
     *
     *     // any actions here that move the player will be tracked again.
     * </pre>
     * <p>
     *     This pattern isn't strictly required, as the {@link AutoCloseable} will close itself after the specified number of ticks. However, it's
     *     prudent to consider the following:
     * </p>
     * <ul>
     *     <li>
     *         This method will reset the tracking on the main thread. This means if you use this async (though usually, you wouldn't do so), you
     *         will possibly find that the tracking will re-enable before the task finishes on the defaults. Minimise the amount of time the
     *         activity tracking must be disabled, and consider increasing the tick count slightly.
     *     </li>
     *     <li>
     *         There is no need to increase the tick parameter on the main thread. Consider using {@link #disableTrackingFor(Player)} for a sane
     *         default.
     *     </li>
     * </ul>
     *
     * <p>
     *      If you do not use "try with resources", call the <code>close</code> method upon completion to reactivate tracking.
     * </p>
     *
     * @param player The {@link Player} to disable tracking for.
     * @param ticks The number of ticks to disable tracking for.
     * @return The {@link AutoCloseable} that will re-enable the tracking when done.
     * @deprecated Use {@link #disableTrackingForPlayer(Player, int)} instead.
     */
    @Deprecated
    default AutoCloseable disableTrackingFor(Player player, int ticks) {
        return disableTrackingForPlayer(player, ticks);
    }

    /**
     * Disables activity tracking for the specified {@link Player} for up to the number of ticks specified.
     *
     * <p>
     *     This method returns an {@link AutoCloseable}, and as such, the recommended way of using this method is using "try with resources":
     * </p>
     * <pre>
     *     try (AutoClosable dummy = disableTrackingFor(player, 1)){
     *         // perform actions here, most likely something like:
     *         player.setLocation(location);
     *
     *         // Any actions here will not disable the AFK timer.
     *     }
     *
     *     // any actions here that move the player will be tracked again.
     * </pre>
     * <p>
     *     This pattern isn't strictly required, as the {@link AutoCloseable} will close itself after the specified number of ticks. However, it's
     *     prudent to consider the following:
     * </p>
     * <ul>
     *     <li>
     *         This method will reset the tracking on the main thread. This means if you use this async (though usually, you wouldn't do so), you
     *         will possibly find that the tracking will re-enable before the task finishes on the defaults. Minimise the amount of time the
     *         activity tracking must be disabled, and consider increasing the tick count slightly.
     *     </li>
     *     <li>
     *         There is no need to increase the tick parameter on the main thread. Consider using {@link #disableTrackingFor(Player)} for a sane
     *         default.
     *     </li>
     * </ul>
     *
     * <p>
     *      If you do not use "try with resources", call the <code>close</code> method upon completion to reactivate tracking.
     * </p>
     *
     * @param player The {@link Player} to disable tracking for.
     * @param ticks The number of ticks to disable tracking for.
     * @return The {@link AutoCloseable} that will re-enable the tracking when done.
     */
    NoExceptionAutoClosable disableTrackingForPlayer(Player player, int ticks);

}

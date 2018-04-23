/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.warn.events;

import io.github.nucleuspowered.nucleus.api.events.NucleusWarnEvent;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

@NonnullByDefault
public abstract class WarnEvent extends AbstractEvent implements NucleusWarnEvent {

    private final Cause cause;
    private final User targetUser;
    private final String reason;

    private WarnEvent(Cause cause, User targetUser, String reason) {
        this.cause = cause;
        this.targetUser = targetUser;
        this.reason = reason;
    }

    @Override public User getTargetUser() {
        return this.targetUser;
    }

    @Override public Cause getCause() {
        return this.cause;
    }

    @Override public String getReason() {
        return this.reason;
    }

    public static class Warned extends WarnEvent implements NucleusWarnEvent.Warned {

        @Nullable private final Duration expiration;

        public Warned(Cause cause, User targetUser, String reason, @Nullable Duration expiration) {
            super(cause, targetUser, reason);

            this.expiration = expiration;
        }

        @Override public Optional<Duration> getTimeUntilExpiration() {
            return Optional.ofNullable(this.expiration);
        }
    }

    public static class Expire extends WarnEvent implements NucleusWarnEvent.Expired {

        @Nullable private final UUID warner;

        public Expire(Cause cause, User targetUser, String reason, @Nullable UUID warner) {
            super(cause, targetUser, reason);
            this.warner = warner;
        }

        @Override public Optional<UUID> getWarner() {
            return Optional.ofNullable(this.warner);
        }
    }
}

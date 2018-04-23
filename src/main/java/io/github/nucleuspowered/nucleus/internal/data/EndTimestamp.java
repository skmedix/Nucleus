/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.data;

import io.github.nucleuspowered.nucleus.api.nucleusdata.TimedEntry;
import ninja.leaping.configurate.objectmapping.Setting;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public abstract class EndTimestamp implements TimedEntry {

    @Setting
    protected Long endtimestamp;

    @Setting
    protected Long timeFromNextLogin;

    /**
     * Gets the timestamp for the end of the mute.
     *
     * @return An {@link Instant}
     */
    public Optional<Instant> getEndTimestamp() {
        if (this.endtimestamp == null) {
            return Optional.empty();
        }

        return Optional.of(Instant.ofEpochSecond(this.endtimestamp));
    }

    public Optional<Duration> getTimeFromNextLogin() {
        if (this.timeFromNextLogin == null) {
            return Optional.empty();
        }

        return Optional.of(Duration.of(this.timeFromNextLogin, ChronoUnit.SECONDS));
    }

    public void setEndtimestamp(Instant time) {
        this.endtimestamp = time.getEpochSecond();
        this.timeFromNextLogin = null;
    }

    public void setTimeFromNextLogin(Duration duration) {
        this.timeFromNextLogin = duration.getSeconds();
        this.endtimestamp = null;
    }

    public void nextLoginToTimestamp() {
        if (this.timeFromNextLogin != null && this.endtimestamp == null) {
            this.endtimestamp = Instant.now().plus(this.timeFromNextLogin, ChronoUnit.SECONDS).getEpochSecond();
            this.timeFromNextLogin = null;
        }
    }

    @Override public Optional<Duration> getRemainingTime() {
        if (this.endtimestamp == null && this.timeFromNextLogin == null) {
            return Optional.empty();
        }

        if (this.endtimestamp != null) {
            return Optional.of(Duration.between(Instant.now(), Instant.ofEpochSecond(this.endtimestamp)));
        }

        return Optional.of(Duration.of(this.timeFromNextLogin, ChronoUnit.SECONDS));
    }

    @Override public boolean isCurrentlyTicking() {
        return this.endtimestamp != null;
    }
}

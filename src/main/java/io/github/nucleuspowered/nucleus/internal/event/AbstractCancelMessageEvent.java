/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.event;

import io.github.nucleuspowered.nucleus.api.events.CancelMessage;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Optional;

import javax.annotation.Nullable;

@NonnullByDefault
public abstract class AbstractCancelMessageEvent extends AbstractEvent implements CancelMessage {

    private final Cause cause;
    @Nullable private Text cancelMessage = null;
    private boolean cancelled = false;

    protected AbstractCancelMessageEvent(Cause cause) {
        this.cause = cause;
    }

    @Override public Optional<Text> getCancelMessage() {
        return Optional.ofNullable(this.cancelMessage);
    }

    @Override public void setCancelMessage(@Nullable Text message) {
        this.cancelMessage = message;
    }

    @Override public boolean isCancelled() {
        return this.cancelled;
    }

    @Override public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override public Cause getCause() {
        return this.cause;
    }
}

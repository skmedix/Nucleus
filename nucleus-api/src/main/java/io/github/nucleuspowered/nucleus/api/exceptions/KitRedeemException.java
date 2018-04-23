/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.api.exceptions;

import io.github.nucleuspowered.nucleus.api.events.NucleusKitEvent;
import org.spongepowered.api.text.Text;

import java.time.Duration;
import java.util.Optional;

import javax.annotation.Nullable;

/**
 * Indicates that a kit could not be redeemed.
 */
public class KitRedeemException extends Exception {

    private final Reason reason;

    public KitRedeemException(String message, Reason reason) {
        super(message);
        this.reason = reason;
    }

    /**
     * The reason supplied by the redeeming system.
     *
     * @return The reason for failure
     */
    public Reason getReason() {
        return this.reason;
    }

    public static class PreCancelled extends KitRedeemException {

        @Nullable private final Text cancelMessage;

        public PreCancelled(@Nullable Text cancelMessage) {
            super(cancelMessage == null ? "Pre event cancelled" : cancelMessage.toPlain(), Reason.PRE_EVENT_CANCELLED);
            this.cancelMessage = cancelMessage;
        }

        public Optional<Text> getCancelMessage() {
            return Optional.ofNullable(this.cancelMessage);
        }
    }


    public static class Cooldown extends KitRedeemException {

        private final Duration timeLeft;

        public Cooldown(String message, Duration timeLeft) {
            super(message, Reason.COOLDOWN_NOT_EXPIRED);
            this.timeLeft = timeLeft;
        }

        public Duration getTimeLeft() {
            return this.timeLeft;
        }

    }

    public enum Reason {

        /**
         * The one time kit has already been redeemed.
         */
        ALREADY_REDEEMED,

        /**
         * The cooldown has not expired.
         *
         * <p>
         *     This indicates that the exception can be cast to
         *     {@link KitRedeemException.Cooldown} to get the
         *     amount of time left.
         * </p>
         */
        COOLDOWN_NOT_EXPIRED,

        /**
         * There is no space for the items in the kit.
         */
        NO_SPACE,

        /**
         * The {@link NucleusKitEvent.Redeem.Pre} was cancelled.
         *
         * <p>
         *     This indicates that the exception can be cast to
         *     {@link KitRedeemException.PreCancelled} to get the
         *     amount of time left.
         * </p>
         */
        PRE_EVENT_CANCELLED,

        /**
         * An unknown error occurred.
         */
        UNKNOWN

    }
}

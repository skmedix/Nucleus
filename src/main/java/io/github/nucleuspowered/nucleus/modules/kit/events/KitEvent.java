/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.events;

import io.github.nucleuspowered.nucleus.api.events.NucleusKitEvent;
import io.github.nucleuspowered.nucleus.api.nucleusdata.Kit;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.time.Instant;
import java.util.Optional;

import javax.annotation.Nullable;

@NonnullByDefault
public abstract class KitEvent extends AbstractEvent implements NucleusKitEvent {

    public static class Redeem extends KitEvent implements NucleusKitEvent.Redeem {

        private final Cause cause;
        private final Kit kit;
        @Nullable private final Instant lastTime;
        private final Player targetPlayer;

        public Redeem(Cause cause, @Nullable Instant lastTime, Kit kit, Player targetPlayer) {
            this.cause = cause;
            this.kit = kit;
            this.targetPlayer = targetPlayer;
            this.lastTime = lastTime;
        }

        @Override public Optional<Instant> getLastRedeemedTime() {
            return Optional.ofNullable(this.lastTime);
        }

        @Override public String getName() {
            return this.kit.getName();
        }

        @Override public Kit getRedeemedKit() {
            return this.kit;
        }

        @Override public Player getTargetEntity() {
            return this.targetPlayer;
        }

        @Override public Cause getCause() {
            return this.cause;
        }
    }

    public static class PreRedeem extends Redeem implements NucleusKitEvent.Redeem.Pre {

        @Nullable private Text cancelMessage = null;
        private boolean isCancelled;

        public PreRedeem(Cause cause, @Nullable Instant lastTime, Kit kit, Player targetPlayer) {
            super(cause, lastTime, kit, targetPlayer);
        }

        @Override public boolean isCancelled() {
            return this.isCancelled;
        }

        @Override public void setCancelled(boolean cancel) {
            this.isCancelled = cancel;
        }

        @Override public Optional<Text> getCancelMessage() {
            return Optional.ofNullable(this.cancelMessage);
        }

        @Override public void setCancelMessage(@Nullable Text message) {
            this.cancelMessage = message;
        }
    }

    public static class PostRedeem extends Redeem implements NucleusKitEvent.Redeem.Post {

        public PostRedeem(Cause cause, @Nullable Instant lastTime, Kit kit, Player targetPlayer) {
            super(cause, lastTime, kit, targetPlayer);
        }
    }

    public static class FailedRedeem extends Redeem implements NucleusKitEvent.Redeem.Failed {

        public FailedRedeem(Cause cause, @Nullable Instant lastTime, Kit kit, Player targetPlayer) {
            super(cause, lastTime, kit, targetPlayer);
        }
    }
}

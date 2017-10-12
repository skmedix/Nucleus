/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.teleport;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.ImmutableList;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.core.config.SafeTeleportConfig;
import io.github.nucleuspowered.nucleus.modules.teleport.events.AboutToTeleportEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.property.block.PassableProperty;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.TeleportHelper;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import javax.annotation.Nullable;

/**
 * Common class for handling teleport related tasks.
 */
public class NucleusTeleportHandler {

    public static final TeleportHelper TELEPORT_HELPER = Sponge.getGame().getTeleportHelper();

    private static final List<BlockType> unsafeBody = ImmutableList.of(
        BlockTypes.AIR,
        BlockTypes.CACTUS,
        BlockTypes.FIRE,
        BlockTypes.LAVA,
        BlockTypes.FLOWING_LAVA
    );

    public final TeleportMode getTeleportModeForPlayer(Player pl) {
        GameMode gm = pl.getGameModeData().get(Keys.GAME_MODE).orElse(GameModes.SURVIVAL);
        if (gm == GameModes.CREATIVE) {
            // We just need no solid blocks...
            return TeleportMode.WALL_CHECK;
        } else if (gm == GameModes.SPECTATOR) {
            // The walls are welcoming too!
            return TeleportMode.NO_CHECK;
        }

        // If we're flying, the sky is the limit! If not, feet firmly on the ground!
        return TeleportMode.FLYING_THEN_SAFE;
    }

    /**
     * Attempts to teleport a subject using the appropriate {@link TeleportMode} strategy.
     *
     * @param player The {@link Player}
     * @param locationToTeleportTo The {@link Location} in the {@link World} to teleport to.
     * @param safe If {@code true}, try to teleport the subject safely based on their current game mode, if false, do a basic {@link TeleportMode#WALL_CHECK}
     * @return The {@link TeleportResult}
     */
    public TeleportResult teleportPlayer(Player player, Location<World> locationToTeleportTo, boolean safe) {
        return teleportPlayer(player, locationToTeleportTo, player.getRotation(), safe);
    }

    /**
     * Attempts to teleport a subject using a {@link TeleportMode} strategy.
     *
     * @param player The {@link Player}
     * @param worldLocation The {@link Location} in the {@link World} to teleport to.
     * @param rotation The {@link Vector3d} containing the rotation to port to.
     * @param safe If {@code true}, try to teleport the subject safely based on their current game mode, if false, do a basic {@link TeleportMode#WALL_CHECK}
     * @return The {@link TeleportResult}
     */
    public TeleportResult teleportPlayer(Player player, Location<World> worldLocation, Vector3d rotation, boolean safe) {
        TeleportMode mode = safe ? getTeleportModeForPlayer(player) : TeleportMode.WALL_CHECK;
        return teleportPlayer(player, worldLocation, rotation, mode, Cause.of(NamedCause.owner(player)));
    }

    public TeleportResult teleportPlayer(Player player, Transform<World> worldTransform) {
        return teleportPlayer(player, worldTransform.getLocation(), worldTransform.getRotation(), getTeleportModeForPlayer(player), Cause.of(NamedCause.owner(player)));
    }

    public TeleportResult teleportPlayer(Player player, Transform<World> worldTransform, boolean safe) {
        TeleportMode mode = safe ? getTeleportModeForPlayer(player) : TeleportMode.WALL_CHECK;
        return teleportPlayer(player, worldTransform.getLocation(), worldTransform.getRotation(), mode, Cause.of(NamedCause.owner(player)));
    }

    public TeleportResult teleportPlayer(Player player, Transform<World> locationToTeleportTo, TeleportMode teleportMode) {
        return teleportPlayer(player, locationToTeleportTo.getLocation(), locationToTeleportTo.getRotation(), teleportMode, Cause.of(NamedCause.owner(player)));
    }

    public TeleportResult teleportPlayer(Player player, Transform<World> locationToTeleportTo, TeleportMode teleportMode, Cause cause) {
        return teleportPlayer(player, locationToTeleportTo.getLocation(), locationToTeleportTo.getRotation(), teleportMode, cause);
    }

    public TeleportResult teleportPlayer(Player pl, Location<World> loc, TeleportMode mode) {
        return teleportPlayer(pl, loc, mode, Cause.of(NamedCause.owner(pl)));
    }

    public TeleportResult teleportPlayer(Player pl, Location<World> loc, TeleportMode mode, Cause of) {
        return teleportPlayer(pl, loc, pl.getRotation(), mode, of);
    }

    public TeleportResult teleportPlayer(Player pl, Location<World> loc, TeleportMode mode, Cause of, boolean addOffset) {
        return teleportPlayer(pl, loc, pl.getRotation(), mode, of, addOffset);
    }

    public TeleportResult teleportPlayer(Player player, Location<World> locationToTeleportTo, Vector3d rotation, TeleportMode teleportMode,
            Cause cause) {
        return teleportPlayer(player, locationToTeleportTo, rotation, teleportMode, cause, false);
    }

    public TeleportResult teleportPlayer(Player player, Location<World> locationToTeleportTo, Vector3d rotation, TeleportMode teleportMode,
            Cause cause, boolean addOffset) {
        Optional<Location<World>> targetLocation = getSafeLocation(player, locationToTeleportTo, teleportMode);

        if (targetLocation.isPresent() && Util.isLocationInWorldBorder(targetLocation.get())) {
            AboutToTeleportEvent event = new AboutToTeleportEvent(
                    cause,
                    new Transform<>(targetLocation.get().getExtent(), targetLocation.get().getPosition(), rotation),
                    player
            );

            if (Sponge.getEventManager().post(event)) {
                event.getCancelMessage().ifPresent(x -> {
                    Object o = cause.root();
                    if (o instanceof MessageReceiver) {
                        ((MessageReceiver) o).sendMessage(x);
                    }
                });
                return TeleportResult.FAILED_CANCELLED;
            }

            Optional<Entity> oe = player.getVehicle();
            if (oe.isPresent()) {
                player.setVehicle(null);
            }

            // Do it, tell the routine if it worked.
            TeleportResult tr;
            if (addOffset) {
                tr = result(player.setLocationAndRotation(targetLocation.get().add(0.5, 0.5, 0.5), rotation));
            } else {
                tr = result(player.setLocationAndRotation(targetLocation.get(), rotation));
            }

            if (tr.isSuccess()) {
                player.setSpectatorTarget(null);
            } else {
                oe.ifPresent(player::setVehicle);
            }

            return result(player.setLocationAndRotation(targetLocation.get(), rotation));
        }

        return TeleportResult.FAILED_NO_LOCATION;
    }

    public Optional<Location<World>> getSafeLocation(@Nullable Player player, Location<World> locationToTeleportTo, TeleportMode teleportMode) {
        if (player == null && teleportMode == TeleportMode.FLYING_THEN_SAFE) {
            teleportMode = TeleportMode.SAFE_TELEPORT;
        }

        // World around Sponge not loading chunks properly sometimes.
        locationToTeleportTo.getExtent().loadChunk(locationToTeleportTo.getChunkPosition(), true);

        // Find a location to teleport to.
        return teleportMode.apply(player, locationToTeleportTo);
    }

    @SuppressWarnings("all")
    private static boolean isPassable(Location<World> worldLocation, boolean checkSafe) {
        BlockState block = worldLocation.getBlock();
        if (checkSafe && unsafeBody.contains(block.getType())) {
            return false;
        }

        return block.getProperty(PassableProperty.class).map(x -> x.getValue()).orElse(false);
    }

    /**
     * The type of checks to perform when teleporting.
     */
    public enum TeleportMode implements BiFunction<Player, Location<World>, Optional<Location<World>>> {
        /**
         * Teleport without doing any checks.
         */
        NO_CHECK {
            @Override public Optional<Location<World>> apply(Player player, Location<World> location) {
                return Optional.of(location);
            }
        },

        /**
         * Teleport to a safe location for someone who is flying.
         */
        FLYING_THEN_SAFE {
            @Override public Optional<Location<World>> apply(Player player, Location<World> location) {
                if (player.get(Keys.IS_FLYING).orElse(false)) {
                    // If flying, we just need to check they don't end up in a wall or will enter an unsafe block.
                    if (isPassable(location, true) && isPassable(location.add(0, 1, 0), true)) {
                        return Optional.of(location);
                    }
                }

                return SAFE_TELEPORT.apply(player, location);
            }
        },

        /**
         * Teleport to a safe location for someone who is flying, but otherwise check the chunk.
         */
        FLYING_THEN_SAFE_CHUNK {
            @Override public Optional<Location<World>> apply(Player player, Location<World> location) {
                if (player.get(Keys.IS_FLYING).orElse(false)) {
                    // If flying, we just need to check they don't end up in a wall or will enter an unsafe block.
                    if (isPassable(location, true) && isPassable(location.add(0, 1, 0), true)) {
                        return Optional.of(location);
                    }
                }

                return SAFE_TELEPORT_CHUNK.apply(player, location);
            }
        },

        /**
         * Teleport simply checking the walls, and falling back to the full blown safe teleport.
         */
        WALL_CHECK {
            @Override public Optional<Location<World>> apply(Player player, Location<World> location) {
                // Check that the block is not solid.
                if (isPassable(location, false) && isPassable(location.add(0, 1, 0), false)) {
                    return Optional.of(location);
                }

                return SAFE_TELEPORT.apply(player, location);
            }
        },

        /**
         * Perform wall checks, but ascend on failure.
         */
        WALL_CHECK_ASCENDING {
            @Override public Optional<Location<World>> apply(Player player, Location<World> location) {
                Location<World> locationToCheck = location;
                int y = location.getExtent().getBlockMax().getY() - 1;
                while (locationToCheck.getBlockY() < y) {
                    Optional<Location<World>> olw = WALL_CHECK.apply(player, locationToCheck);
                    if (olw.isPresent()) {
                        return olw;
                    }

                    locationToCheck = locationToCheck.add(0, 1, 0);
                }

                return Optional.empty();
            }
        },

        /**
         * Teleport using the Sponge Safe Teleport routine.
         */
        SAFE_TELEPORT {

            private CoreConfigAdapter coreConfigAdapter = null;

            private CoreConfigAdapter getCoreConfigAdapter() throws Exception {
                if (coreConfigAdapter == null) {
                    coreConfigAdapter = Nucleus.getNucleus().getModuleContainer().getConfigAdapterForModule("core", CoreConfigAdapter.class);
                }

                return coreConfigAdapter;
            }

            @Override public Optional<Location<World>> apply(Player player, Location<World> location) {
                SafeTeleportConfig stc;
                try {
                    stc = getCoreConfigAdapter().getNodeOrDefault().getSafeTeleportConfig();
                } catch (Exception e) {
                    stc = new SafeTeleportConfig();
                }

                return TELEPORT_HELPER.getSafeLocation(location, stc.getHeight(), stc.getWidth());
            }
        },

        /**
         * Teleport using the Sponge Safe Teleport routine, but using a chunk radius.
         */
        SAFE_TELEPORT_CHUNK {
            @Override public Optional<Location<World>> apply(Player player, Location<World> location) {
                return TELEPORT_HELPER.getSafeLocation(location, 8, 8);
            }
        },

        /**
         * Perform safe teleport checks, but ascend on failure.
         */
        SAFE_TELEPORT_ASCENDING {
            @Override public Optional<Location<World>> apply(Player player, Location<World> location) {
                return TeleportMode.teleportCheck(player, location, (l, h) -> l.add(0, h, 0), i -> i < location.getExtent().getBlockMax().getY() - 1);
            }
        },

        /**
         * Perform safe teleport checks, but descend to the surface first.
         */
        SAFE_TELEPORT_DESCEND {
            @Override public Optional<Location<World>> apply(Player player, Location<World> location) {
                return TeleportMode.teleportCheck(player, location, (l, h) -> l.sub(0, h, 0), i -> i > 1);
            }
        };

        //

        private static CoreConfigAdapter coreConfigAdapter = null;

        private static CoreConfigAdapter getCoreConfigAdapter() throws Exception {
            if (coreConfigAdapter == null) {
                coreConfigAdapter = Nucleus.getNucleus().getModuleContainer().getConfigAdapterForModule("core", CoreConfigAdapter.class);
            }

            return coreConfigAdapter;
        }

        private static Optional<Location<World>> teleportCheck(
            Player player,
            Location<World> location,
            BiFunction<Location<World>, Integer, Location<World>> locationTransformFunction,
            Predicate<Integer> blockWhileLoop) {

            Location<World> locationToCheck = location;
            int height;
            try {
                height = getCoreConfigAdapter().getNodeOrDefault().getSafeTeleportConfig().getHeight();
            } catch (Exception e) {
                height = TeleportHelper.DEFAULT_HEIGHT;
            }

            while (blockWhileLoop.test(locationToCheck.getBlockY())) {
                Optional<Location<World>> olw = SAFE_TELEPORT.apply(player, locationToCheck);
                if (olw.isPresent()) {
                    return olw;
                }

                // Go up as far as we've already checked.
                locationToCheck = locationTransformFunction.apply(locationToCheck, height);
            }

            return Optional.empty();
        }
    }

    private TeleportResult result(boolean res) {
        return res ? TeleportResult.SUCCESS : TeleportResult.FAILED_CANCELLED;
    }

    public enum TeleportResult {
        /**
         * The teleportation was a success.
         */
        SUCCESS(true),

        /**
         * The teleportation failed due to no safe location.
         */
        FAILED_NO_LOCATION(false),

        /**
         * The teleportation failed due to a plugin cancellation.
         */
        FAILED_CANCELLED(false);

        private final boolean b;

        TeleportResult(boolean b) {
            this.b = b;
        }

        public boolean isSuccess() {
            return b;
        }
    }
}

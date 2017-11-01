/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.rtp.commands;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.Sets;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.argumentparsers.NucleusWorldPropertiesArgument;
import io.github.nucleuspowered.nucleus.internal.CostCancellableTask;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.command.StandardAbstractCommand;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SubjectPermissionCache;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.internal.teleport.NucleusTeleportHandler;
import io.github.nucleuspowered.nucleus.modules.rtp.RTPModule;
import io.github.nucleuspowered.nucleus.modules.rtp.config.RTPConfig;
import io.github.nucleuspowered.nucleus.modules.rtp.config.RTPConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.rtp.events.RTPSelectedLocationEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.data.property.block.MatterProperty;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.PositionOutOfBoundsException;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.util.blockray.BlockRay;
import org.spongepowered.api.util.blockray.BlockRayHit;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.WorldBorder;
import org.spongepowered.api.world.biome.BiomeTypes;
import org.spongepowered.api.world.storage.WorldProperties;
import uk.co.drnaylor.quickstart.config.TypedAbstractConfigAdapter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nullable;

@NonnullByDefault
@Permissions(supportsOthers = true)
@RegisterCommand({"rtp", "randomteleport", "rteleport"})
public class RandomTeleportCommand extends StandardAbstractCommand.SimpleTargetOtherPlayer implements StandardAbstractCommand.Reloadable {

    private RTPConfig rc = new RTPConfig();

    private final String worldKey = "world";

    @Nullable private Set<BlockType> prohibitedTypes = null;
    private Set<String> prohibitedBiomeTypes = Sets.newHashSet(
        BiomeTypes.OCEAN.getId(), BiomeTypes.DEEP_OCEAN.getId(), BiomeTypes.FROZEN_OCEAN.getId()
    );

    // Works around a problem in the Sponge implementation.
    private final Vector3d direction = new Vector3d(0.00001, -1, 0.000001);

    private final Random random = new Random();

    @Override protected Map<String, PermissionInformation> permissionSuffixesToRegister() {
        return new HashMap<String, PermissionInformation>() {{
            put("worlds", PermissionInformation.getWithTranslation("permission.rtp.worlds", SuggestedLevel.ADMIN));
        }};
    }

    @Override public CommandElement[] additionalArguments() {
        return new CommandElement[] {
            GenericArguments.optionalWeak(
                GenericArguments.requiringPermission(
                    new NucleusWorldPropertiesArgument(Text.of(worldKey), NucleusWorldPropertiesArgument.Type.ENABLED_ONLY),
                    permissions.getPermissionWithSuffix("world")
                ))
        };
    }

    @Override
    protected CommandResult executeWithPlayer(SubjectPermissionCache<CommandSource> src, Player player, CommandContext args, boolean self)
            throws Exception {

        // Get the current world.
        final WorldProperties wp;
        if (this.rc.getDefaultWorld().isPresent()) {
            wp = args.<WorldProperties>getOne(worldKey).orElseGet(() -> this.rc.getDefaultWorld().get());
        } else {
            wp = this.getWorldFromUserOrArgs(src.getSubject(), worldKey, args);
        }

        if (rc.isPerWorldPermissions()) {
            String name = wp.getWorldName();
            permissions.checkSuffix(src, "worlds." + name.toLowerCase(), () -> ReturnMessageException.fromKey("command.rtp.worldnoperm", name));
        }

        World currentWorld = Sponge.getServer().loadWorld(wp.getUniqueId()).orElse(null);
        if (currentWorld == null) {
            currentWorld = Sponge.getServer().loadWorld(wp).orElseThrow(() -> ReturnMessageException.fromKey("command.rtp.worldnoload", wp.getWorldName()));
        }

        // World border
        WorldBorder wb = currentWorld.getWorldBorder();

        int diameter = Math.min(Math.abs(rc.getRadius(currentWorld.getName()) * 2), (int)wb.getDiameter());
        Vector3d centre = wb.getCenter();

        src.getSubject().sendMessage(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("command.rtp.searching"));

        if (self) {
            SubjectPermissionCache<Player> sp = new SubjectPermissionCache<>(player, src);
            Sponge.getScheduler().createTaskBuilder().execute(new RTPTask(plugin, centre, diameter, getCost(src, args), sp, currentWorld, rc))
                    .submit(plugin);
        } else {
            Sponge.getScheduler().createTaskBuilder().execute(new RTPTask(plugin, centre, diameter, getCost(src, args), player, currentWorld, src,
                    rc)).submit(plugin);
        }

        return CommandResult.success();
    }

    @Override public void onReload() {
        this.rc = plugin.getConfigAdapter(RTPModule.ID, RTPConfigAdapter.class).map(TypedAbstractConfigAdapter::getNodeOrDefault).orElseGet(RTPConfig::new);
    }

    /*
     * (non-Javadoc)
     *
     * The RTPTask class encapsulates the logic for the /rtp. Because TeleportHelper#getSafeLocation(Location) can be slow, particularly if there is a
     * large area to check, we opt for smaller areas, but to try multiple times. We separate each check by a couple of ticks so that the server
     * still gets to keep ticking, avoiding timeouts and too much lag.
     */
    private class RTPTask extends CostCancellableTask {

        private final Cause cause;
        private final int minY;
        private final int maxY;
        private final SubjectPermissionCache<? extends CommandSource> source;
        private final Player target;
        private int count;
        private final int maxCount;
        private final int diameter;
        private final Vector3d centre;
        private final World currentWorld;
        private final boolean onSurface;
        private boolean isSelf = false;

        private RTPTask(Nucleus plugin, Vector3d centre, int diameter, double cost, SubjectPermissionCache<Player> target,
            World currentWorld, RTPConfig config) {
            this(plugin, centre, diameter, cost, target.getSubject(), currentWorld, target, config);
            isSelf = true;
        }

        private RTPTask(Nucleus plugin, Vector3d centre, int diameter, double cost, Player target, World currentWorld,
                SubjectPermissionCache<? extends CommandSource> source, RTPConfig config) {
            super(plugin, source, cost);
            this.count = Math.max(config.getNoOfAttempts(), 1);
            this.maxCount = this.count;
            this.diameter = diameter;
            this.centre = centre;
            this.currentWorld = currentWorld;

            String name = this.currentWorld.getName();
            this.onSurface = config.isMustSeeSky(name);
            this.minY = config.getMinY(name);
            this.maxY = config.getMaxY(name);
            this.target = target;
            this.source = source;
            this.cause = Cause.of(NamedCause.source(source.getSubject()));
        }

        @Override
        public void accept(Task task) {
            count--;
            if (!target.isOnline()) {
                onCancel();
                return;
            }

            plugin.getLogger().debug(String.format("RTP of %s, attempt %s of %s", target.getName(), maxCount - count, maxCount));

            // Generate random co-ords.
            int x;
            int z;

            int counter = 0;
            do {
                if (counter++ == 10) {
                    onUnsuccesfulAttempt();
                    return;
                }

                x = RandomTeleportCommand.this.random.nextInt(diameter) - diameter/2;
                z = RandomTeleportCommand.this.random.nextInt(diameter) - diameter/2;

                // Load the chunk before continuing with /rtp. Sponge issue means we have to load the chunk first.
                // currentWorld.loadChunk(new Vector3i(x / 16, 0, z / 16), true);
                // Of course, the above line doesn't work. So, let's just inspect the location instead using a method we KNOW will work.
                NucleusTeleportHandler.TELEPORT_HELPER.getSafeLocation(new Location<>(this.currentWorld, x, 0, z), 1, 1);

                // If this is a prohibited type, loop again.
            } while (prohibitedBiomeTypes.contains(this.currentWorld.getBiome(x, 0, z).getId()));

            int y;
            if (onSurface) {
                int startY = this.currentWorld.getBlockMax().getY() - 11;
                OptionalInt oy = getHighest(startY, x, z);

                if (oy.isPresent()) {
                    y = oy.getAsInt();
                    if (y > this.maxY) {
                        onUnsuccesfulAttempt();
                        return;
                    }
                } else {
                    onUnsuccesfulAttempt();
                    return;
                }
            } else {
                // We remove 11 to avoid getting a location too high up for the safe location teleporter to handle.
                y = Math.min(this.currentWorld.getBlockMax().getY() - 11, random.nextInt(maxY - minY + 1) + minY);
            }

            // To get within the world border, add the centre on.
            final Location<World> test = new Location<>(this.currentWorld, new Vector3d(x + centre.getX(), y, z + centre.getZ()));
            Optional<Location<World>> oSafeLocation = Nucleus.getNucleus().getTeleportHandler().getSafeLocation(this.target, test);
                    // NucleusTeleportHandler.TELEPORT_HELPER.getSafeLocation(test, 10, 5);

            // getSafeLocation might have put us out of the world border. Best to check.
            // We also check to see that it's not in water or lava, and if enabled, we see if the subject would end up on the surface.
            try {
                if (oSafeLocation.isPresent()
                    && oSafeLocation.get().getPosition().getY() >= minY
                    && oSafeLocation.get().getPosition().getY() <= maxY
                    && isSafe(oSafeLocation.get()) && Util.isLocationInWorldBorder(oSafeLocation.get())
                    && isOnSurface(oSafeLocation.get())) {

                    if (Sponge.getEventManager().post(new RTPSelectedLocationEvent(
                            oSafeLocation.get(),
                            this.target,
                            this.cause
                    ))) {
                        onUnsuccesfulAttempt();
                        return;
                    }

                    Location<World> tpTarget = oSafeLocation.get();

                    plugin.getLogger().debug(String.format("RTP of %s, found location %s, %s, %s", target.getName(),
                            String.valueOf(tpTarget.getBlockX()),
                            String.valueOf(tpTarget.getBlockY()),
                            String.valueOf(tpTarget.getBlockZ())));
                    if (target.setLocation(tpTarget)) {
                        if (!isSelf) {
                            target.sendMessage(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("command.rtp.other"));
                            source.getSubject().sendMessage(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("command.rtp.successother",
                                target.getName(),
                                String.valueOf(tpTarget.getBlockX()),
                                String.valueOf(tpTarget.getBlockY()),
                                String.valueOf(tpTarget.getBlockZ())));
                        }

                        target.sendMessage(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("command.rtp.success",
                                String.valueOf(tpTarget.getBlockX()),
                                String.valueOf(tpTarget.getBlockY()),
                                String.valueOf(tpTarget.getBlockZ())));
                        return;
                    } else {
                        source.getSubject().sendMessage(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("command.rtp.cancelled"));
                        onCancel();
                        return;
                    }
                }
            } catch (PositionOutOfBoundsException e) {
                // Swallow - we treat it as a fail.
            }

            onUnsuccesfulAttempt();
        }

        private boolean isOnSurface(Location<World> worldLocation) {
            return isSolid(worldLocation.sub(0, 1, 0)) || isSolid(worldLocation.sub(0, 2, 0));
        }

        private void onUnsuccesfulAttempt() {
            if (count <= 0) {
                plugin.getLogger().debug(String.format("RTP of %s was unsuccessful", subject.getSubject().getName()));
                subject.getSubject().sendMessage(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("command.rtp.error"));
                onCancel();
            } else {
                // We're using a scheduler to allow some ticks to go by between attempts to find a
                // safe place.
                Sponge.getScheduler().createTaskBuilder().delayTicks(2).execute(this).submit(plugin);
            }
        }

        private boolean isSafe(Location<World> location) {
            return !location.hasBlock() || !isProhibitedBlockType(location) || !isSolid(location);
        }

        private boolean isProhibitedBlockType(Location<World> location) {
            return getProhibitedTypes().contains(location.getBlockType());
        }

        private Set<BlockType> getProhibitedTypes() {
            if (prohibitedTypes == null) {
                prohibitedTypes = Sets.newHashSet(
                    BlockTypes.WATER,
                    BlockTypes.LAVA,
                    BlockTypes.FLOWING_WATER,
                    BlockTypes.FLOWING_LAVA
                );
            }

            return prohibitedTypes;
        }

        private boolean isSolid(Location<World> location) {
            Optional<MatterProperty> pp = location.getBlockType().getProperty(MatterProperty.class);
            return pp.isPresent() && pp.get().getValue() == MatterProperty.Matter.SOLID;
        }

        @Override
        public void onCancel() {
            super.onCancel();
            if (isSelf) {
                RandomTeleportCommand.this.removeCooldown(target.getUniqueId());
            }
        }

        private OptionalInt getHighest(int startY, int x, int z) {
            int distance = startY - Math.max(this.currentWorld.getBlockMin().getY(), minY);
            if (distance < 0) {
                return OptionalInt.empty();
            }

            BlockRay<World> blockRay = BlockRay
                    .from(new Location<>(currentWorld, new Vector3d(x, Math.min(this.currentWorld.getBlockMax().getY() - 11, maxY), z)))
                    .direction(direction)
                    .distanceLimit(distance)
                    .stopFilter(bl -> bl.getExtent().getBlock(bl.getBlockPosition()).getType().equals(BlockTypes.AIR)).build();
            Optional<BlockRayHit<World>> blockRayHitOptional = blockRay.end();
            return blockRayHitOptional.map(worldBlockRayHit -> OptionalInt.of(worldBlockRayHit.getBlockY())).orElseGet(OptionalInt::empty);

        }
    }
}

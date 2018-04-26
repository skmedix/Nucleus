/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.rtp.commands;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.Sets;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.CostCancellableTask;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.internal.teleport.NucleusTeleportHandler;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfig;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.rtp.RTPModule;
import io.github.nucleuspowered.nucleus.modules.rtp.config.RTPConfig;
import io.github.nucleuspowered.nucleus.modules.rtp.config.RTPConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.rtp.events.RTPSelectedLocationEvent;
import io.github.nucleuspowered.nucleus.util.CauseStackHelper;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.data.property.block.MatterProperty;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.PositionOutOfBoundsException;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.WorldBorder;
import org.spongepowered.api.world.biome.BiomeTypes;
import org.spongepowered.api.world.storage.WorldProperties;
import uk.co.drnaylor.quickstart.config.TypedAbstractConfigAdapter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import javax.annotation.Nullable;

@NonnullByDefault
@Permissions(supportsOthers = true)
@RegisterCommand({"rtp", "randomteleport", "rteleport"})
public class RandomTeleportCommand extends AbstractCommand.SimpleTargetOtherPlayer implements Reloadable {

    private RTPConfig rc = new RTPConfig();
    private int height = 10;

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
                        NucleusParameters.WORLD_PROPERTIES_ENABLED_ONLY, this.permissions.getPermissionWithSuffix("world")
                ))
        };
    }

    @Override
    protected CommandResult executeWithPlayer(CommandSource src, Player player, CommandContext args, boolean self)
            throws Exception {

        // Get the current world.
        final WorldProperties wp;
        if (this.rc.getDefaultWorld().isPresent()) {
            wp = args.<WorldProperties>getOne(this.worldKey).orElseGet(() -> this.rc.getDefaultWorld().get());
        } else {
            wp = this.getWorldFromUserOrArgs(src, this.worldKey, args);
        }

        if (this.rc.isPerWorldPermissions()) {
            String name = wp.getWorldName();
            this.permissions.checkSuffix(src, "worlds." + name.toLowerCase(), () -> ReturnMessageException.fromKey("command.rtp.worldnoperm", name));
        }

        World currentWorld = Sponge.getServer().loadWorld(wp.getUniqueId()).orElse(null);
        if (currentWorld == null) {
            currentWorld = Sponge.getServer().loadWorld(wp).orElseThrow(() -> ReturnMessageException.fromKey("command.rtp.worldnoload", wp.getWorldName()));
        }

        // World border
        WorldBorder wb = currentWorld.getWorldBorder();

        int diameter = Math.min(Math.abs(this.rc.getRadius(currentWorld.getName()) * 2), (int)wb.getDiameter());
        int minDiameter = Math.max(Math.abs(this.rc.getMinRadius(currentWorld.getName()) * 2), 0);
        Vector3d centre = wb.getCenter();

        src.sendMessage(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("command.rtp.searching"));

        if (self) {
            Sponge.getScheduler().createTaskBuilder().execute(
                    new RTPTask(Nucleus.getNucleus(), centre, minDiameter, diameter, getCost(src, args), player, currentWorld, this.rc))
                    .submit(Nucleus.getNucleus());
        } else {
            Sponge.getScheduler().createTaskBuilder().execute(
                    new RTPTask(Nucleus.getNucleus(), centre, minDiameter, diameter, getCost(src, args), player, currentWorld, src, this.rc)).submit(
                    Nucleus.getNucleus());
        }

        return CommandResult.success();
    }

    @Override public void onReload() {
        this.rc = Nucleus.getNucleus().getConfigAdapter(RTPModule.ID, RTPConfigAdapter.class)
                .map(TypedAbstractConfigAdapter::getNodeOrDefault).orElseGet(RTPConfig::new);
        CoreConfig cc = Nucleus.getNucleus().getInternalServiceManager().getService(CoreConfigAdapter.class)
                .map(TypedAbstractConfigAdapter::getNodeOrDefault).orElseGet(CoreConfig::new);
        this.height = cc.getSafeTeleportConfig().getHeight();
    }

    private static NucleusTeleportHandler.TeleportMode wrapMode(NucleusTeleportHandler.TeleportMode nth) {
        return (player, location) -> nth.apply(player, location)
                .filter(x -> location.getBlockY() >= x.getExtent().getHighestYAt(x.getBlockX(), x.getBlockZ()));
    }

    /*
     * (non-Javadoc)
     *
     * The RTPTask class encapsulates the logic for the /rtp. Because TeleportHelper#getSafeLocation(Location) can be slow, particularly if there is a
     * large area to check, we opt for smaller areas, but to try multiple times. We separate each check by a couple of ticks so that the server
     * still gets to keep ticking, avoiding timeouts and too much lag.
     */
    private class RTPTask extends CostCancellableTask {

        private final NucleusTeleportHandler.TeleportMode mode;

        private final Cause cause;
        private final int minY;
        private final int maxY;
        private final CommandSource source;
        private final Player target;
        private int count;
        private final int maxCount;
        private final int minDiameter;
        private final int diameter;
        private final Vector3d centre;
        private final World currentWorld;
        private final boolean onSurface;
        private final boolean aroundPlayer;
        private boolean isSelf = false;

        private RTPTask(Nucleus plugin, Vector3d centre, int minDiameter, int diameter, double cost, Player target,
            World currentWorld, RTPConfig config) {
            this(plugin, centre, minDiameter, diameter, cost, target, currentWorld, target, config);
            this.isSelf = true;
        }

        private RTPTask(Nucleus plugin, Vector3d centre, int minDiameter, int diameter, double cost, Player target, World currentWorld,
                CommandSource source, RTPConfig config) {
            super(plugin, source, cost);
            this.count = Math.max(config.getNoOfAttempts(), 1);
            this.maxCount = this.count;
            this.minDiameter = Math.max(0, minDiameter);
            this.diameter = diameter;
            this.centre = centre;
            this.currentWorld = currentWorld;

            String name = this.currentWorld.getName();
            this.onSurface = config.isMustSeeSky(name);
            this.minY = config.getMinY(name);
            this.maxY = config.getMaxY(name);
            this.target = target;
            this.source = source;
            this.cause = CauseStackHelper.createCause(source);
            this.aroundPlayer = config.isAroundPlayer(name);
            if (this.onSurface) {
                this.mode = NucleusTeleportHandler.StandardTeleportMode.FLYING_THEN_SAFE_TELEPORT_SURFACE;
            } else {
                this.mode = plugin.getTeleportHandler().getTeleportModeForPlayer(this.target);
            }
        }

        @Override
        public void accept(Task task) {
            this.count--;
            if (!this.target.isOnline()) {
                onCancel();
                return;
            }

            Nucleus.getNucleus()
                    .getLogger().debug(String.format("RTP of %s, attempt %s of %s", this.target.getName(), this.maxCount - this.count, this.maxCount));

            // Generate random co-ords.
            int x;
            int z;

            int counter = 0;
            do {
                if (counter++ == 10) {
                    onUnsuccesfulAttempt();
                    return;
                }

                x = RandomTeleportCommand.this.random.nextInt(this.diameter - this.minDiameter) - this.diameter / 2;
                z = RandomTeleportCommand.this.random.nextInt(this.diameter - this.minDiameter) - this.diameter / 2;
                if (this.minDiameter > 0) {
                    x += (x / Math.abs(x)) * this.minDiameter;
                    z += (z / Math.abs(z)) * this.minDiameter;
                }

                if (this.aroundPlayer) {
                    x += this.target.getLocation().getBlockX();
                    z += this.target.getLocation().getBlockZ();
                    if (!Util.isLocationInWorldBorder(new Location<>(this.currentWorld, x, 0, z))) {
                        continue;
                    }
                }

                // Load the chunk before continuing with /rtp. Sponge issue means we have to load the chunk first.
                Optional<Chunk> oc = this.currentWorld.loadChunk(new Vector3i(x / 16, 0, z / 16), true);
                if (!oc.isPresent()) {
                    Sponge.getTeleportHelper().getSafeLocation(new Location<>(this.currentWorld, x, 1, z));
                }

                // If this is a prohibited type, loop again.
            } while (RandomTeleportCommand.this.prohibitedBiomeTypes.contains(this.currentWorld.getBiome(x, 0, z).getId()));

            int y;
            if (this.onSurface && this.currentWorld.getDimension().hasSky()) {
                y = this.currentWorld.getHighestYAt(x, z) + 1;
                if (y < this.minY || y > this.maxY) {
                    onUnsuccesfulAttempt();
                    return;
                }
            } else {
                // We remove 11 to avoid getting a location too high up for the safe location teleporter to handle.
                y = Math.min(this.currentWorld.getBlockMax().getY() - RandomTeleportCommand.this.height - 1,
                        RandomTeleportCommand.this.random.nextInt(this.maxY - this.minY + 1) + this.minY);
            }

            // To get within the world border, add the centre on.
            final Location<World> test = new Location<>(this.currentWorld, new Vector3d(x + this.centre.getX(), y, z + this.centre.getZ()));
            Optional<Location<World>> oSafeLocation =
                    Nucleus.getNucleus().getTeleportHandler().getSafeLocation(this.target, test, this.mode);

            // getSafeLocation might have put us out of the world border. Best to check.
            // We also check to see that it's not in water or lava, and if enabled, we see if the subject would end up on the surface.
            try {
                if (oSafeLocation.isPresent()
                    && oSafeLocation.get().getPosition().getY() >= this.minY
                    && oSafeLocation.get().getPosition().getY() <= this.maxY
                    && !isProhibitedBlockType(oSafeLocation.get().sub(Vector3d.UNIT_Y))
                    && !isProhibitedBlockType(oSafeLocation.get().sub(Vector3d.UNIT_Y).sub(Vector3d.UNIT_Y))
                    && Util.isLocationInWorldBorder(oSafeLocation.get())) {

                    if (Sponge.getEventManager().post(new RTPSelectedLocationEvent(
                            oSafeLocation.get(),
                            this.target,
                            this.cause
                    ))) {
                        onUnsuccesfulAttempt();
                        return;
                    }

                    Location<World> tpTarget = oSafeLocation.get();

                    Nucleus.getNucleus().getLogger().debug(String.format("RTP of %s, found location %s, %s, %s", this.target.getName(),
                            String.valueOf(tpTarget.getBlockX()),
                            String.valueOf(tpTarget.getBlockY()),
                            String.valueOf(tpTarget.getBlockZ())));
                    if (NucleusTeleportHandler.setLocation(this.target, tpTarget)) {
                        if (!this.isSelf) {
                            this.target.sendMessage(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("command.rtp.other"));
                            this.source.sendMessage(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("command.rtp"
                                            + ".successother",
                                    this.target.getName(),
                                String.valueOf(tpTarget.getBlockX()),
                                String.valueOf(tpTarget.getBlockY()),
                                String.valueOf(tpTarget.getBlockZ())));
                        }

                        this.target.sendMessage(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("command.rtp.success",
                                String.valueOf(tpTarget.getBlockX()),
                                String.valueOf(tpTarget.getBlockY()),
                                String.valueOf(tpTarget.getBlockZ())));
                        return;
                    } else {
                        this.source.sendMessage(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("command.rtp.cancelled"));
                        onCancel();
                        return;
                    }
                }
            } catch (PositionOutOfBoundsException e) {
                // Swallow - we treat it as a fail.
            }

            onUnsuccesfulAttempt();
        }

        private void onUnsuccesfulAttempt() {
            if (this.count <= 0) {
                Nucleus.getNucleus().getLogger().debug(String.format("RTP of %s was unsuccessful", this.subject.getName()));
                this.subject.sendMessage(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("command.rtp.error"));
                onCancel();
            } else {
                // We're using a scheduler to allow some ticks to go by between attempts to find a
                // safe place.
                Sponge.getScheduler().createTaskBuilder().delayTicks(2).execute(this).submit(Nucleus.getNucleus());
            }
        }

        private boolean isProhibitedBlockType(Location<World> location) {
            return getProhibitedTypes().contains(location.getBlockType());
        }

        private Set<BlockType> getProhibitedTypes() {
            if (RandomTeleportCommand.this.prohibitedTypes == null) {
                RandomTeleportCommand.this.prohibitedTypes = Sets.newHashSet(
                    BlockTypes.WATER,
                    BlockTypes.LAVA,
                    BlockTypes.FLOWING_WATER,
                    BlockTypes.FLOWING_LAVA
                );
            }

            return RandomTeleportCommand.this.prohibitedTypes;
        }

        private boolean isSolid(Location<World> location) {
            Optional<MatterProperty> pp = location.getBlockType().getProperty(MatterProperty.class);
            return pp.isPresent() && pp.get().getValue() == MatterProperty.Matter.SOLID;
        }

        @Override
        public void onCancel() {
            super.onCancel();
            if (this.isSelf) {
                RandomTeleportCommand.this.removeCooldown(this.target.getUniqueId());
            }
        }
    }
}

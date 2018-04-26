/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.fun.commands;

import com.flowpowered.math.imaginary.Quaterniond;
import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.messages.MessageProvider;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.util.CauseStackHelper;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.OcelotType;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.explosion.Explosion;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;

@Permissions(supportsOthers = true)
@RegisterCommand({"kittycannon", "kc"})
@NonnullByDefault
public class KittyCannonCommand extends AbstractCommand<CommandSource> {

    private final Random random = new Random();
    private final List<OcelotType> ocelotTypes = Lists.newArrayList(Sponge.getRegistry().getAllOf(OcelotType.class));

    @Override protected Map<String, PermissionInformation> permissionSuffixesToRegister() {
        return new HashMap<String, PermissionInformation>() {{
            MessageProvider provider = Nucleus.getNucleus().getMessageProvider();
            put("damage", new PermissionInformation(provider.getMessageWithFormat("permission.kittycannon.damage"), SuggestedLevel.ADMIN));
            put("break", new PermissionInformation(provider.getMessageWithFormat("permission.kittycannon.break"), SuggestedLevel.ADMIN));
            put("fire", new PermissionInformation(provider.getMessageWithFormat("permission.kittycannon.fire"), SuggestedLevel.ADMIN));
        }};
    }

    @Override public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.flags()
                .permissionFlag(this.permissions.getPermissionWithSuffix("damage"), "d", "-damageentities")
                .permissionFlag(this.permissions.getPermissionWithSuffix("break"), "b", "-breakblocks")
                .permissionFlag(this.permissions.getPermissionWithSuffix("fire"), "f", "-fire")
                .buildWith(
                    GenericArguments.optional(
                        GenericArguments.requiringPermission(NucleusParameters.MANY_PLAYER, this.permissions.getOthers())))
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Collection<Player> playerList = args.getAll(NucleusParameters.Keys.PLAYER);
        if (playerList.isEmpty()) {
            if (src instanceof Player) {
                playerList = Lists.newArrayList((Player)src);
            } else {
                throw new ReturnMessageException(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.playeronly"));
            }
        }

        // For each subject, create a kitten, throw it out in the direction of the subject, and make it explode after between 2 and 5 seconds
        playerList.forEach(x -> getACat(src, x, args.hasAny("d"), args.hasAny("b"), args.hasAny("f")));
        return CommandResult.success();
    }

    private void getACat(CommandSource source, Player spawnAt, boolean damageEntities, boolean breakBlocks, boolean causeFire) {
        // Fire it in the direction that the subject is facing with a speed of 0.5 to 3.5, plus the subject's current velocity.
        Vector3d headRotation = spawnAt.getHeadRotation();
        Quaterniond rot = Quaterniond.fromAxesAnglesDeg(headRotation.getX(), -headRotation.getY(), headRotation.getZ());
        Vector3d velocity = spawnAt.getVelocity().add(rot.rotate(Vector3d.UNIT_Z).mul(5 * this.random.nextDouble() + 1));
        World world = spawnAt.getWorld();
        Entity cat = world.createEntity(EntityTypes.OCELOT, spawnAt.getLocation()
            .getPosition().add(0, 1, 0).add(spawnAt.getTransform().getRotationAsQuaternion().getDirection()));
        cat.offer(Keys.OCELOT_TYPE, this.ocelotTypes.get(this.random.nextInt(this.ocelotTypes.size())));

        Sponge.getScheduler().createTaskBuilder().intervalTicks(5).delayTicks(5)
            .execute(new CatTimer(world.getUniqueId(), cat.getUniqueId(), spawnAt, this.random.nextInt(60) + 20, damageEntities, breakBlocks, causeFire))
            .submit(Nucleus.getNucleus());

        CauseStackHelper.createFrameWithCausesWithConsumer(c -> world.spawnEntity(cat), source);
        cat.offer(Keys.VELOCITY, velocity);
    }

    private class CatTimer implements Consumer<Task> {

        private final UUID entity;
        private final UUID world;
        private final Player player;
        private final boolean damageEntities;
        private final boolean causeFire;
        private final boolean breakBlocks;
        private int ticksToDestruction;

        private CatTimer(UUID world, UUID entity, Player player, int ticksToDestruction, boolean damageEntities, boolean breakBlocks, boolean causeFire) {
            this.entity = entity;
            this.ticksToDestruction = ticksToDestruction;
            this.world = world;
            this.player = player;
            this.damageEntities = damageEntities;
            this.breakBlocks = breakBlocks;
            this.causeFire = causeFire;
        }

        @Override public void accept(Task task) {
            Optional<World> oWorld = Sponge.getServer().getWorld(this.world);
            if (!oWorld.isPresent()) {
                task.cancel();
                return;
            }

            Optional<Entity> oe = oWorld.get().getEntity(this.entity);
            if (!oe.isPresent()) {
                task.cancel();
                return;
            }

            Entity e = oe.get();
            if (e.isRemoved()) {
                task.cancel();
                return;
            }

            this.ticksToDestruction -= 5;
            if (this.ticksToDestruction <= 0 || e.isOnGround()) {
                // Cat explodes.
                Explosion explosion = Explosion.builder().location(e.getLocation()).canCauseFire(this.causeFire)
                    .shouldDamageEntities(this.damageEntities).shouldPlaySmoke(true).shouldBreakBlocks(this.breakBlocks)
                    .radius(2).build();
                e.remove();
                CauseStackHelper.createFrameWithCausesWithConsumer(c -> oWorld.get().triggerExplosion(explosion), this.player);

                task.cancel();
            }
        }
    }
}

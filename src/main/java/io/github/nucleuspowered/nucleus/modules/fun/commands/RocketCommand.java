/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.fun.commands;

import com.flowpowered.math.vector.Vector3d;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.argumentparsers.PositiveDoubleArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.explosion.Explosion;

import java.util.concurrent.TimeUnit;

@NonnullByDefault
@RegisterCommand("rocket")
@Permissions(supportsOthers = true, suggestedLevel = SuggestedLevel.ADMIN)
public class RocketCommand extends AbstractCommand<CommandSource> {

    private final String velocity = "velocity";

    @Override
    protected CommandElement[] getArguments() {
        return new CommandElement[] {
                GenericArguments.flags()
                        .flag("h", "-hard")
                        .flag("g", "-g")
                        .valueFlag(new PositiveDoubleArgument(Text.of(this.velocity)), "v", "-velocity")
                        .flag("s", "-silent")
                        .flag("e", "-explosion")
                        .buildWith(NucleusParameters.ONE_PLAYER)
        };
    }

    @Override
    protected CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Player target = getUserFromArgs(Player.class, src, NucleusParameters.Keys.PLAYER, args);
        boolean isSelf = target.equals(src);
        if (!isSelf && !this.permissions.testOthers(src)) {
            throw ReturnMessageException.fromKey("command.rocket.noothers");
        }

        double v = 2;
        if (args.hasAny(this.velocity)) {
            v = args.<Double>getOne(this.velocity).get();
        } else if (args.hasAny("g")) {
            v = 0.5;
        } else if (args.hasAny("h")) {
            v = 4;
        }

        if (args.hasAny("e")) {
            Explosion ex = Explosion.builder()
                    .canCauseFire(false)
                    .location(target.getLocation())
                    .shouldBreakBlocks(false)
                    .shouldPlaySmoke(true)
                    .shouldDamageEntities(false)
                    .radius((float) v * 2.0f)
                    .build();
            ex.getWorld().triggerExplosion(ex);
            Sponge.getScheduler().createSyncExecutor(Nucleus.getNucleus())
                    .schedule(() ->
                                    ex.getWorld().playSound(SoundTypes.ENTITY_FIREWORK_LAUNCH, target.getLocation().getPosition(), 2),
                            500,
                            TimeUnit.MILLISECONDS);
        }

        Vector3d velocity = new Vector3d(0, v, 0);
        target.offer(Keys.VELOCITY, velocity);
        if (!args.hasAny("s")) {
            target.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.rocket.self"));
        }

        if (!isSelf) {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.rocket.other", target.getName()));
        }

        return CommandResult.success();
    }
}

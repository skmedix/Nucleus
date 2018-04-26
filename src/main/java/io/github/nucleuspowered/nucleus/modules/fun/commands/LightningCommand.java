/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.fun.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.util.CauseStackHelper;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.util.blockray.BlockRay;
import org.spongepowered.api.util.blockray.BlockRayHit;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Collection;
import java.util.Optional;

import javax.annotation.Nullable;

@Permissions(supportsOthers = true)
@NonnullByDefault
@RegisterCommand({"lightning", "strike", "smite", "thor", "shock"})
@EssentialsEquivalent(value = {"lightning", "strike", "smite", "thor", "shock"}, isExact = false,
        notes = "Selectors can be used, entities can be struck.")
public class LightningCommand extends AbstractCommand<CommandSource> {

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[]{
                GenericArguments.optional(
                    GenericArguments.requiringPermission(NucleusParameters.MANY_LIVING, this.permissions.getPermissionWithSuffix("others")))
        };
    }

    @Override
    public CommandResult executeCommand(final CommandSource src, CommandContext args) throws Exception {
        Collection<Living> playerCollection = args.getAll(NucleusParameters.Keys.SUBJECT);

        // No argument, let's not smite the subject.
        if (playerCollection.isEmpty()) {
            if (!(src instanceof Player)) {
                src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.playeronly"));
                return CommandResult.empty();
            }

            Player pl = (Player)src;

            // 100 is a good limit here.
            BlockRay<World> playerBlockRay = BlockRay.from(pl).distanceLimit(100).stopFilter(BlockRay.continueAfterFilter(BlockRay.onlyAirFilter(), 1)).build();
            Optional<BlockRayHit<World>> obh = playerBlockRay.end();
            Location<World> lightningLocation;
            // Smite above, but not on.
            lightningLocation = obh.map(BlockRayHit::getLocation).orElseGet(() -> pl.getLocation().add(0, 3, 0));

            return this.spawnLightning(lightningLocation, src, null);
        }

        int successCount = 0;
        for (Living pl : playerCollection) {
            CommandResult cr = this.spawnLightning(
                    pl.getLocation(),
                    src,
                    pl instanceof Player ? (Player)pl : null);
            successCount += cr.getSuccessCount().orElse(0);
        }

        return CommandResult.builder().successCount(successCount).build();
    }

    private CommandResult spawnLightning(Location<World> location, CommandSource src, @Nullable Player target) throws ReturnMessageException {
        World world = location.getExtent();
        Entity bolt = world.createEntity(EntityTypes.LIGHTNING, location.getPosition());

        if (CauseStackHelper.createFrameWithCausesWithReturn(c -> world.spawnEntity(bolt), src)) {
            if (target != null) {
                src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithTextFormat("command.lightning.success.other", Nucleus.getNucleus().getNameUtil()
                        .getName(target)));
            }

            return CommandResult.success();
        }

        if (target != null) {
            throw ReturnMessageException.fromKeyText("command.lightning.errorplayer", Nucleus.getNucleus().getNameUtil().getName(target));
        } else {
            throw ReturnMessageException.fromKey("command.lightning.error");
        }

    }
}

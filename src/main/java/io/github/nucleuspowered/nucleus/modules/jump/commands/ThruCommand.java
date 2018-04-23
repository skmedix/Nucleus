/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.jump.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.internal.teleport.NucleusTeleportHandler;
import io.github.nucleuspowered.nucleus.modules.jump.config.JumpConfigAdapter;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.util.blockray.BlockRay;
import org.spongepowered.api.util.blockray.BlockRayHit;
import org.spongepowered.api.world.World;

@Permissions
@RegisterCommand({"thru", "through"})
@NonnullByDefault
public class ThruCommand extends AbstractCommand<Player> implements Reloadable {

    private int maxThru = 20;

    // Original code taken from EssentialCmds. With thanks to 12AwsomeMan34 for
    // the initial contribution.
    @Override
    public CommandResult executeCommand(Player player, CommandContext args) {
        BlockRay<World> playerBlockRay = BlockRay.from(player).distanceLimit(this.maxThru).build();
        World world = player.getWorld();

        // First, see if we get a wall.
        while (playerBlockRay.hasNext()) {
            // Once we have a wall, we'll break out.
            if (!world.getBlockType(playerBlockRay.next().getBlockPosition()).equals(BlockTypes.AIR)) {
                break;
            }
        }

        // Even if we did find a wall, no good if we are at the end of the ray.
        if (!playerBlockRay.hasNext()) {
            player.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.thru.nowall"));
            return CommandResult.empty();
        }

        do {
            BlockRayHit<World> b = playerBlockRay.next();
            if (player.getWorld().getBlockType(b.getBlockPosition()).equals(BlockTypes.AIR)) {
                if (!Util.isLocationInWorldBorder(b.getLocation())) {
                    player.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.jump.outsideborder"));
                    return CommandResult.empty();
                }

                // If we can go, do so.
                if (Nucleus.getNucleus().getTeleportHandler()
                        .teleportPlayer(player, b.getLocation(), NucleusTeleportHandler.StandardTeleportMode.SAFE_TELEPORT).isSuccess()) {
                    player.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.thru.success"));
                    return CommandResult.success();
                } else {
                    player.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.thru.notsafe"));
                    return CommandResult.empty();
                }
            }
        } while (playerBlockRay.hasNext());

        player.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.thru.nospot"));
        return CommandResult.empty();
    }

    @Override
    public void onReload() {
        this.maxThru = Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(JumpConfigAdapter.class).getNodeOrDefault().getMaxThru();
    }

}

/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.spawn.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.spawn.config.SpawnConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.spawn.datamodules.SpawnGeneralDataModule;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.World;

import java.util.Optional;

@Permissions(suggestedLevel = SuggestedLevel.USER)
@RegisterCommand("firstspawn")
@NonnullByDefault
public class FirstSpawnCommand extends AbstractCommand<Player> implements Reloadable {

    private boolean isSafeTeleport = true;

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) {

        Optional<Transform<World>> olwr = Nucleus.getNucleus().getGeneralService().get(SpawnGeneralDataModule.class).getFirstSpawn();
        if (!olwr.isPresent()) {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.firstspawn.notset"));
            return CommandResult.empty();
        }

        if (Nucleus.getNucleus().getTeleportHandler().teleportPlayer(src, olwr.get(), this.isSafeTeleport, true).isSuccess()) {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.firstspawn.success"));
            return CommandResult.success();
        }

        src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.firstspawn.fail"));
        return CommandResult.empty();
    }


    @Override public void onReload() {
        this.isSafeTeleport = getServiceUnchecked(SpawnConfigAdapter.class).getNodeOrDefault().isSafeTeleport();
    }
}

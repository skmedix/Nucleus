/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.spawn.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.dataservices.modular.ModularWorldService;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.modules.spawn.datamodules.SpawnWorldDataModule;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@RegisterCommand({"setspawn"})
@Permissions
@NoModifiers
@NonnullByDefault
@EssentialsEquivalent("setspawn")
public class SetSpawnCommand extends AbstractCommand<Player> {

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) {
        // Minecraft does not set the rotation of the player at the spawn point, so we'll do it for them!
        ModularWorldService worldService = Nucleus.getNucleus().getWorldDataManager().getWorld(src.getWorld().getUniqueId()).get();
        SpawnWorldDataModule m = worldService.get(SpawnWorldDataModule.class);
        m.setSpawnRotation(src.getRotation());
        worldService.set(m);

        src.getWorld().getProperties().setSpawnPosition(src.getLocation().getBlockPosition());
        src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.setspawn.success", src.getWorld().getName()));
        return CommandResult.success();
    }
}

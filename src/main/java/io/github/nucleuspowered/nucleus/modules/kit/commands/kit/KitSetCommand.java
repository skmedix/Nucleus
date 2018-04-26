/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.commands.kit;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.nucleusdata.Kit;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.kit.commands.KitFallbackBase;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.annotation.NonnullByDefault;

/**
 * Sets kit items.
 *
 * Command Usage: /kit set Permission: plugin.kit.set.base
 */
@Permissions(prefix = "kit", suggestedLevel = SuggestedLevel.ADMIN)
@RegisterCommand(value = {"set", "update", "setFromInventory"}, subcommandOf = KitCommand.class)
@NoModifiers
@NonnullByDefault
public class KitSetCommand extends KitFallbackBase<Player> {

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
            KitFallbackBase.KIT_PARAMETER_NO_PERM_CHECK
        };
    }

    @Override
    public CommandResult executeCommand(final Player player, CommandContext args) {
        Kit kitInfo = args.<Kit>getOne(KIT_PARAMETER_KEY).get();
        kitInfo.updateKitInventory(player);
        KIT_HANDLER.saveKit(kitInfo);
        player.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.kit.set.success", kitInfo.getName()));
        return CommandResult.success();
    }
}

/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.item.commands.itemname;

import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@NonnullByDefault
@RegisterCommand(value = "itemname", hasExecutor = false)
public class ItemNameCommand extends AbstractCommand<Player> {
    @Override
    public CommandResult executeCommand(Player src, CommandContext args) {
        return CommandResult.empty();
    }
}

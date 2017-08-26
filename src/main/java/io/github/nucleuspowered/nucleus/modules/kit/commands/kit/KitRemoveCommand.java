/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.commands.kit;

import io.github.nucleuspowered.nucleus.api.nucleusdata.Kit;
import io.github.nucleuspowered.nucleus.argumentparsers.KitArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.kit.handlers.KitHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.inject.Inject;

/**
 * Sets kit items.
 *
 * Command Usage: /kit remove Permission: plugin.kit.remove.base
 */
@Permissions(prefix = "kit", suggestedLevel = SuggestedLevel.ADMIN)
@RegisterCommand(value = {"remove", "del", "delete"}, subcommandOf = KitCommand.class)
@RunAsync
@NoModifiers
@NonnullByDefault
public class KitRemoveCommand extends AbstractCommand<CommandSource> {

    private final KitHandler kitConfig;
    private final String kit = "kit";

    @Inject
    public KitRemoveCommand(KitHandler kitConfig) {
        this.kitConfig = kitConfig;
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {GenericArguments.onlyOne(new KitArgument(Text.of(kit), false))};
    }

    @Override
    public CommandResult executeCommand(final CommandSource player, CommandContext args) throws Exception {
        Kit kitName = args.<Kit>getOne(kit).get();
        kitConfig.removeKit(kitName.getName());
        player.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.kit.remove.success", kitName.getName()));
        return CommandResult.success();
    }
}

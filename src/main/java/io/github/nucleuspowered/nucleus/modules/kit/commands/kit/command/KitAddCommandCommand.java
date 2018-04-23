/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.commands.kit.command;

import io.github.nucleuspowered.nucleus.api.nucleusdata.Kit;
import io.github.nucleuspowered.nucleus.argumentparsers.KitArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.kit.commands.KitFallbackBase;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@NoModifiers
@NonnullByDefault
@RunAsync
@Permissions(prefix = "kit.command", suggestedLevel = SuggestedLevel.OWNER)
@RegisterCommand(value = {"add", "+"}, subcommandOf = KitCommandCommand.class)
public class KitAddCommandCommand extends KitFallbackBase<CommandSource> {

    private final String command = "command";

    @Override public CommandElement[] getArguments() {
        return new CommandElement[] {
            new KitArgument(Text.of(KIT_PARAMETER), false),
            GenericArguments.remainingRawJoinedStrings(Text.of(command))
        };
    }

    @Override protected CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Kit kitInfo = args.<Kit>getOne(KIT_PARAMETER).get();
        String c = args.<String>getOne(command).get().replace(" {player} ", " {{player}} ");
        kitInfo.addCommand(c);
        KIT_HANDLER.saveKit(kitInfo);

        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.kit.command.add.command", c, kitInfo.getName()));
        return CommandResult.success();
    }
}

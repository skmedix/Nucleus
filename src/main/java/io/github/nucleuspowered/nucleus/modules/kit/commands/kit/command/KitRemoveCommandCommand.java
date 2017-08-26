/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.commands.kit.command;

import io.github.nucleuspowered.nucleus.api.nucleusdata.Kit;
import io.github.nucleuspowered.nucleus.argumentparsers.KitArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.PositiveIntegerArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.RemainingStringsArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.kit.handlers.KitHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.List;

import javax.inject.Inject;

@NoModifiers
@NonnullByDefault
@RunAsync
@Permissions(prefix = "kit.command", suggestedLevel = SuggestedLevel.NONE)
@RegisterCommand(value = {"remove", "del", "-"}, subcommandOf = KitCommandCommand.class)
public class KitRemoveCommandCommand extends AbstractCommand<CommandSource> {

    private final String key = "kit";
    private final String index = "index";
    private final String command = "command";

    private final KitHandler handler;

    @Inject
    public KitRemoveCommandCommand(KitHandler handler) {
        this.handler = handler;
    }

    @Override public CommandElement[] getArguments() {
        return new CommandElement[] {
            new KitArgument(Text.of(key), false),
            GenericArguments.firstParsing(
                new PositiveIntegerArgument(Text.of(index)),
                new RemainingStringsArgument(Text.of(command)))
        };
    }

    @Override protected CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Kit kitInfo = args.<Kit>getOne(key).get();
        List<String> commands = kitInfo.getCommands();

        String cmd;
        if (args.hasAny(index)) {
            int idx = args.<Integer>getOne(index).get();
            if (idx == 0) {
                throw ReturnMessageException.fromKey("command.kit.command.remove.onebased");
            }

            if (idx > commands.size()) {
                throw ReturnMessageException.fromKey("command.kit.command.remove.overidx", String.valueOf(commands.size()), kitInfo.getName());
            }

            cmd = commands.remove(idx - 1);
        } else {
            cmd = args.<String>getOne(command).get().replace(" {player} ", " {{player}} ");
            if (!commands.remove(cmd)) {
                throw ReturnMessageException.fromKey("command.kit.command.remove.noexist", cmd, kitInfo.getName());
            }
        }

        kitInfo.setCommands(commands);
        handler.saveKit(kitInfo);
        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.kit.command.remove.success", cmd, kitInfo.getName()));
        return CommandResult.success();
    }
}

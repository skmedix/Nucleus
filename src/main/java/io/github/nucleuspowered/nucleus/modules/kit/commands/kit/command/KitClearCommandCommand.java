/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.commands.kit.command;

import com.google.common.collect.Lists;
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
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.inject.Inject;

@NoModifiers
@NonnullByDefault
@RunAsync
@Permissions(prefix = "kit.command", mainOverride = "remove", suggestedLevel = SuggestedLevel.NONE)
@RegisterCommand(value = {"clear"}, subcommandOf = KitCommandCommand.class)
public class KitClearCommandCommand extends AbstractCommand<CommandSource> {

    private final String key = "kit";

    private final KitHandler handler;

    @Inject
    public KitClearCommandCommand(KitHandler handler) {
        this.handler = handler;
    }

    @Override public CommandElement[] getArguments() {
        return new CommandElement[] {
            new KitArgument(Text.of(key), false)
        };
    }

    @Override protected CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Kit kitInfo = args.<Kit>getOne(key).get();
        kitInfo.setCommands(Lists.newArrayList());
        handler.saveKit(kitInfo);

        src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.kit.command.clear.command", kitInfo.getName()));
        return CommandResult.success();
    }
}

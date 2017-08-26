/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.commands.kit.command;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.nucleusdata.Kit;
import io.github.nucleuspowered.nucleus.argumentparsers.KitArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.modules.kit.commands.kit.KitCommand;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.List;

@NoModifiers
@NonnullByDefault
@RunAsync
@Permissions(prefix = "kit")
@RegisterCommand(value = {"command", "commands"}, subcommandOf = KitCommand.class)
public class KitCommandCommand extends AbstractCommand<CommandSource> {

    private final String key = "kit";
    private final String removePermission = Nucleus.getNucleus().getPermissionRegistry()
            .getPermissionsForNucleusCommand(KitRemoveCommandCommand.class).getBase();
    private final Text removeIcon = Text.of(TextColors.WHITE, "[", TextColors.DARK_RED, "X", TextColors.WHITE, "]");

    @Override public CommandElement[] getArguments() {
        return new CommandElement[] {
            new KitArgument(Text.of(key), true)
        };
    }

    @Override protected CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        // List all commands on a kit.
        Kit kit = args.<Kit>getOne(key).get();
        List<String> commands = kit.getCommands();

        if (commands.isEmpty()) {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.kit.command.nocommands", kit.getName()));
        } else {
            List<Text> cc = Lists.newArrayList();
            for (int i = 0; i < commands.size(); i++) {
                Text t = plugin.getMessageProvider().getTextMessageWithFormat("command.kit.command.commands.entry", String.valueOf(i + 1), commands.get(i));
                if (src.hasPermission(removePermission)) {
                    t = Text.of(
                            Text.builder().append(removeIcon)
                                .onClick(TextActions.runCommand("/nucleus:kit command remove " + kit.getName() + " " + commands.get(i)))
                                .onHover(TextActions.showText(
                                    plugin.getMessageProvider().getTextMessageWithFormat("command.kit.command.removehover"))).build()
                            , " ", t);
                }

                cc.add(t);
            }

            Util.getPaginationBuilder(src)
                .title(plugin.getMessageProvider().getTextMessageWithFormat("command.kit.command.commands.title", kit.getName()))
                .contents(cc)
                .sendTo(src);
        }

        return CommandResult.success();
    }
}

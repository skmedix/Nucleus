/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.nameban.commands;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.argumentparsers.RegexArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.modules.nameban.config.NameBanConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.nameban.handlers.NameBanHandler;
import io.github.nucleuspowered.nucleus.util.CauseStackHelper;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.stream.Collectors;

@Permissions
@RunAsync
@NoModifiers
@NonnullByDefault
@RegisterCommand("nameban")
public class NameBanCommand extends AbstractCommand<CommandSource> implements Reloadable {

    private final String nameKey = "name";

    private final NameBanHandler handler = getServiceUnchecked(NameBanHandler.class);
    private String defaultReason = "Your name is inappropriate";

    @Override public CommandElement[] getArguments() {
        return new CommandElement[] {
            new RegexArgument(Text.of(this.nameKey), Util.usernameRegexPattern, "command.nameban.notvalid", ((commandSource, commandArgs, commandContext) -> {
                try {
                    String arg = commandArgs.peek().toLowerCase();
                    return Sponge.getServer().getOnlinePlayers().stream().filter(x -> x.getName().toLowerCase().startsWith(arg))
                        .map(User::getName)
                        .collect(Collectors.toList());
                } catch (Exception e) {
                    return Lists.newArrayList();
                }
            })),
            NucleusParameters.OPTIONAL_REASON
        };
    }

    @Override public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        String name = args.<String>getOne(this.nameKey).get().toLowerCase();
        String reason = args.<String>getOne(NucleusParameters.Keys.REASON).orElse(this.defaultReason);

        if (this.handler.addName(name, reason, CauseStackHelper.createCause(src))) {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.nameban.success", name));
            return CommandResult.success();
        }

        throw new ReturnMessageException(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.nameban.failed", name));
    }


    @Override public void onReload() {
        this.defaultReason = getServiceUnchecked(NameBanConfigAdapter.class).getNodeOrDefault().getDefaultReason();
    }
}

/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.jail.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.nucleusdata.NamedLocation;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.messages.MessageProvider;
import io.github.nucleuspowered.nucleus.modules.jail.JailParameters;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Permissions
@RunAsync
@NoModifiers
@RegisterCommand("checkjailed")
@NonnullByDefault
public class CheckJailedCommand extends AbstractCommand<CommandSource> {

    @Override public CommandElement[] getArguments() {
        return new CommandElement[] {
                JailParameters.OPTIONAL_JAIL
        };
    }

    @Override protected CommandResult executeCommand(CommandSource src, CommandContext args) {
        // Using the cache, tell us who is jailed.
        MessageProvider provider = Nucleus.getNucleus().getMessageProvider();
        Optional<NamedLocation> jail = args.getOne(JailParameters.JAIL_KEY);
        List<UUID> usersInJail = jail.map(x -> Nucleus.getNucleus().getUserCacheService().getJailedIn(x.getName()))
                .orElseGet(() -> Nucleus.getNucleus().getUserCacheService().getJailed());
        String jailName = jail.map(NamedLocation::getName).orElseGet(() -> provider.getMessageWithFormat("standard.alljails"));

        if (usersInJail.isEmpty()) {
            src.sendMessage(provider.getTextMessageWithFormat("command.checkjailed.none", jailName));
            return CommandResult.success();
        }

        // Get the users in this jail, or all jails
        Util.getPaginationBuilder(src)
            .title(provider.getTextMessageWithFormat("command.checkjailed.header", jailName))
            .contents(usersInJail.stream().map(x -> {
                Text name = Nucleus.getNucleus().getNameUtil().getName(x).orElseGet(() -> Text.of("unknown: ", x.toString()));
                return name.toBuilder()
                    .onHover(TextActions.showText(provider.getTextMessageWithFormat("command.checkjailed.hover")))
                    .onClick(TextActions.runCommand("/nucleus:checkjail " + x.toString()))
                    .build();
            }).collect(Collectors.toList())).sendTo(src);
        return CommandResult.success();
    }
}

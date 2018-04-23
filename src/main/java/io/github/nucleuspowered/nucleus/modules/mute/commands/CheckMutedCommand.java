/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.mute.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.messages.MessageProvider;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Permissions
@RunAsync
@NoModifiers
@NonnullByDefault
@RegisterCommand("checkmuted")
public class CheckMutedCommand extends AbstractCommand<CommandSource> {

    @Override
    protected CommandResult executeCommand(CommandSource src, CommandContext args) {
        // Using the cache, tell us who is jailed.
        MessageProvider provider = Nucleus.getNucleus().getMessageProvider();
        List<UUID> usersInMute = Nucleus.getNucleus().getUserCacheService().getMuted();

        if (usersInMute.isEmpty()) {
            src.sendMessage(provider.getTextMessageWithFormat("command.checkmuted.none"));
            return CommandResult.success();
        }

        // Get the users in this jail, or all jails
        Util.getPaginationBuilder(src)
            .title(provider.getTextMessageWithFormat("command.checkmuted.header"))
            .contents(usersInMute.stream().map(x -> {
                Text name = Nucleus.getNucleus().getNameUtil().getName(x).orElseGet(() -> Text.of("unknown: ", x.toString()));
                return name.toBuilder()
                    .onHover(TextActions.showText(provider.getTextMessageWithFormat("command.checkmuted.hover")))
                    .onClick(TextActions.runCommand("/nucleus:checkmute " + x.toString()))
                    .build();
            }).collect(Collectors.toList())).sendTo(src);
        return CommandResult.success();
    }
}

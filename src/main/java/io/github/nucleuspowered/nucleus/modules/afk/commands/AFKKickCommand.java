/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.afk.commands;

import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.modules.afk.handlers.AFKHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Collection;
import java.util.Optional;

@NoModifiers
@Permissions
@RegisterCommand({"afkkick", "kickafk"})
public class AFKKickCommand extends AbstractCommand<CommandSource> {

    private final String permission = getPermissionHandlerFor(AFKCommand.class).getPermissionWithSuffix(AFKCommand.KICK_EXEMPT_SUFFIX);

    @Override
    protected CommandElement[] getArguments() {
        return new CommandElement[] {
                NucleusParameters.OPTIONAL_REASON
        };
    }

    @Override
    protected CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Optional<Text> reason = args.<String>getOne(NucleusParameters.Keys.REASON).map(TextSerializers.FORMATTING_CODE::deserialize);

        Collection<Player> playersToKick = getServiceUnchecked(AFKHandler.class).getAfk(x -> !hasPermission(x, this.permission));
        if (playersToKick.isEmpty()) {
            sendMessageTo(src, "command.afkkick.nokick");
            return CommandResult.empty();
        }

        int number = playersToKick.size();
        playersToKick.forEach(x -> x.kick(reason.orElseGet(() -> getMessageFor(src.getLocale(), "afk.kickreason"))));

        sendMessageTo(src, "command.afkkick.success", number);
        return CommandResult.successCount(number);
    }
}

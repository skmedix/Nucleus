/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.warn.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.warn.data.WarnData;
import io.github.nucleuspowered.nucleus.modules.warn.handlers.WarnHandler;
import io.github.nucleuspowered.nucleus.util.CauseStackHelper;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.List;

@Permissions(suggestedLevel = SuggestedLevel.ADMIN)
@RunAsync
@NoModifiers
@RegisterCommand({"clearwarnings", "removeallwarnings"})
@NonnullByDefault
public class ClearWarningsCommand extends AbstractCommand<CommandSource> {

    private final WarnHandler handler = getServiceUnchecked(WarnHandler.class);
    private final String playerKey = "subject";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {GenericArguments.flags().flag("-all", "a").flag("-remove", "r").flag("-expired", "e").buildWith(
                        GenericArguments.onlyOne(GenericArguments.user(Text.of(this.playerKey))))};
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) {
        User user = args.<User>getOne(this.playerKey).get();

        List<WarnData> warnings = this.handler.getWarningsInternal(user);
        if (warnings.isEmpty()) {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.checkwarnings.none", user.getName()));
            return CommandResult.success();
        }

        //By default expire all active warnings.
        //If the flag --all is used then remove all warnings
        //If the flag --expired is used then remove all expired warnings.
        //If the flag --remove is used then remove all active warnings.
        boolean removeActive = false;
        boolean removeExpired = false;
        Text message = Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.clearwarnings.success", user.getName());
        if (args.hasAny("all")) {
            removeActive = true;
            removeExpired = true;
            message = Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.clearwarnings.all", user.getName());
        } else if (args.hasAny("remove")) {
            removeActive = true;
            message = Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.clearwarnings.remove", user.getName());
        } else if (args.hasAny("expired")) {
            removeExpired = true;
            message = Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.clearwarnings.expired", user.getName());
        }

        if (this.handler.clearWarnings(user, removeActive, removeExpired, CauseStackHelper.createCause(src))) {
            src.sendMessage(message);
            return CommandResult.success();
        }

        src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.clearwarnings.failure", user.getName()));
        return CommandResult.empty();
    }
}

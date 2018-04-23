/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.nickname.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.modules.nickname.services.NicknameService;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@NonnullByDefault
@RegisterCommand({"delnick", "delnickname", "deletenick"})
@Permissions(mainOverride = "nick")
public class DelNickCommand extends AbstractCommand<CommandSource> {

    private final NicknameService nicknameService = getServiceUnchecked(NicknameService.class);
    private final String playerKey = "subject";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                GenericArguments.optional(GenericArguments.requiringPermission(GenericArguments.onlyOne(GenericArguments.user(Text.of(this.playerKey))),
                        this.permissions.getPermissionWithSuffix("others")))};
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        User pl = this.getUserFromArgs(User.class, src, this.playerKey, args);
        this.nicknameService.removeNick(pl, src);

        if (!src.equals(pl)) {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.delnick.success.other", pl.getName()));
        }

        return CommandResult.success();
    }
}

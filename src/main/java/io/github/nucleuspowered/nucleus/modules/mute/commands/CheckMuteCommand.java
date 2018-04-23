/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.mute.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.argumentparsers.UUIDArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.mute.data.MuteData;
import io.github.nucleuspowered.nucleus.modules.mute.handler.MuteHandler;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.time.Instant;
import java.util.Optional;

@Permissions(suggestedLevel = SuggestedLevel.MOD)
@RunAsync
@NoModifiers
@RegisterCommand("checkmute")
@NonnullByDefault
public class CheckMuteCommand extends AbstractCommand<CommandSource> {

    private final MuteHandler handler = getServiceUnchecked(MuteHandler.class);
    private final String playerKey = "user/UUID";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.firstParsing(
                GenericArguments.user(Text.of(this.playerKey)),
                    new UUIDArgument<>(Text.of(this.playerKey), u -> Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(u))
            )
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) {
        // Get the user.
        User user = args.<User>getOne(this.playerKey).get();

        Optional<MuteData> omd = this.handler.getPlayerMuteData(user);
        if (!omd.isPresent()) {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.checkmute.none", user.getName()));
            return CommandResult.success();
        }

        // Muted, get information.
        MuteData md = omd.get();
        String name;
        if (!md.getMuter().isPresent()) {
            name = Sponge.getServer().getConsole().getName();
        } else {
            name = Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(md.getMuter().get())
                    .map(User::getName)
                    .orElseGet(() -> Nucleus.getNucleus().getMessageProvider().getMessageWithFormat("standard.unknown"));
        }

        if (md.getRemainingTime().isPresent()) {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.checkmute.mutedfor", user.getName(),
                    name, Util.getTimeStringFromSeconds(md.getRemainingTime().get().getSeconds())));
        } else {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.checkmute.mutedperm", user.getName(),
                    name));
        }

        if (md.getCreationTime() > 0) {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.checkmute.created",
                    Util.FULL_TIME_FORMATTER.withLocale(src.getLocale()).format(Instant.ofEpochSecond(md.getCreationTime()))));
        } else {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.checkmute.created",
                    Nucleus.getNucleus().getMessageProvider().getMessageWithFormat("standard.unknown")));
        }

        src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("standard.reasoncoloured", md.getReason()));
        return CommandResult.success();
    }
}

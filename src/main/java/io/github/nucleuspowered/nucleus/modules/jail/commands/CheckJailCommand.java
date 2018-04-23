/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.jail.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.argumentparsers.UUIDArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.messages.MessageProvider;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.jail.data.JailData;
import io.github.nucleuspowered.nucleus.modules.jail.handlers.JailHandler;
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

@Permissions(prefix = "jail", suggestedLevel = SuggestedLevel.MOD)
@RunAsync
@NoModifiers
@NonnullByDefault
@RegisterCommand({"checkjail"})
public class CheckJailCommand extends AbstractCommand<CommandSource> {

    private final String playerKey = "user/UUID";
    private final JailHandler handler = Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(JailHandler.class);

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
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        User user = args.<User>getOne(this.playerKey).get();
        Optional<JailData> jail = this.handler.getPlayerJailDataInternal(user);
        MessageProvider mp = Nucleus.getNucleus().getMessageProvider();

        if (!jail.isPresent()) {
            throw ReturnMessageException.fromKey("command.checkjail.nojail", user.getName());
        }

        JailData md = jail.get();
        String name;
        if (md.getJailerInternal().equals(Util.consoleFakeUUID)) {
            name = Sponge.getServer().getConsole().getName();
        } else {
            name = Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(md.getJailerInternal())
                    .map(User::getName).orElseGet(() -> mp.getMessageWithFormat("standard.unknown"));
        }

        if (md.getRemainingTime().isPresent()) {
            src.sendMessage(mp.getTextMessageWithFormat("command.checkjail.jailedfor", user.getName(), md.getJailName(),
                    name, Util.getTimeStringFromSeconds(md.getRemainingTime().get().getSeconds())));
        } else {
            src.sendMessage(mp.getTextMessageWithFormat("command.checkjail.jailedperm", user.getName(), md.getJailName(),
                    name));
        }

        if (md.getCreationTime() > 0) {
            src.sendMessage(mp.getTextMessageWithFormat("command.checkjail.created",
                    Util.FULL_TIME_FORMATTER.withLocale(src.getLocale()).format(Instant.ofEpochSecond(md.getCreationTime()))));
        } else {
            src.sendMessage(mp.getTextMessageWithFormat("command.checkjail.created", mp.getMessageWithFormat("standard.unknown")));
        }

        src.sendMessage(mp.getTextMessageWithFormat("standard.reasoncoloured", md.getReason()));
        return CommandResult.success();
    }
}

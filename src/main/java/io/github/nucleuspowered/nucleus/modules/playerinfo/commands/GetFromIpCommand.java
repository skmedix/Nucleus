/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.playerinfo.commands;

import io.github.nucleuspowered.nucleus.NameUtil;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.argumentparsers.RegexArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Permissions
@RegisterCommand("getfromip")
@NonnullByDefault
public class GetFromIpCommand extends AbstractCommand<CommandSource> {

    private final String ipKey = "IP Address";

    @Override public CommandElement[] getArguments() {
        return new CommandElement[] {
            new RegexArgument(Text.of(this.ipKey), "^(\\d{1,3}\\.){3}\\d{1,3}$", "command.getfromip.notvalid")
        };
    }

    @Override protected CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        String ip = args.<String>getOne(this.ipKey).get();
        if (Arrays.stream(ip.split("\\.")).anyMatch(x -> Integer.parseInt(x) > 255)) {
            throw ReturnMessageException.fromKey("command.getfromip.notvalid");
        }

        UserStorageService uss = Sponge.getServiceManager().provideUnchecked(UserStorageService.class);
        List<User> users = Nucleus.getNucleus().getUserCacheService().getForIp(ip).stream().map(uss::get).filter(Optional::isPresent)
                .map(Optional::get).collect(Collectors.toList());

        if (users.isEmpty()) {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.getfromip.nousers"));
            return CommandResult.success();
        }

        NameUtil name = Nucleus.getNucleus().getNameUtil();
        Util.getPaginationBuilder(src).title(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.getfromip.title", ip))
                .contents(
                    users.stream().map(y -> {
                        Text n = name.getName(y);
                        return n.toBuilder().onClick(TextActions.runCommand("/nucleus:seen " + y.getName()))
                            .onHover(TextActions.showText(Nucleus.getNucleus().getMessageProvider().getTextMessageWithTextFormat("command.getfromip.hover", n)))
                            .build();
                    }).collect(Collectors.toList())
                )
                .sendTo(src);
        return CommandResult.success();
    }
}

/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.ban.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.argumentparsers.GameProfileArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.UUIDArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.util.PermissionMessageChannel;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.ban.BanService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MutableMessageChannel;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.util.ban.Ban;

import java.util.Optional;

@RegisterCommand({"unban", "pardon"})
@Permissions(suggestedLevel = SuggestedLevel.MOD)
@NoModifiers
@EssentialsEquivalent({"unban", "pardon"})
@NonnullByDefault
public class UnbanCommand extends AbstractCommand<CommandSource> {

    private final String key = "uuid";
    private final String key2 = "user";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.firstParsing(
                // GenericArguments.onlyOne(GenericArguments.user(Text.of(key))),
                GenericArguments.onlyOne(UUIDArgument.gameProfile(Text.of(this.key))),
                GenericArguments.onlyOne(new GameProfileArgument(Text.of(this.key2)))
            )
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) {
        GameProfile gp;
        if (args.hasAny(this.key)) {
            gp = args.<GameProfile>getOne(this.key).get();
        } else {
            gp = args.<GameProfile>getOne(this.key2).get();
        }

        BanService service = Sponge.getServiceManager().provideUnchecked(BanService.class);

        Optional<Ban.Profile> obp = service.getBanFor(gp);
        if (!obp.isPresent()) {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.checkban.notset", gp.getName().orElse(
                    Nucleus.getNucleus().getMessageProvider().getMessageWithFormat("standard.unknown"))));
            return CommandResult.empty();
        }

        service.removeBan(obp.get());

        MutableMessageChannel notify = new PermissionMessageChannel(BanCommand.notifyPermission).asMutable();
        notify.addMember(src);
        notify.send(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.unban.success", obp.get().getProfile().getName().orElse("standard.unknown"), src.getName()));
        return CommandResult.success();
    }
}

/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.ban.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
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

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.firstParsing(
                    NucleusParameters.ONE_GAME_PROFILE_UUID,
                    NucleusParameters.ONE_GAME_PROFILE
            )
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) {
        GameProfile gp;
        if (args.hasAny(NucleusParameters.Keys.USER_UUID)) {
            gp = args.<GameProfile>getOne(NucleusParameters.Keys.USER_UUID).get();
        } else {
            gp = args.<GameProfile>getOne(NucleusParameters.Keys.USER).get();
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

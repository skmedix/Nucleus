/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.core.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.argumentparsers.RegexArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.UUIDArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.GameProfileManager;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@NoModifiers
@NonnullByDefault
@Permissions(suggestedLevel = SuggestedLevel.NONE)
@RegisterCommand(value = "getuser", subcommandOf = NucleusCommand.class)
public class GetUserCommand extends AbstractCommand<CommandSource> {

    private final String uuidKey = "UUID";
    private final String playerKey = "name";

    @Override public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.firstParsing(
                new UUIDArgument<>(Text.of(this.uuidKey), Optional::ofNullable),
                new RegexArgument(Text.of(this.playerKey), "^[A-Za-z0-9_]{3,16}$", "command.nucleus.getuser.regex")
            )
        };
    }

    @Override protected CommandResult executeCommand(CommandSource src, CommandContext args) {
        CompletableFuture<GameProfile> profile;
        final String toGet;
        final GameProfileManager manager = Sponge.getServer().getGameProfileManager();
        if (args.hasAny(this.uuidKey)) {
            UUID u = args.<UUID>getOne(this.uuidKey).get();
            toGet = u.toString();
            profile = manager.get(u, false);
        } else {
            toGet = args.<String>getOne(this.playerKey).get();
            profile = manager.get(toGet, false);
        }

        src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.nucleus.getuser.starting", toGet));

        profile.handle((gp, th) -> {
            if (th != null || gp == null) {
                if (th != null && Nucleus.getNucleus().isDebugMode()) {
                    th.printStackTrace();
                }

                src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.nucleus.getuser.failed", toGet));
                return 0; // I have to return something, even though I don't care about it.
            }

            // We have a game profile, it's been added to the cache. Create the user too, just in case.
            Sponge.getServiceManager().provideUnchecked(UserStorageService.class).getOrCreate(gp);
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.nucleus.getuser.success",
                    gp.getUniqueId().toString(), gp.getName().orElse("unknown")));

            return 0;
        });


        return CommandResult.success();
    }
}

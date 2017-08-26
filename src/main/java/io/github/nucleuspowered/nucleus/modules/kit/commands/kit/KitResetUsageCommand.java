/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.commands.kit;

import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.nucleusdata.Kit;
import io.github.nucleuspowered.nucleus.argumentparsers.KitArgument;
import io.github.nucleuspowered.nucleus.dataservices.loaders.UserDataManager;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.kit.datamodules.KitUserDataModule;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.inject.Inject;

@Permissions(prefix = "kit", suggestedLevel = SuggestedLevel.ADMIN)
@RegisterCommand(value = {"resetusage", "reset"}, subcommandOf = KitCommand.class)
@RunAsync
@NonnullByDefault
@NoModifiers
public class KitResetUsageCommand extends AbstractCommand<CommandSource> {

    private final UserDataManager userConfigLoader;

    private final String kit = "kit";
    private final String user = "subject";

    @Inject
    public KitResetUsageCommand(UserDataManager userConfigLoader) {
        this.userConfigLoader = userConfigLoader;
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.onlyOne(GenericArguments.user(Text.of(user))),
            GenericArguments.onlyOne(new KitArgument(Text.of(kit), false))
        };
    }

    @Override
    public CommandResult executeCommand(final CommandSource player, CommandContext args) throws Exception {
        Kit kitInfo = args.<Kit>getOne(kit).get();
        User u = args.<User>getOne(user).get();
        KitUserDataModule inu = userConfigLoader.getUnchecked(u).get(KitUserDataModule.class);

        if (Util.getKeyIgnoreCase(inu.getKitLastUsedTime(), kitInfo.getName()).isPresent()) {
            // Remove the key.
            inu.removeKitLastUsedTime(kitInfo.getName().toLowerCase());

            player.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.kit.resetuser.success", u.getName(), kitInfo.getName()));
            return CommandResult.success();
        }

        throw ReturnMessageException.fromKey("command.kit.resetuser.empty", u.getName(), kitInfo.getName());
    }
}

/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.ignore.commands;

import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.ignore.datamodules.IgnoreUserDataModule;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Map;

@RunAsync
@NoModifiers
@RegisterCommand("ignore")
@Permissions(suggestedLevel = SuggestedLevel.USER)
@EssentialsEquivalent("ignore")
@NonnullByDefault
public class IgnoreCommand extends AbstractCommand<Player> {

    @Override
    protected Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> m = Maps.newHashMap();
        m.put("exempt.chat", PermissionInformation.getWithTranslation("permission.ignore.chat", SuggestedLevel.MOD));
        return m;
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                NucleusParameters.ONE_USER,
                NucleusParameters.OPTIONAL_ONE_TRUE_FALSE
        };
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) {
        // Get the target
        User target = args.<User>getOne(NucleusParameters.Keys.USER).get();

        if (target.equals(src)) {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.ignore.self"));
            return CommandResult.empty();
        }

        IgnoreUserDataModule inu = Nucleus.getNucleus().getUserDataManager().getUnchecked(src).get(IgnoreUserDataModule.class);

        if (this.permissions.testSuffix(target, "exempt.chat")) {
            // Make sure they are removed.
            inu.removeFromIgnoreList(target.getUniqueId());
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.ignore.exempt", target.getName()));
            return CommandResult.empty();
        }

        // Ok, we can ignore or unignore them.
        boolean ignore = args.<Boolean>getOne(NucleusParameters.Keys.BOOL).orElse(!inu.getIgnoreList().contains(target.getUniqueId()));

        if (ignore) {
            inu.addToIgnoreList(target.getUniqueId());
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.ignore.added", target.getName()));
        } else {
            inu.removeFromIgnoreList(target.getUniqueId());
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.ignore.remove", target.getName()));
        }

        return CommandResult.success();
    }
}

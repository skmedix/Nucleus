/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.commands.kit;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.nucleusdata.Kit;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.kit.commands.KitFallbackBase;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.time.Duration;

@Permissions(prefix = "kit", suggestedLevel = SuggestedLevel.ADMIN)
@RegisterCommand(value = {"setcooldown", "setinterval"}, subcommandOf = KitCommand.class)
@RunAsync
@NoModifiers
@NonnullByDefault
public class KitSetCooldownCommand extends KitFallbackBase<CommandSource> {

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                KitFallbackBase.KIT_PARAMETER_NO_PERM_CHECK,
                NucleusParameters.DURATION
        };
    }

    @Override
    public CommandResult executeCommand(final CommandSource player, CommandContext args) {
        Kit kitInfo = args.<Kit>getOne(KIT_PARAMETER_KEY).get();
        long seconds = args.<Long>getOne(NucleusParameters.Keys.DURATION).get();

        kitInfo.setCooldown(Duration.ofSeconds(seconds));
        KIT_HANDLER.saveKit(kitInfo);
        player.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.kit.setcooldown.success",
                kitInfo.getName(), Util.getTimeStringFromSeconds(seconds)));
        return CommandResult.success();
    }
}

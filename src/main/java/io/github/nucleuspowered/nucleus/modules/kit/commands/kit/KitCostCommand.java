/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.commands.kit;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.nucleusdata.Kit;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.kit.commands.KitFallbackBase;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

/**
 * Sets kit cost.
 */
@Permissions(prefix = "kit", suggestedLevel = SuggestedLevel.ADMIN)
@RegisterCommand(value = {"cost", "setcost"}, subcommandOf = KitCommand.class)
@RunAsync
@NoModifiers
@NonnullByDefault
public class KitCostCommand extends KitFallbackBase<CommandSource> {

    private final String costKey = "cost";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                KitFallbackBase.KIT_PARAMETER_NO_PERM_CHECK,
                GenericArguments.onlyOne(GenericArguments.doubleNum(Text.of(this.costKey)))
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) {
        Kit kit = args.<Kit>getOne(KIT_PARAMETER_KEY).get();
        double cost = args.<Double>getOne(this.costKey).get();

        if (cost < 0) {
            cost = 0;
        }

        kit.setCost(cost);
        KIT_HANDLER.saveKit(kit);
        src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.kit.cost.success", kit.getName(), String.valueOf(cost)));
        return CommandResult.success();
    }
}

/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.commands.kit;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.kit.commands.KitFallbackBase;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

/**
 * Sets kit items.
 */
@Permissions(prefix = "kit", suggestedLevel = SuggestedLevel.ADMIN)
@RegisterCommand(value = {"add", "createFromInventory"}, subcommandOf = KitCommand.class)
@NoModifiers
@NonnullByDefault
public class KitAddCommand extends KitFallbackBase<Player> {

    private final String name = "name";

    @Override protected boolean allowFallback(CommandSource source, CommandArgs args, CommandContext context) {
        if (context.hasAny(this.name)) {
            return false;
        }
        return super.allowFallback(source, args, context);
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                GenericArguments.onlyOne(GenericArguments.string(Text.of(this.name)))
        };
    }

    @Override
    public CommandResult executeCommand(final Player player, CommandContext args) throws Exception {
        String kitName = args.<String>getOne(this.name).get();

        if (KIT_HANDLER.getKitNames().stream().noneMatch(kitName::equalsIgnoreCase)) {
            KIT_HANDLER.saveKit(KIT_HANDLER.createKit(kitName).updateKitInventory(player));
            player.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.kit.add.success", kitName));
            return CommandResult.success();
        } else {
            throw ReturnMessageException.fromKey("command.kit.add.alreadyexists", kitName);
        }
    }
}

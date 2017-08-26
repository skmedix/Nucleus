/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.commands.kit;

import io.github.nucleuspowered.nucleus.api.nucleusdata.Kit;
import io.github.nucleuspowered.nucleus.argumentparsers.KitArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.kit.handlers.KitHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import javax.inject.Inject;

@Permissions(prefix = "kit", suggestedLevel = SuggestedLevel.ADMIN)
@RegisterCommand(value = {"toggleredeemmessage", "togglemessage"}, subcommandOf = KitCommand.class)
@RunAsync
@NoModifiers
@NonnullByDefault
public class KitRedeemMessageCommand extends AbstractCommand<CommandSource> {

    private final KitHandler handler;
    private final String toggle = "displayMessageToggle";
    private final String kit = "kit";

    @Inject
    public KitRedeemMessageCommand(KitHandler handler) {
        this.handler = handler;
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                GenericArguments.seq(GenericArguments.onlyOne(new KitArgument(Text.of(kit), true)),
                        GenericArguments.onlyOne(GenericArguments.bool(Text.of(toggle))))
        };
    }

    @Override
    public CommandResult executeCommand(final CommandSource player, CommandContext args) throws Exception {
        Kit kitInfo = args.<Kit>getOne(kit).get();
        boolean b = args.<Boolean>getOne(toggle).get();

        // This Kit is a reference back to the version in list, so we don't need
        // to update it explicitly
        kitInfo.setDisplayMessageOnRedeem(b);
        handler.saveKit(kitInfo);
        player.sendMessage(plugin.getMessageProvider()
                .getTextMessageWithFormat(b ? "command.kit.displaymessage.on" : "command.kit.displaymessage.off", kitInfo.getName()));

        return CommandResult.success();
    }
}

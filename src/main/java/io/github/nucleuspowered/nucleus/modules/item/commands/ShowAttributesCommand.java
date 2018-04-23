/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.item.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.messages.MessageProvider;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@NonnullByDefault
@RegisterCommand({"showitemattributes", "showattributes"})
public class ShowAttributesCommand extends AbstractCommand<Player> {

    private final String flag = "toggle";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                GenericArguments.optional(GenericArguments.bool(Text.of(this.flag)))
        };
    }

    @Override
    protected CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        ItemStack itemStack = src.getItemInHand(HandTypes.MAIN_HAND)
                .orElseThrow(() -> ReturnMessageException.fromKey("command.generalerror.handempty"));

        boolean b = args.<Boolean>getOne(this.flag).orElseGet(() -> itemStack.get(Keys.HIDE_ATTRIBUTES).orElse(false));

        // Command is show, key is hide. We invert.
        itemStack.offer(Keys.HIDE_ATTRIBUTES, !b);
        src.setItemInHand(HandTypes.MAIN_HAND, itemStack);

        MessageProvider mp = Nucleus.getNucleus().getMessageProvider();
        src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithTextFormat("command.showitemattributes.success." + String.valueOf(b),
                Text.of(itemStack)));

        return CommandResult.success();
    }

}

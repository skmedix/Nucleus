/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.item.commands.itemname;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.messages.MessageProvider;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Optional;

@Permissions(prefix = "itemname")
@RegisterCommand(value = "clear", subcommandOf = ItemNameCommand.class, rootAliasRegister = { "clearitemname", "resetitemname" })
@NonnullByDefault
public class ItemNameClearCommand extends AbstractCommand<Player> {

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        MessageProvider provider = Nucleus.getNucleus().getMessageProvider();
        if (!src.getItemInHand(HandTypes.MAIN_HAND).isPresent()) {
            throw ReturnMessageException.fromKey("command.itemname.clear.noitem");
        }

        ItemStack stack = src.getItemInHand(HandTypes.MAIN_HAND).get();
        Optional<Text> data = stack.get(Keys.DISPLAY_NAME);

        if (!data.isPresent()) {
            // No display name.
            throw ReturnMessageException.fromKey("command.lore.clear.none");
        }

        if (stack.remove(Keys.DISPLAY_NAME).isSuccessful()) {
            src.setItemInHand(HandTypes.MAIN_HAND, stack);
            src.sendMessage(provider.getTextMessageWithFormat("command.itemname.clear.success"));
            return CommandResult.success();
        }

        throw ReturnMessageException.fromKey("command.itemname.clear.fail");
    }
}

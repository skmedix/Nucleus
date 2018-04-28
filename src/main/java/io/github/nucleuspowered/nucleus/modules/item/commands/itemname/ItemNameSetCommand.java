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
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@Permissions(prefix = "itemname")
@NonnullByDefault
@RegisterCommand(value = "set", subcommandOf = ItemNameCommand.class, rootAliasRegister = { "setitemname", "renameitem" })
public class ItemNameSetCommand extends AbstractCommand<Player> {

    private final String nameKey = "name";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                GenericArguments.remainingJoinedStrings(Text.of(this.nameKey))
        };
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        MessageProvider provider = Nucleus.getNucleus().getMessageProvider();
        if (!src.getItemInHand(HandTypes.MAIN_HAND).isPresent()) {
            throw ReturnMessageException.fromKey("command.itemname.set.noitem");
        }

        ItemStack stack = src.getItemInHand(HandTypes.MAIN_HAND).get();
        Text name = TextSerializers.FORMATTING_CODE.deserialize(args.<String>getOne(this.nameKey).get());

        if (stack.offer(Keys.DISPLAY_NAME, name).isSuccessful()) {
            src.setItemInHand(HandTypes.MAIN_HAND, stack);

            src.sendMessage(provider.getTextMessageWithFormat("command.itemname.set.success"));
            return CommandResult.success();
        }

        throw ReturnMessageException.fromKey("command.itemname.set.fail");
    }

}

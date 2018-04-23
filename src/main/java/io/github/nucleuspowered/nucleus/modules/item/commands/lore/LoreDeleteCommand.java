/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.item.commands.lore;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.argumentparsers.PositiveIntegerArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.item.LoreData;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.List;

@Permissions(prefix = "lore", mainOverride = "set")
@NonnullByDefault
@RegisterCommand(value = "delete", subcommandOf = LoreCommand.class)
public class LoreDeleteCommand extends AbstractCommand<Player> {

    private final String loreLine = "line";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                new PositiveIntegerArgument(Text.of(this.loreLine), false)
        };
    }

    @Override
    protected CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        int line = args.<Integer>getOne(this.loreLine).get() - 1;

        ItemStack stack = src.getItemInHand(HandTypes.MAIN_HAND).orElseThrow(() -> ReturnMessageException.fromKey("command.lore.clear.noitem"));
        LoreData loreData = stack.getOrCreate(LoreData.class).get();

        List<Text> loreList = loreData.lore().get();
        if(loreList.size() < line){
            throw ReturnMessageException.fromKey("command.lore.set.invalidLine");
        }

        loreList.remove(line);

        if (stack.offer(Keys.ITEM_LORE, loreList).isSuccessful()) {
            src.setItemInHand(HandTypes.MAIN_HAND, stack);

            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.lore.set.success"));
            return CommandResult.success();
        }

        throw ReturnMessageException.fromKey("command.lore.set.fail");
    }
}

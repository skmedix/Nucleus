/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.item.commands.lore;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.mutable.item.LoreData;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.List;

@NonnullByDefault
abstract class LoreSetBaseCommand extends AbstractCommand<Player> {

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                NucleusParameters.LORE
        };
    }

    CommandResult setLore(Player src, String message, boolean replace) throws Exception{
        ItemStack stack = src.getItemInHand(HandTypes.MAIN_HAND).orElseThrow(() -> ReturnMessageException.fromKey("command.lore.set.noitem"));
        LoreData loreData = stack.getOrCreate(LoreData.class).get();

        Text getLore = TextSerializers.FORMATTING_CODE.deserialize(message);

        List<Text> loreList;
        if (replace) {
            loreList = Lists.newArrayList(getLore);
        } else {
            loreList = loreData.lore().get();
            loreList.add(getLore);
        }

        if (stack.offer(Keys.ITEM_LORE, loreList).isSuccessful()) {
            src.setItemInHand(HandTypes.MAIN_HAND, stack);

            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.lore.set.success"));
            return CommandResult.success();
        }

        throw ReturnMessageException.fromKey("command.lore.set.fail");
    }
}

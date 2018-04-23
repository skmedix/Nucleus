/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.powertool.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.modules.powertool.datamodules.PowertoolUserDataModule;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.List;
import java.util.Optional;

@Permissions(mainOverride = "powertool")
@RunAsync
@NoModifiers
@NonnullByDefault
@RegisterCommand(value = {"delete", "del", "rm", "remove"}, subcommandOf = PowertoolCommand.class)
public class DeletePowertoolCommand extends AbstractCommand<Player> {


    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        Optional<ItemStack> itemStack = src.getItemInHand(HandTypes.MAIN_HAND);
        if (!itemStack.isPresent()) {
            throw ReturnMessageException.fromKey("command.powertool.noitem");
        }

        PowertoolUserDataModule user = Nucleus.getNucleus().getUserDataManager().getUnchecked(src).get(PowertoolUserDataModule.class);
        ItemType item = itemStack.get().getType();

        Optional<List<String>> cmds = user.getPowertoolForItem(item);
        if (cmds.isPresent() && !cmds.get().isEmpty()) {
            user.clearPowertool(item);
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithTextFormat("command.powertool.removed", Text.of(item)));
        } else {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithTextFormat("command.powertool.nocmds", Text.of(item)));
        }

        return CommandResult.success();
    }
}

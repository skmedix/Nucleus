/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.powertool.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.messages.MessageProvider;
import io.github.nucleuspowered.nucleus.modules.powertool.datamodules.PowertoolUserDataModule;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Permissions(mainOverride = "powertool")
@RunAsync
@NoModifiers
@RegisterCommand(value = {"list", "ls"}, subcommandOf = PowertoolCommand.class)
@NonnullByDefault
public class ListPowertoolCommand extends AbstractCommand<Player> {

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) {
        PowertoolUserDataModule inu = Nucleus.getNucleus().getUserDataManager().getUnchecked(src).get(PowertoolUserDataModule.class);

        boolean toggle = inu.isPowertoolToggled();
        Map<String, List<String>> powertools = inu.getPowertools();

        if (powertools.isEmpty()) {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.powertool.list.none"));
            return CommandResult.success();
        }

        // Generate powertools.
        List<Text> mesl = powertools.entrySet().stream().sorted((a, b) -> a.getKey().compareToIgnoreCase(b.getKey()))
                .map(k -> from(inu, src, k.getKey(), k.getValue())).collect(Collectors.toList());

        // Paginate the tools.
        Util.getPaginationBuilder(src).title(Nucleus.getNucleus().getMessageProvider()
                .getTextMessageWithFormat("command.powertool.list.header", toggle ? "&aenabled" : "&cdisabled"))
                .padding(Text.of(TextColors.YELLOW, "-")).contents(mesl).sendTo(src);

        return CommandResult.success();
    }

    private Text from(final PowertoolUserDataModule inu, Player src, String powertool, List<String> commands) {
        Optional<ItemType> oit = Sponge.getRegistry().getType(ItemType.class, powertool);

        MessageProvider mp = Nucleus.getNucleus().getMessageProvider();

        // Create the click actions.
        ClickAction viewAction = TextActions.executeCallback(pl -> Util.getPaginationBuilder(src)
                .title(mp.getTextMessageWithFormat("command.powertool.ind.header", powertool))
                .padding(Text.of(TextColors.GREEN, "-"))
                .contents(commands.stream().map(x -> Text.of(TextColors.YELLOW, x)).collect(Collectors.toList())).sendTo(src));

        ClickAction deleteAction = TextActions.executeCallback(pl -> {
            inu.clearPowertool(powertool);
            pl.sendMessage(mp.getTextMessageWithFormat("command.powertool.removed", powertool));
        });

        TextColor tc = oit.map(itemType -> TextColors.YELLOW).orElse(TextColors.GRAY);

        // id - [View] - [Delete]
        return Text.builder().append(Text.of(tc, powertool)).append(Text.of(" - "))
                .append(Text.builder(mp.getMessageWithFormat("standard.view")).color(TextColors.YELLOW).onClick(viewAction).build())
                .append(Text.of(" - "))
                .append(Text.builder(mp.getMessageWithFormat("standard.delete")).color(TextColors.DARK_RED).onClick(deleteAction).build())
                .build();
    }
}

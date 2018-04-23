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
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.messages.MessageProvider;
import io.github.nucleuspowered.nucleus.modules.powertool.datamodules.PowertoolUserDataModule;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@Permissions(mainOverride = "powertool")
@RunAsync
@NoModifiers
@RegisterCommand(value = {"toggle"}, subcommandOf = PowertoolCommand.class)
@NonnullByDefault
@EssentialsEquivalent({"powertooltoggle", "ptt", "pttoggle"})
public class TogglePowertoolCommand extends AbstractCommand<Player> {

    private final String toggleKey = "toggle";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.bool(Text.of(this.toggleKey))))};
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) {
        PowertoolUserDataModule user = Nucleus.getNucleus().getUserDataManager().getUnchecked(src).get(PowertoolUserDataModule.class);

        // If specified - get the key. Else, the inverse of what we have now.
        boolean toggle = args.<Boolean>getOne(this.toggleKey).orElse(!user.isPowertoolToggled());
        user.setPowertoolToggle(toggle);

        MessageProvider mp = Nucleus.getNucleus().getMessageProvider();
        src.sendMessage(mp.getTextMessageWithFormat("command.powertool.toggle",
                mp.getMessageWithFormat(toggle ? "standard.enabled" : "standard.disabled")));
        return CommandResult.success();
    }
}

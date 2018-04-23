/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.mute.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.modules.mute.handler.MuteHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@RunAsync
@NoModifiers
@NonnullByDefault
@Permissions
@RegisterCommand("globalmute")
public class GlobalMuteCommand extends AbstractCommand<CommandSource> {

    private final String on = "turn on";
    private final MuteHandler muteHandler = getServiceUnchecked(MuteHandler.class);

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.optional(GenericArguments.bool(Text.of(this.on)))
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) {
        boolean turnOn = args.<Boolean>getOne(this.on).orElse(!this.muteHandler.isGlobalMuteEnabled());

        this.muteHandler.setGlobalMuteEnabled(turnOn);
        String onOff = Nucleus.getNucleus().getMessageProvider().getMessageFromKey(turnOn ? "standard.enabled" : "standard.disabled").get();
        src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.globalmute.status", onOff));
        MessageChannel.TO_ALL.send(
                Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.globalmute.broadcast." + (turnOn ? "enabled" : "disabled")));

        return CommandResult.success();
    }
}

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
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.mute.handler.MuteHandler;
import io.github.nucleuspowered.nucleus.util.PermissionMessageChannel;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MutableMessageChannel;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Map;
import java.util.UUID;

@RunAsync
@NoModifiers
@Permissions(prefix = "globalmute")
@RegisterCommand("voice")
@NonnullByDefault
public class VoiceCommand extends AbstractCommand<CommandSource> {

    private final String on = "turn on";
    private final String player = "subject";

    private final MuteHandler muteHandler = getServiceUnchecked(MuteHandler.class);

    @Override
    protected Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> m = super.permissionSuffixesToRegister();
        m.put("auto", PermissionInformation.getWithTranslation("permission.voice.auto", SuggestedLevel.ADMIN));
        m.put("notify", PermissionInformation.getWithTranslation("permission.voice.notify", SuggestedLevel.ADMIN));
        return m;
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                GenericArguments.player(Text.of(this.player)),
                GenericArguments.optional(GenericArguments.bool(Text.of(this.on)))
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) {
        if (!this.muteHandler.isGlobalMuteEnabled()) {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.voice.globaloff"));
            return CommandResult.empty();
        }

        Player pl = args.<Player>getOne(this.player).get();
        if (this.permissions.testSuffix(pl, "auto")) {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.voice.autovoice", pl.getName()));
            return CommandResult.empty();
        }

        boolean turnOn = args.<Boolean>getOne(this.on).orElse(!this.muteHandler.isVoiced(pl.getUniqueId()));

        UUID voice = pl.getUniqueId();
        if (turnOn == this.muteHandler.isVoiced(voice)) {
            if (turnOn) {
                src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.voice.alreadyvoiced", pl.getName()));
            } else {
                src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.voice.alreadynotvoiced", pl.getName()));
            }

            return CommandResult.empty();
        }

        MutableMessageChannel mmc = new PermissionMessageChannel(this.permissions.getPermissionWithSuffix("notify")).asMutable();
        mmc.addMember(src);
        if (turnOn) {
            this.muteHandler.addVoice(pl.getUniqueId());
            mmc.send(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.voice.voiced.source", pl.getName()));
            pl.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.voice.voiced.target"));
        } else {
            this.muteHandler.removeVoice(pl.getUniqueId());
            mmc.send(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.voice.voiced.source", pl.getName()));
            pl.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.voice.voiced.target"));
        }

        return CommandResult.success();
    }
}

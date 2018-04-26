/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.note.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.note.data.NoteData;
import io.github.nucleuspowered.nucleus.modules.note.handlers.NoteHandler;
import io.github.nucleuspowered.nucleus.util.PermissionMessageChannel;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.channel.MutableMessageChannel;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Permissions(suggestedLevel = SuggestedLevel.MOD)
@NoModifiers
@NonnullByDefault
@RegisterCommand({"note", "addnote"})
public class NoteCommand extends AbstractCommand<CommandSource> {
    private static final String notifyPermission = PermissionRegistry.PERMISSIONS_PREFIX + "note.notify";

    private final NoteHandler noteHandler = getServiceUnchecked(NoteHandler.class);

    @Override
    public Map<String, PermissionInformation> permissionsToRegister() {
        Map<String, PermissionInformation> m = new HashMap<>();
        m.put(notifyPermission, PermissionInformation.getWithTranslation("permission.note.notify", SuggestedLevel.MOD));
        return m;
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                NucleusParameters.ONE_USER,
                NucleusParameters.MESSAGE
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) {
        User user = args.<User>getOne(NucleusParameters.Keys.USER).get();
        String note = args.<String>getOne(NucleusParameters.Keys.MESSAGE).get();

        UUID noter = Util.consoleFakeUUID;
        if (src instanceof Player) {
            noter = ((Player) src).getUniqueId();
        }

        NoteData noteData = new NoteData(Instant.now(), noter, note);

        if (this.noteHandler.addNote(user, noteData)) {
            MutableMessageChannel messageChannel = new PermissionMessageChannel(notifyPermission).asMutable();
            messageChannel.addMember(src);

            messageChannel.send(
                    Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.note.success", src.getName(), noteData.getNote(), user.getName()));

            return CommandResult.success();
        }

        src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.warn.fail", user.getName()));
        return CommandResult.empty();
    }
}

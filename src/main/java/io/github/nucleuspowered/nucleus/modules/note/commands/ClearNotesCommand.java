/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.note.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.note.data.NoteData;
import io.github.nucleuspowered.nucleus.modules.note.handlers.NoteHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.List;

@Permissions(suggestedLevel = SuggestedLevel.ADMIN)
@RunAsync
@NoModifiers
@NonnullByDefault
@RegisterCommand({"clearnotes", "removeallnotes"})
public class ClearNotesCommand extends AbstractCommand<CommandSource> {

    private final NoteHandler handler = getServiceUnchecked(NoteHandler.class);
    private final String playerKey = "subject";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {GenericArguments.onlyOne(GenericArguments.user(Text.of(this.playerKey)))};
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) {
        User user = args.<User>getOne(this.playerKey).get();

        List<NoteData> notes = this.handler.getNotesInternal(user);
        if (notes.isEmpty()) {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.checknotes.none", user.getName()));
            return CommandResult.success();
        }

        if (this.handler.clearNotes(user)) {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.clearnotes.success", user.getName()));
            return CommandResult.success();
        }

        src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.clearnotes.failure", user.getName()));
        return CommandResult.empty();
    }
}
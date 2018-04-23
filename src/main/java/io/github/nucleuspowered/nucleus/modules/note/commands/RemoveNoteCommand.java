/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.note.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.argumentparsers.NoteArgument;
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
@RegisterCommand({"removenote", "deletenote", "delnote"})
public class RemoveNoteCommand extends AbstractCommand<CommandSource> {

    private final NoteHandler handler = getServiceUnchecked(NoteHandler.class);
    private final String noteKey = "note";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {GenericArguments.onlyOne(new NoteArgument(Text.of(this.noteKey), this.handler))};
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) {
        NoteArgument.Result result = args.<NoteArgument.Result>getOne(this.noteKey).get();
        User user = result.user;

        List<NoteData> notes = this.handler.getNotesInternal(user);
        if (notes.isEmpty()) {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.checkwarnings.none", user.getName()));
            return CommandResult.success();
        }

        if (this.handler.removeNote(user, result.noteData)) {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.removenote.success", user.getName()));
            return CommandResult.success();
        }

        src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.removenote.failure", user.getName()));
        return CommandResult.empty();
    }
}

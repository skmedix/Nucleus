/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.note.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.note.data.NoteData;
import io.github.nucleuspowered.nucleus.modules.note.handlers.NoteHandler;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Checks the notes of a subject.
 *
 * Command Usage: /checknotes user Permission: plugin.checknotes.base
 */
@Permissions(suggestedLevel = SuggestedLevel.MOD)
@RunAsync
@NoModifiers
@NonnullByDefault
@RegisterCommand({"checknotes", "notes"})
public class CheckNotesCommand extends AbstractCommand<CommandSource> {

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

        List<Text> messages = notes.stream().sorted(Comparator.comparing(NoteData::getDate)).map(x -> createMessage(x, user)).collect(Collectors.toList());
        messages.add(0, Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.checknotes.info"));

        PaginationService paginationService = Sponge.getGame().getServiceManager().provideUnchecked(PaginationService.class);
        paginationService.builder()
                .title(
                        Text.builder()
                        .color(TextColors.GOLD)
                        .append(Text.of(Nucleus.getNucleus().getMessageProvider().getMessageWithFormat("command.checknotes.header", user.getName())))
                        .build())
                .padding(
                        Text.builder()
                        .color(TextColors.YELLOW)
                        .append(Text.of("="))
                        .build())
                .contents(messages)
                .sendTo(src);

        return CommandResult.success();
    }

    private Text createMessage(NoteData note, User user) {
        String name;
        if (note.getNoterInternal().equals(Util.consoleFakeUUID)) {
            name = Sponge.getServer().getConsole().getName();
        } else {
            Optional<User> ou = Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(note.getNoterInternal());
            name = ou.map(User::getName).orElseGet(() -> Nucleus.getNucleus().getMessageProvider().getMessageWithFormat("standard.unknown"));
        }

        //Get the ID of the note, its index in the users List<NoteData>. Add one to start with an ID of 1.
        int id = this.handler.getNotesInternal(user).indexOf(note) + 1;

        //Action buttons, this should look like 'Action > [Delete] - [Return] <'
        Text.Builder actions = Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.checknotes.action").toBuilder();

        //Add separation between the word 'Action' and action buttons
        actions.append(Text.of(TextColors.GOLD, " > "));

        //Add the delete button [Delete]
        actions.append(Text.builder().append(Text.of(TextColors.RED, Nucleus.getNucleus().getMessageProvider().getMessageWithFormat("standard.action.delete")))
                .onHover(TextActions.showText(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.checknotes.hover.delete")))
                .onClick(TextActions.runCommand("/removenote " + user.getName() + " " + id))
                .build());

        //Add a - to separate it from the next action button
        actions.append(Text.of(TextColors.GOLD, " - "));

        //Add the return button [Return]
        actions.append(Text.builder().append(Text.of(TextColors.GREEN, Nucleus.getNucleus().getMessageProvider().getMessageWithFormat("standard.action.return")))
                .onHover(TextActions.showText(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.checknotes.hover.return")))
                .onClick(TextActions.runCommand("/checknotes " + user.getName()))
                .build());

        //Add a < to end the actions button list
        actions.append(Text.of(TextColors.GOLD, " < "));

        //Get and format the date of the warning
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMM dd, yyyy").withZone(ZoneId.systemDefault());
        String date = dtf.format(note.getDate());

        //Create a clickable name providing more information about the warning
        Text.Builder information = Text.builder(name)
                .onHover(TextActions.showText(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.checknotes.hover.check")))
                .onClick(TextActions.executeCallback(commandSource -> {
                    commandSource.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.checknotes.id", String.valueOf(id)));
                    commandSource.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.checknotes.date", date));
                    commandSource.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.checknotes.noter", name));
                    commandSource.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.checknotes.note", note.getNote()));
                    commandSource.sendMessage(actions.build());
                }));

        //Create the warning message
        Text.Builder message = Text.builder()
                .append(Text.of(TextColors.GREEN, information.build()))
                .append(Text.of(": "))
                .append(Text.of(TextColors.YELLOW, note.getNote()));


        return message.build();
    }
}

/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.note.handlers;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.nucleusdata.Note;
import io.github.nucleuspowered.nucleus.api.service.NucleusNoteService;
import io.github.nucleuspowered.nucleus.dataservices.loaders.UserDataManager;
import io.github.nucleuspowered.nucleus.dataservices.modular.ModularUserService;
import io.github.nucleuspowered.nucleus.modules.note.data.NoteData;
import io.github.nucleuspowered.nucleus.modules.note.datamodules.NoteUserDataModule;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.User;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class NoteHandler implements NucleusNoteService {

    private final Nucleus nucleus = Nucleus.getNucleus();
    private final UserDataManager userDataManager = this.nucleus.getUserDataManager();

    public List<NoteData> getNotesInternal(User user) {
        Optional<ModularUserService> userService = this.userDataManager.get(user);
        return userService.map(modularUserService -> modularUserService.get(NoteUserDataModule.class).getNotes()).orElseGet(Lists::newArrayList);
    }

    @Override public ImmutableList<Note> getNotes(User user) {
        return ImmutableList.copyOf(getNotesInternal(user));
    }

    @Override public boolean addNote(User user, CommandSource source, String note) {
        return addNote(user, new NoteData(Instant.now(), Util.getUUID(source), note));
    }

    public boolean addNote(User user, NoteData note) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(note);

        Optional<ModularUserService> optUserService = this.userDataManager.get(user);
        if (!optUserService.isPresent()) {
            return false;
        }

        optUserService.get().get(NoteUserDataModule.class).addNote(note);
        return true;
    }

    @Override
    public boolean removeNote(User user, Note note) {
        Optional<ModularUserService> userService = this.userDataManager.get(user);
        if (userService.isPresent()) {
            userService.get().get(NoteUserDataModule.class).removeNote(note);
            return true;
        }

        return false;
    }

    @Override
    public boolean clearNotes(User user) {
        Optional<ModularUserService> userService = this.userDataManager.get(user);
        if (userService.isPresent()) {
            userService.get().get(NoteUserDataModule.class).clearNotes();
            return true;
        }

        return false;
    }
}

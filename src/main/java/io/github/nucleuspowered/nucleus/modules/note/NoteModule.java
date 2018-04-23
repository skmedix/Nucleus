/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.note;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.service.NucleusNoteService;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterService;
import io.github.nucleuspowered.nucleus.internal.qsml.module.ConfigurableModule;
import io.github.nucleuspowered.nucleus.modules.note.commands.CheckNotesCommand;
import io.github.nucleuspowered.nucleus.modules.note.config.NoteConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.note.handlers.NoteHandler;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

@RegisterService(value = NoteHandler.class, apiService = NucleusNoteService.class)
@ModuleData(id = NoteModule.ID, name = "Note")
public class NoteModule extends ConfigurableModule<NoteConfigAdapter> {

    public final static String ID = "note";

    @Override
    public NoteConfigAdapter createAdapter() {
        return new NoteConfigAdapter();
    }

    @Override
    public void performEnableTasks() {
        // Take base permission from /checknotes.
        createSeenModule(CheckNotesCommand.class, (c, u) -> {

            NoteHandler jh = Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(NoteHandler.class);
            int active = jh.getNotesInternal(u).size();

            Text r = Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("seen.notes", String.valueOf(active));
            if (active > 0) {
                return Lists.newArrayList(
                        r.toBuilder().onClick(TextActions.runCommand("/checknotes " + u.getName()))
                                .onHover(TextActions.showText(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("standard.clicktoseemore"))).build());
            }

            return Lists.newArrayList(r);
        });
    }
}

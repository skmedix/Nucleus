/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.note.listeners;

import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.note.NoteModule;
import io.github.nucleuspowered.nucleus.modules.note.config.NoteConfig;
import io.github.nucleuspowered.nucleus.modules.note.config.NoteConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.note.data.NoteData;
import io.github.nucleuspowered.nucleus.modules.note.handlers.NoteHandler;
import io.github.nucleuspowered.nucleus.util.PermissionMessageChannel;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.channel.MutableMessageChannel;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class NoteListener implements ListenerBase.Conditional {

    private final NoteHandler handler = getServiceUnchecked(NoteHandler.class);

    private final String showOnLogin = PermissionRegistry.PERMISSIONS_PREFIX + "note.showonlogin";

    /**
     * At the time the subject joins, check to see if the subject has any notes,
     * if he does send them to users with the permission plugin.note.showonlogin
     *
     * @param event The event.
     * @param player The {@link Player} that has just logged in.
     */
    @Listener
    public void onPlayerLogin(final ClientConnectionEvent.Join event, @Getter("getTargetEntity") final Player player) {
        Sponge.getScheduler().createTaskBuilder().async().delay(500, TimeUnit.MILLISECONDS).execute(() -> {
            List<NoteData> notes = this.handler.getNotesInternal(player);
            if (notes != null && !notes.isEmpty()) {
                MutableMessageChannel messageChannel = new PermissionMessageChannel(this.showOnLogin).asMutable();
                messageChannel.send(
                        Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("note.login.notify", player.getName(), String.valueOf(notes.size())).toBuilder()
                        .onHover(TextActions.showText(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("note.login.view", player.getName())))
                        .onClick(TextActions.runCommand("/checknotes " + player.getName()))
                        .build());

            }
        }).submit(Nucleus.getNucleus());
    }

    @Override
    public Map<String, PermissionInformation> getPermissions() {
        Map<String, PermissionInformation> mp = Maps.newHashMap();
        mp.put(this.showOnLogin, PermissionInformation.getWithTranslation("permission.note.showonlogin", SuggestedLevel.MOD));
        return mp;
    }

    @Override public boolean shouldEnable() {
        return Nucleus.getNucleus().getConfigValue(NoteModule.ID, NoteConfigAdapter.class, NoteConfig::isShowOnLogin).orElse(false);
    }

}

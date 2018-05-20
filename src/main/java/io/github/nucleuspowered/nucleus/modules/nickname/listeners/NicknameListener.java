/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.nickname.listeners;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.traits.InternalServiceManagerTrait;
import io.github.nucleuspowered.nucleus.modules.nickname.datamodules.NicknameUserDataModule;
import io.github.nucleuspowered.nucleus.modules.nickname.services.NicknameService;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;

import java.util.Optional;

public class NicknameListener implements ListenerBase, InternalServiceManagerTrait {

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event, @Root Player player) {
        Nucleus.getNucleus().getUserDataManager().get(player).ifPresent(x -> {
            Optional<Text> d = x.get(NicknameUserDataModule.class).getNicknameAsText();
            d.ifPresent(text -> getServiceUnchecked(NicknameService.class).updateCache(player.getUniqueId(), text));

            player.offer(
                    Keys.DISPLAY_NAME,
                    d.orElseGet(() -> Text.of(player.getName())));
        });
    }

    @Listener
    public void onPlayerQuit(ClientConnectionEvent.Disconnect event, @Root Player player) {
        getServiceUnchecked(NicknameService.class).removeFromCache(player.getUniqueId());
    }
}

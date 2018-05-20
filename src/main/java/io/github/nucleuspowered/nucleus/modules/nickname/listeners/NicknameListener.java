/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.nickname.listeners;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.modules.nickname.datamodules.NicknameUserDataModule;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;

public class NicknameListener extends ListenerBase {

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event, @Root Player player) {
        Nucleus.getNucleus().getUserDataManager().get(player).ifPresent(x -> {
            player.offer(
                    Keys.DISPLAY_NAME,
                    x.get(NicknameUserDataModule.class).getNicknameAsText().orElseGet(() -> Text.of(player.getName())));
        });
    }
}

/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.nameban.listeners;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.serializer.TextSerializers;

public class NameBanListener implements ListenerBase {

    @Listener
    public void onPlayerLogin(ClientConnectionEvent.Auth event) {
        event.getProfile().getName().ifPresent(name -> Nucleus.getNucleus().getNameBanService().getBanReason(name.toLowerCase()).ifPresent(x -> {
            event.setCancelled(true);
            event.setMessage(TextSerializers.FORMATTING_CODE.deserialize(x));
        }));
    }
}

/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.mute.listeners;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.modules.mute.MuteModule;
import io.github.nucleuspowered.nucleus.modules.mute.config.MuteConfig;
import io.github.nucleuspowered.nucleus.modules.mute.config.MuteConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.mute.datamodules.MuteUserDataModule;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.network.ClientConnectionEvent;

public class MuteLogoutConditionalListener implements ListenerBase.Conditional {

    @Listener
    public void onLogout(ClientConnectionEvent.Disconnect event, @Getter("getTargetEntity") Player player) {
        Nucleus.getNucleus().getUserDataManager().get(player).ifPresent(y -> y.get(MuteUserDataModule.class).getMuteData().ifPresent(x -> {
                x.getRemainingTime().ifPresent(x::setTimeFromNextLogin);
            Nucleus.getNucleus().getUserDataManager().getUnchecked(player).get(MuteUserDataModule.class).setMuteData(x);
            }));
    }

    @Override public boolean shouldEnable() {
        return Nucleus.getNucleus().getConfigValue(MuteModule.ID, MuteConfigAdapter.class, MuteConfig::isMuteOnlineOnly).orElse(false);
    }

}

/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.protection.listeners;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.modules.protection.ProtectionModule;
import io.github.nucleuspowered.nucleus.modules.protection.config.ProtectionConfig;
import io.github.nucleuspowered.nucleus.modules.protection.config.ProtectionConfigAdapter;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;

public class CropTrampleListener implements Reloadable, ListenerBase.Conditional {

    private boolean cropentity = false;
    private boolean cropplayer = false;

    // Not sure this should be correct. Keep an eye.
    @Listener
    public void onBlockChange(ChangeBlockEvent.Place breakEvent, @Root Entity entity) {
        // If player or entity and the corresponding option is added
        boolean isPlayer = entity instanceof Player;
        if (this.cropplayer && isPlayer || this.cropentity && !isPlayer) {
            // Go from Farmland to Dirt.
            breakEvent.getTransactions().stream()
                    .filter(Transaction::isValid)
                    .filter(x -> x.getOriginal().getState().getType().equals(BlockTypes.FARMLAND))
                    .filter(x -> x.getFinal().getState().getType().equals(BlockTypes.DIRT))
                    .forEach(x -> x.setValid(false));
        }
    }

    @Override
    public void onReload() {
        ProtectionConfigAdapter protectionConfigAdapter = getServiceUnchecked(ProtectionConfigAdapter.class);
        this.cropentity = protectionConfigAdapter.getNodeOrDefault().isDisableMobCropTrample();
        this.cropplayer = protectionConfigAdapter.getNodeOrDefault().isDisablePlayerCropTrample();
    }

    @Override public boolean shouldEnable() {
        return Nucleus.getNucleus().getConfigValue(ProtectionModule.ID, ProtectionConfigAdapter.class, ProtectionConfig::isDisableAnyCropTrample)
                .orElse(false);
    }
}

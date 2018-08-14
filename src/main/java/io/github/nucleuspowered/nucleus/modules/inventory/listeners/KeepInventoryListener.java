/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.inventory.listeners;

import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.annotations.RequireExistenceOf;
import io.github.nucleuspowered.nucleus.internal.annotations.SkipOnError;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.ServiceChangeListener;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.filter.Getter;

import java.util.Map;

@SkipOnError
@RequireExistenceOf(value = "org.spongepowered.api.event.entity.DestructEntityEvent$Death#setKeepInventory", showError = false)
public class KeepInventoryListener implements ListenerBase.Conditional {

    @Override
    public Map<String, PermissionInformation> getPermissions() {
        Map<String, PermissionInformation> perms = Maps.newHashMap();
        perms.put("nucleus.inventory.keepondeath", PermissionInformation.getWithTranslation("permission.inventory.keep", SuggestedLevel.ADMIN));
        return perms;
    }

    @Listener
    public void onEntityDeath(DestructEntityEvent.Death event, @Getter("getTargetEntity") Player living) {
        if (hasPermission(living, "nucleus.inventory.keepondeath")) {
            event.setKeepInventory(true);
        }
    }

    @Override
    public boolean shouldEnable() {
        return !ServiceChangeListener.isOpOnly();
    }

}

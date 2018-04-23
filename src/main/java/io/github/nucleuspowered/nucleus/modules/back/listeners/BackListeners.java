/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.back.listeners;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.NucleusAPI;
import io.github.nucleuspowered.nucleus.api.service.NucleusJailService;
import io.github.nucleuspowered.nucleus.internal.CommandPermissionHandler;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.modules.back.commands.BackCommand;
import io.github.nucleuspowered.nucleus.modules.back.config.BackConfig;
import io.github.nucleuspowered.nucleus.modules.back.config.BackConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.back.handlers.BackHandler;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.type.Exclude;

import javax.annotation.Nullable;

public class BackListeners implements Reloadable, ListenerBase {

    public static final String ON_TELEPORT = "targets.teleport";
    public static final String ON_DEATH = "targets.death";
    public static final String ON_PORTAL = "targets.portal";

    private final BackHandler handler = Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(BackHandler.class);
    private BackConfig backConfig = new BackConfig();
    @Nullable private final NucleusJailService njs = NucleusAPI.getJailService().orElse(null);

    private CommandPermissionHandler s = Nucleus.getNucleus().getPermissionRegistry().getPermissionsForNucleusCommand(BackCommand.class);

    @Override public void onReload() {
        this.backConfig = Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(BackConfigAdapter.class).getNodeOrDefault();
    }

    @Listener
    @Exclude(MoveEntityEvent.Teleport.Portal.class) // Don't set /back on a portal.
    public void onTeleportPlayer(MoveEntityEvent.Teleport event, @Getter("getTargetEntity") Player pl) {
        if (this.backConfig.isOnTeleport() && check(event) && getLogBack(pl) && this.s.testSuffix(pl, ON_TELEPORT)) {
            this.handler.setLastLocation(pl, event.getFromTransform());
        }
    }

    @Listener
    public void onPortalPlayer(MoveEntityEvent.Teleport.Portal event, @Getter("getTargetEntity") Player pl) {
        if (this.backConfig.isOnPortal() && check(event) && getLogBack(pl) && this.s.testSuffix(pl, ON_PORTAL)) {
            this.handler.setLastLocation(pl, event.getFromTransform());
        }
    }

    @Listener
    public void onDeathEvent(DestructEntityEvent.Death event) {
        Living e = event.getTargetEntity();
        if (!(e instanceof Player)) {
            return;
        }

        Player pl = (Player)e;
        if (this.backConfig.isOnDeath() && getLogBack(pl) && this.s.testSuffix(pl, ON_DEATH)) {
            this.handler.setLastLocation(pl, event.getTargetEntity().getTransform());
        }
    }

    private boolean check(MoveEntityEvent.Teleport event) {
        return !event.getFromTransform().equals(event.getToTransform());
    }

    private boolean getLogBack(Player player) {
        return !(this.njs != null && this.njs.isPlayerJailed(player)) && this.handler.isLoggingLastLocation(player);
    }
}

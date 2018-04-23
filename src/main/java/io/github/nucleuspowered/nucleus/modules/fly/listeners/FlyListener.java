/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.fly.listeners;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.dataservices.modular.ModularUserService;
import io.github.nucleuspowered.nucleus.internal.CommandPermissionHandler;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.internal.teleport.NucleusTeleportHandler;
import io.github.nucleuspowered.nucleus.modules.fly.commands.FlyCommand;
import io.github.nucleuspowered.nucleus.modules.fly.config.FlyConfig;
import io.github.nucleuspowered.nucleus.modules.fly.config.FlyConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.fly.datamodules.FlyUserDataModule;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.world.World;

import java.util.Optional;

public class FlyListener implements Reloadable, ListenerBase {

    private FlyConfig flyConfig = new FlyConfig();
    private CommandPermissionHandler flyCommandHandler =
            Nucleus.getNucleus().getPermissionRegistry().getPermissionsForNucleusCommand(FlyCommand.class);

    // Do it first, so other plugins can have a say.
    @Listener(order = Order.FIRST)
    public void onPlayerJoin(ClientConnectionEvent.Join event, @Getter("getTargetEntity") Player pl) {
        if (shouldIgnoreFromGameMode(pl)) {
            return;
        }

        if (this.flyConfig.isPermissionOnLogin() && !this.flyCommandHandler.testBase(pl)) {
            safeTeleport(pl);
            return;
        }

        Optional<ModularUserService> serviceOptional = Nucleus.getNucleus().getUserDataManager().get(pl);
        if (serviceOptional.isPresent()) {
            // Let's just reset these...
            if (serviceOptional.get().get(FlyUserDataModule.class).isFlyingSafe()) {
                pl.offer(Keys.CAN_FLY, true);

                // If in the air, flying!
                if (pl.getLocation().add(0, -1, 0).getBlockType().getId().equals(BlockTypes.AIR.getId())) {
                    pl.offer(Keys.IS_FLYING, true);
                }

                return;
            }
        }

        safeTeleport(pl);
    }

    @Listener
    public void onPlayerQuit(ClientConnectionEvent.Disconnect event, @Getter("getTargetEntity") Player pl) {
        if (!this.flyConfig.isSaveOnQuit()) {
            return;
        }

        if (shouldIgnoreFromGameMode(pl)) {
            return;
        }

        try {
            Nucleus.getNucleus().getUserDataManager().getUnchecked(pl)
                    .get(FlyUserDataModule.class).setFlying(pl.get(Keys.CAN_FLY).orElse(false));
        } catch (Exception e) {
            Nucleus.getNucleus().printStackTraceIfDebugMode(e);
        }
    }

    // Only fire if there is no cancellation at the end.
    @Listener(order = Order.LAST)
    public void onPlayerTransferWorld(MoveEntityEvent.Teleport event,
                                      @Getter("getTargetEntity") Entity target,
                                      @Getter("getFromTransform") Transform<World> twfrom,
                                      @Getter("getToTransform") Transform<World> twto) {

        if (!(target instanceof Player)) {
            return;
        }

        Player pl = (Player)target;
        if (shouldIgnoreFromGameMode(pl)) {
            return;
        }

        ModularUserService uc;
        try {
            uc = Nucleus.getNucleus().getUserDataManager().getUnchecked(pl);
            if (!uc.get(FlyUserDataModule.class).isFlying()) {
                return;
            }
        } catch (Exception e) {
            Nucleus.getNucleus().printStackTraceIfDebugMode(e);

            return;
        }

        // If we have a subject, and this happens...
        boolean isFlying = target.get(Keys.IS_FLYING).orElse(false);

        // If we're moving world...
        if (!twfrom.getExtent().getUniqueId().equals(twto.getExtent().getUniqueId())) {
            // Next tick, they can fly... if they have permission to do so.
            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                if (getFlyCommandHandler().testBase(pl)) {
                    target.offer(Keys.CAN_FLY, true);
                    if (isFlying) {
                        target.offer(Keys.IS_FLYING, true);
                    }
                } else {
                    uc.get(FlyUserDataModule.class).setFlying(false);
                    target.offer(Keys.CAN_FLY, false);
                    target.offer(Keys.IS_FLYING, false);
                }
            }).submit(Nucleus.getNucleus());
        }
    }

    static boolean shouldIgnoreFromGameMode(Player player) {
        GameMode gm = player.get(Keys.GAME_MODE).orElse(GameModes.NOT_SET);
        return (gm.equals(GameModes.CREATIVE) || gm.equals(GameModes.SPECTATOR));
    }

    private CommandPermissionHandler getFlyCommandHandler() {
        if (this.flyCommandHandler == null) {
            this.flyCommandHandler = Nucleus.getNucleus().getPermissionRegistry().getPermissionsForNucleusCommand(FlyCommand.class);
        }

        return this.flyCommandHandler;
    }

    @Override public void onReload() throws Exception {
        this.flyConfig = Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(FlyConfigAdapter.class).getNode();
    }

    private void safeTeleport(Player pl) {
        if (!pl.isOnGround() && this.flyConfig.isFindSafeOnLogin()) {
            // Try to bring the subject down.
            Nucleus.getNucleus().getTeleportHandler().teleportPlayer(pl,
                    pl.getTransform(),
                    NucleusTeleportHandler.StandardTeleportMode.SAFE_TELEPORT_DESCEND);
        }
    }
}

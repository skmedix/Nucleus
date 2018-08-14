/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.vanish.service;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.internal.traits.PermissionTrait;
import io.github.nucleuspowered.nucleus.modules.vanish.commands.VanishCommand;
import io.github.nucleuspowered.nucleus.modules.vanish.config.VanishConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.vanish.datamodules.VanishUserDataModule;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.tab.TabListEntry;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;

public class VanishService implements Reloadable, PermissionTrait {

    private final String canseePerm = Nucleus.getNucleus().getPermissionRegistry()
            .getPermissionsForNucleusCommand(VanishCommand.class).getPermissionWithSuffix("see");
    private boolean isAlter = false;

    @Override
    public void onReload() {
        String property = System.getProperty("nucleus.vanish.tablist.enable");
        this.isAlter = property != null && !property.isEmpty() &&
            Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(VanishConfigAdapter.class).getNodeOrDefault().isAlterTabList();
    }

    public boolean isVanished(Player player) {
        return Nucleus.getNucleus().getUserDataManager().getUnchecked(player).get(VanishUserDataModule.class).isVanished();
    }


    public void vanishPlayer(Player player) {
        vanishPlayer(player, false);
    }

    public void vanishPlayer(Player player, boolean delay) {
        VanishUserDataModule service = Nucleus.getNucleus().getUserDataManager().getUnchecked(player).get(VanishUserDataModule.class);
        service.setVanished(true);

        if (delay) {
            Task.builder().execute(() -> vanishPlayerInternal(player)).delayTicks(0).name("Nucleus Vanish runnable").submit(Nucleus.getNucleus());
        } else {
            vanishPlayerInternal(player);
        }
    }

    private void vanishPlayerInternal(Player player) {
        VanishUserDataModule service = Nucleus.getNucleus().getUserDataManager().getUnchecked(player).get(VanishUserDataModule.class);
        if (service.isVanished()) {
            player.offer(Keys.VANISH, true);
            player.offer(Keys.VANISH_IGNORES_COLLISION, true);
            player.offer(Keys.VANISH_PREVENTS_TARGETING, true);

            if (this.isAlter) {
                Sponge.getServer().getOnlinePlayers().stream().filter(x -> !player.equals(x) || !hasPermission(x, this.canseePerm))
                        .forEach(x -> x.getTabList().removeEntry(player.getUniqueId()));
            }
        }
    }

    public void unvanishPlayer(Player player) {
        VanishUserDataModule service = Nucleus.getNucleus().getUserDataManager().getUnchecked(player).get(VanishUserDataModule.class);
        service.setVanished(false);
        player.offer(Keys.VANISH, false);
        player.offer(Keys.VANISH_IGNORES_COLLISION, false);
        player.offer(Keys.VANISH_PREVENTS_TARGETING, false);

        if (this.isAlter) {
            Sponge.getServer().getOnlinePlayers().forEach(x -> {
                if (!x.getTabList().getEntry(player.getUniqueId()).isPresent()) {
                    x.getTabList().addEntry(TabListEntry.builder()
                            .displayName(Text.of(player.getName()))
                            .profile(player.getProfile())
                            .gameMode(player.gameMode().get())
                            .latency(player.getConnection().getLatency())
                            .list(x.getTabList()).build());
                }
            });
        }
    }

}

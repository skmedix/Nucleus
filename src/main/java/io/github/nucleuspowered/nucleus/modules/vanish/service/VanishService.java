/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.vanish.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.modules.vanish.commands.VanishCommand;
import io.github.nucleuspowered.nucleus.modules.vanish.config.VanishConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.vanish.datamodules.VanishUserDataModule;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.tab.TabList;
import org.spongepowered.api.entity.living.player.tab.TabListEntry;
import org.spongepowered.api.text.Text;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class VanishService implements Reloadable {

    private final String canseePerm = Nucleus.getNucleus().getPermissionRegistry()
            .getPermissionsForNucleusCommand(VanishCommand.class).getPermissionWithSuffix("see");
    private boolean isAlter = false;

    @Override
    public void onReload() throws Exception {
        String property = System.getProperty("nucleus.vanish.tablist.enable");
        this.isAlter = property != null && !property.isEmpty() &&
            Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(VanishConfigAdapter.class).getNodeOrDefault().isAlterTabList();
    }

    public boolean isVanished(Player player) {
        return Nucleus.getNucleus().getUserDataManager().getUnchecked(player).get(VanishUserDataModule.class).isVanished();
    }

    public void vanishPlayer(Player player) {
        VanishUserDataModule service = Nucleus.getNucleus().getUserDataManager().getUnchecked(player).get(VanishUserDataModule.class);
        service.setVanished(true);
        player.offer(Keys.VANISH, true);
        player.offer(Keys.VANISH_IGNORES_COLLISION, true);
        player.offer(Keys.VANISH_PREVENTS_TARGETING, true);

        if (this.isAlter) {
            Sponge.getServer().getOnlinePlayers().stream().filter(x -> !player.equals(x) || !x.hasPermission(this.canseePerm))
                    .forEach(x -> x.getTabList().removeEntry(player.getUniqueId()));
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

    public void resetPlayerTabLists() {
        if (this.isAlter) {
            Collection<Player> players = Sponge.getServer().getOnlinePlayers();
            List<TabListEntry.Builder> standard = Lists.newArrayList();
            List<TabListEntry.Builder> enhanced = Lists.newArrayList();
            Map<UUID, TabListEntry.Builder> difference = Maps.newHashMap();

            players.forEach(x -> {
                TabListEntry.Builder t = TabListEntry.builder()
                        .displayName(Text.of(x.getName()))
                        .profile(x.getProfile())
                        .gameMode(x.gameMode().get())
                        .latency(x.getConnection().getLatency());
                if (!x.get(Keys.VANISH).orElse(false)) {
                    standard.add(t);
                } else {
                    difference.put(x.getUniqueId(), t);
                }

                enhanced.add(t);
            });

            players.forEach(x -> {
                TabList list = x.getTabList();
                Collection<TabListEntry> toRemove = Lists.newArrayList(list.getEntries());
                toRemove.forEach(y -> list.removeEntry(y.getProfile().getUniqueId()));

                if (x.hasPermission(this.canseePerm)) {
                    enhanced.forEach(y -> list.addEntry(y.list(list).build()));
                } else {
                    standard.forEach(y -> list.addEntry(y.list(list).build()));
                    TabListEntry.Builder t = difference.get(x.getUniqueId());
                    if (t != null) {
                        list.addEntry(t.list(list).build());
                    }
                }
            });
        }

    }
}

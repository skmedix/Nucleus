/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.listeners;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.events.NucleusFirstJoinEvent;
import io.github.nucleuspowered.nucleus.api.exceptions.KitRedeemException;
import io.github.nucleuspowered.nucleus.dataservices.KitService;
import io.github.nucleuspowered.nucleus.dataservices.loaders.UserDataManager;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.modules.kit.config.KitConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.kit.datamodules.KitUserDataModule;
import io.github.nucleuspowered.nucleus.modules.kit.handlers.KitHandler;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.filter.type.Exclude;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("ALL")
public class KitListener implements Reloadable, ListenerBase {

    private final UserDataManager loader = Nucleus.getNucleus().getUserDataManager();
    private final KitHandler handler = getServiceUnchecked(KitHandler.class);
    private final KitService gds = Nucleus.getNucleus().getKitService();

    private boolean mustGetAll;

    @Listener
    public void onPlayerFirstJoin(NucleusFirstJoinEvent event, @Getter("getTargetEntity") Player player) {
        loader.get(player).ifPresent(p -> {
            gds.getFirstJoinKits().stream().filter(x -> x.isFirstJoinKit())
                .forEach(kit -> {
                    try {
                        handler.redeemKit(kit, player, false, true);
                    } catch (KitRedeemException e) {
                        // ignored
                    }
                });
        });
    }

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event, @Root Player player) {
        loader.get(player).ifPresent(p -> {
            KitUserDataModule user = loader.get(player.getUniqueId()).get().get(KitUserDataModule.class);
            gds.getAutoRedeemable().stream()
                .filter(k -> k.ignoresPermission() ||
                        !hasPermission(player, PermissionRegistry.PERMISSIONS_PREFIX + "kits." + k.getName().toLowerCase()))
                .forEach(k -> {
                    try {
                        handler.redeemKit(k, player, true, this.mustGetAll);
                    } catch (KitRedeemException e) {
                        // player.sendMessage(e.getText());
                    }
                });
        });
    }

    @Listener
    @Exclude({InteractInventoryEvent.Open.class})
    public void onPlayerInteractInventory(final InteractInventoryEvent event, @Root final Player player,
            @Getter("getTargetInventory") final Container inventory) {
        handler.getCurrentlyOpenInventoryKit(inventory).ifPresent(x -> {
            try {
                x.getFirst().updateKitInventory(x.getSecond());
                handler.saveKit(x.getFirst());

                if (event instanceof InteractInventoryEvent.Close) {
                    gds.save();
                    player.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.kit.edit.success", x.getFirst().getName()));
                    handler.removeKitInventoryFromListener(inventory);
                    return;
                }
            } catch (Exception e) {
                if (Nucleus.getNucleus().isDebugMode()) {
                    e.printStackTrace();
                }

                player.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.kit.edit.error", x.getFirst().getName()));
            }
        });

        if (handler.isViewer(inventory)) {
            if (event instanceof InteractInventoryEvent.Close) {
                this.handler.removeViewer(inventory);
            } else {
                event.setCancelled(true);
            }
        }
    }

    private String fixup(List<Text> texts) {
        return getCommand(texts.stream().map(x -> {
            try {
                return TextSerializers.JSON.deserialize(x.toPlain()).toPlain();
            } catch (Exception e) {
                return x.toPlain();
            }
        }).collect(Collectors.toList()));
    }

    private String getCommandFromText(List<Text> texts) {
        return getCommand(texts.stream().map(x -> x.toPlain()).collect(Collectors.toList()));
    }

    private String getCommand(List<String> strings) {
        StringBuilder builder = new StringBuilder();
        for (String string : strings) {
            if (builder.length() > 0) {
                builder.append(" ");
            }

            if (string.contains("\n")) {
                builder.append(string.split("\\n")[0]);
                return builder.toString();
            }

            builder.append(string);
        }

        return builder.toString();
    }

    @Override public void onReload() throws Exception {
        KitConfigAdapter kca = getServiceUnchecked(KitConfigAdapter.class);
        this.mustGetAll = kca.getNodeOrDefault().isMustGetAll();
    }
}

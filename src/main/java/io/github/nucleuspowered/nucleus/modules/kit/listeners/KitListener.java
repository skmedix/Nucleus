/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.listeners;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.events.NucleusFirstJoinEvent;
import io.github.nucleuspowered.nucleus.api.exceptions.KitRedeemException;
import io.github.nucleuspowered.nucleus.api.nucleusdata.Kit;
import io.github.nucleuspowered.nucleus.dataservices.KitService;
import io.github.nucleuspowered.nucleus.dataservices.loaders.UserDataManager;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import io.github.nucleuspowered.nucleus.modules.kit.config.KitConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.kit.datamodules.KitUserDataModule;
import io.github.nucleuspowered.nucleus.modules.kit.handlers.KitHandler;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.EntitySpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.filter.type.Exclude;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

@SuppressWarnings("ALL")
public class KitListener extends ListenerBase.Reloadable {

    private final UserDataManager loader;
    private final KitHandler handler;
    private final KitService gds;
    private final KitConfigAdapter kca;

    private boolean mustGetAll;

    @Inject
    public KitListener(UserDataManager loader, KitHandler handler, KitService gds,
            KitConfigAdapter kca) {
        this.loader = loader;
        this.handler = handler;
        this.gds = gds;
        this.kca = kca;
    }

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
                        (!kca.getNodeOrDefault().isSeparatePermissions() &&
                        !player.hasPermission(PermissionRegistry.PERMISSIONS_PREFIX + "kits." + k.getName().toLowerCase())))
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
    public void onPlayerInteractInventory(final InteractInventoryEvent event, @Root final Player player, @Getter("getTargetInventory") final Container inventory) {
        handler.getCurrentlyOpenInventoryKit(inventory).ifPresent(x -> {
            try {
                x.getFirst().updateKitInventory(x.getSecond());
                handler.saveKit(x.getFirst());

                if (event instanceof InteractInventoryEvent.Close) {
                    gds.save();
                    player.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.kit.edit.success", x.getFirst().getName()));
                    handler.removeKitInventoryFromListener(inventory);
                }
            } catch (Exception e) {
                if (Nucleus.getNucleus().isDebugMode()) {
                    e.printStackTrace();
                }

                player.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.kit.edit.error", x.getFirst().getName()));
            }
        });

        if (handler.isViewer(inventory)) {
            event.setCancelled(true);
        }
    }

    @Listener
    public void onPlayerInteractInventoryClose(final InteractInventoryEvent.Close event, @Root final Player player,
            @Getter("getTargetInventory") final Container inventory) {
        handler.getCurrentlyOpenInventoryCommandKit(inventory).ifPresent(x -> {
            // Set the commands.
            Kit kitInfo = x.getFirst();
            List<String> c = Lists.newArrayList();

            // For each slot, is it a written book?
            x.getSecond().slots().forEach(slot -> slot.poll().ifPresent(item -> {
                if (item.getItem().equals(ItemTypes.WRITTEN_BOOK)) {
                    item.get(Keys.BOOK_PAGES).ifPresent(y -> c.add(fixup(y)));
                } else if (item.getItem().equals(ItemTypes.WRITABLE_BOOK)) {
                    item.get(Keys.BOOK_PAGES).ifPresent(page -> c.add(getCommandFromText(page)));
                } else {
                    // Drop the item.
                    item.get(Keys.ITEM_BLOCKSTATE).ifPresent(z -> {
                        World world = player.getLocation().getExtent();
                        Entity e = world.createEntity(EntityTypes.ITEM, player.getLocation().getPosition());
                        e.offer(Keys.ITEM_BLOCKSTATE, z);
                        world.spawnEntity(e, Cause.of(NamedCause.owner(EntitySpawnCause.builder()
                                .type(SpawnTypes.PLUGIN).entity(player).build())));
                    });
                }

                kitInfo.setCommands(c);
                handler.saveKit(kitInfo);
            }));

            player.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.kit.command.edit.success", kitInfo.getName()));
            handler.removeKitCommandInventoryFromListener(inventory);
        });

        handler.removeViewer(inventory);
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
        this.mustGetAll = kca.getNodeOrDefault().isMustGetAll();
    }
}

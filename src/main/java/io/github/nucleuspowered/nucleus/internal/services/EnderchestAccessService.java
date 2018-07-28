/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.services;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.inventory.Inventory;

import java.util.Optional;

@FunctionalInterface
public interface EnderchestAccessService {

    EnderchestAccessService DEFAULT = user -> user.getPlayer().map(Player::getEnderChestInventory);

    Optional<Inventory> getEnderChest(User user);

}

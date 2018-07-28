/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.services;

import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.inventory.Inventory;

import java.util.Optional;

public class UserEnderchestAccessService implements EnderchestAccessService {

    @Override
    public Optional<Inventory> getEnderChest(User user) {
        return Optional.of(user.getEnderChestInventory());
    }
}

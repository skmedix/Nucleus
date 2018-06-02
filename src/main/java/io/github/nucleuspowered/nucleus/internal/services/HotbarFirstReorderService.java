/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.services;

import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryTransformations;

public class HotbarFirstReorderService implements InventoryReorderService {

    @Override
    public Inventory getOrderedInventory(Inventory inventory) {
        return inventory.transform(InventoryTransformations.PLAYER_MAIN_HOTBAR_FIRST);
    }
}

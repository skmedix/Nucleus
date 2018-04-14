/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.services;

import org.spongepowered.api.item.inventory.Inventory;

@FunctionalInterface
public interface InventoryReorderService {

    InventoryReorderService DEFAULT = inventory -> inventory;

    Inventory getOrderedInventory(Inventory inventory);

}

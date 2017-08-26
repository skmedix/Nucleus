/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.handlers;

import io.github.nucleuspowered.nucleus.api.service.NucleusKitService;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.util.Collection;

public class KitRedeemResult implements NucleusKitService.RedeemResult {

    private final Collection<ItemStackSnapshot> rejected;
    private final Collection<ItemStackSnapshot> previousInventory;

    public KitRedeemResult(Collection<ItemStackSnapshot> rejected,
            Collection<ItemStackSnapshot> previousInventory) {
        this.rejected = rejected;
        this.previousInventory = previousInventory;
    }

    @Override
    public Collection<ItemStackSnapshot> previousInventory() {
        return this.previousInventory;
    }

    @Override
    public Collection<ItemStackSnapshot> rejected() {
        return this.rejected;
    }

}

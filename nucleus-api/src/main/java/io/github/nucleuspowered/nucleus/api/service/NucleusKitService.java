/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.api.service;

import io.github.nucleuspowered.nucleus.api.exceptions.KitRedeemException;
import io.github.nucleuspowered.nucleus.api.nucleusdata.Kit;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * A service for getting and setting kits.
 */
public interface NucleusKitService {

    /**
     * Gets the names of all the kits currently in NucleusPlugin.
     * @return A {@link Set} of {@link String}s.
     */
    Set<String> getKitNames();

    /**
     * Gets the requested kit if it exists.
     *
     * @param name The name of the kit.
     * @return An {@link Optional} that might contain the kit.
     */
    Optional<Kit> getKit(String name);

    /**
     * Returns the items for a kit that the target player would redeem
     * (that is, with token replacements if applicable).
     *
     * @param kit The kit to process
     * @param player The player
     * @return The items.
     */
    Collection<ItemStack> getItemsForPlayer(Kit kit, Player player);

    /**
     * Redeems a kit on a player. Whether the player must get all items is controlled
     * by the Nucleus config.
     *
     * @param kit The kit to redeem
     * @param player The player to redeem the kit against
     * @param performChecks Whether to perform standard permission and cooldown checks
     * @return The {@link RedeemResult}
     *
     * @throws KitRedeemException thrown if a problem occurs.
     */
    RedeemResult redeemKit(Kit kit, Player player, boolean performChecks) throws KitRedeemException;

    /**
     * Redeems a kit on a player.
     *
     * @param kit The kit to redeem
     * @param player The player to redeem the kit against
     * @param performChecks Whether to perform standard permission and cooldown checks
     * @param mustRedeemAll Whether all items must be redeemed to count as a success
     * @return The {@link RedeemResult}
     * @throws KitRedeemException thrown if a problem occurs.
     */
    RedeemResult redeemKit(Kit kit, Player player, boolean performChecks, boolean mustRedeemAll) throws KitRedeemException;

    /**
     * Removes the requested kit.
     *
     * @param kitName The name of the kit to remove.
     * @return <code>true</code> if a kit was removed.
     */
    boolean removeKit(String kitName);

    /**
     * Renames a kit.
     *
     * @param kitName The name of the kit to rename
     * @param newKitName The new name of the kit
     * @throws IllegalArgumentException if either the kit or the target name are unavailable
     */
    void renameKit(String kitName, String newKitName) throws IllegalArgumentException;

    /**
     * Saves a kit with the requested name.
     *
     * @param kitName The name of the kit to save.
     * @param kit The kit to save.
     * @deprecated kitName is now ignored.
     */
    @Deprecated
    default void saveKit(String kitName, Kit kit) {
        saveKit(kit);
    }

    /**
     * Saves a kit.
     *
     * @param kit The kit.
     */
    void saveKit(Kit kit);

    /**
     * Gets a new kit object for use with the Kit service.
     *
     * <p>
     *     Do not make your own kit type, it will not get saved! Use this instead.
     * </p>
     *
     * @param name The name of the kit to create
     * @return The {@link Kit}
     * @throws IllegalArgumentException if the kit name already exists, or is invalid
     */
    Kit createKit(String name) throws IllegalArgumentException;

    /**
     * The result for a successful redemption.
     */
    interface RedeemResult {

        /**
         * The player's previous inventory.
         *
         * @return The previous inventory.
         */
        Collection<ItemStackSnapshot> previousInventory();

        /**
         * The items that didn't make it into the inventory.
         *
         * @return The rejected items.
         */
        Collection<ItemStackSnapshot> rejected();

    }

}

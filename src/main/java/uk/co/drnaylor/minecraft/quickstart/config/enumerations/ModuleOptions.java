/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.minecraft.quickstart.config.enumerations;

public enum ModuleOptions {
    /**
     * Loads the module unless another plugin requests it to not be removed.
     */
    DEFAULT,

    /**
     * Does not load the module.
     */
    DISABLED,

    /**
     * Loads the module, even if a plugin asks for it to be removed.
     */
    FORCELOAD
}

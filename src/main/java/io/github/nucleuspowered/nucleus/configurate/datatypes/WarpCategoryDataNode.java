/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.configurate.datatypes;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.Optional;

import javax.annotation.Nullable;

@ConfigSerializable
public class WarpCategoryDataNode {

    public WarpCategoryDataNode() {
    }

    public WarpCategoryDataNode(@Nullable String displayName, @Nullable String description) {
        this.displayName = displayName;
        this.description = description;
    }

    @Setting
    @Nullable
    private String displayName = null;

    @Setting
    @Nullable
    private String description = null;

    public Optional<String> getDisplayName() {
        return Optional.ofNullable(this.displayName);
    }

    public void setDisplayName(@Nullable String displayName) {
        this.displayName = displayName;
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(this.description);
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }
}

/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.permissions;

import javax.annotation.Nullable;

public enum SuggestedLevel {
    NONE(null),
    ADMIN(NONE),
    MOD(ADMIN),
    USER(MOD);

    @Nullable private final SuggestedLevel level;

    SuggestedLevel(@Nullable SuggestedLevel level) {
        this.level = level;
    }

    @Nullable
    public SuggestedLevel getLowerLevel() {
        return this.level;
    }
}

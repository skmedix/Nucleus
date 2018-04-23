/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.freezeplayer.datamodules;

import io.github.nucleuspowered.nucleus.dataservices.modular.DataKey;
import io.github.nucleuspowered.nucleus.dataservices.modular.DataModule;
import io.github.nucleuspowered.nucleus.dataservices.modular.ModularUserService;

public class FreezePlayerUserDataModule extends DataModule<ModularUserService> {

    @DataKey("isFrozen")
    private boolean isFrozen = false;

    public boolean isFrozen() {
        return this.isFrozen;
    }

    public void setFrozen(boolean value) {
        this.isFrozen = value;
    }

}

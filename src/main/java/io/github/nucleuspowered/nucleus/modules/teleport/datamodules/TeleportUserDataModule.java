/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.teleport.datamodules;

import io.github.nucleuspowered.nucleus.dataservices.modular.DataKey;
import io.github.nucleuspowered.nucleus.dataservices.modular.DataModule;
import io.github.nucleuspowered.nucleus.dataservices.modular.ModularUserService;

public class TeleportUserDataModule extends DataModule<ModularUserService> {

    @DataKey("tptoggle")
    private boolean isTeleportToggled = true;

    public boolean isTeleportToggled() {
        return this.isTeleportToggled;
    }

    public void setTeleportToggled(boolean teleportToggled) {
        this.isTeleportToggled = teleportToggled;
    }
}

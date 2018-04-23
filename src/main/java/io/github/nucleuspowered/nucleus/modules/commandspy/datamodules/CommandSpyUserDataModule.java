/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.commandspy.datamodules;

import io.github.nucleuspowered.nucleus.dataservices.modular.DataKey;
import io.github.nucleuspowered.nucleus.dataservices.modular.DataModule;
import io.github.nucleuspowered.nucleus.dataservices.modular.ModularUserService;

public class CommandSpyUserDataModule extends DataModule<ModularUserService> {

    @DataKey("isCommandSpy")
    private boolean isCommandSpy = false;

    public boolean isCommandSpy() {
        return this.isCommandSpy;
    }

    public void setCommandSpy(boolean commandSpy) {
        this.isCommandSpy = commandSpy;
    }
}

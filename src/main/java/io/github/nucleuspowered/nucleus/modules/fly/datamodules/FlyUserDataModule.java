/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.fly.datamodules;

import io.github.nucleuspowered.nucleus.dataservices.modular.DataKey;
import io.github.nucleuspowered.nucleus.dataservices.modular.DataModule;
import io.github.nucleuspowered.nucleus.dataservices.modular.ModularUserService;
import org.spongepowered.api.data.key.Keys;

public class FlyUserDataModule extends DataModule.ReferenceService<ModularUserService> {

    @DataKey("fly")
    private boolean fly = false;

    public FlyUserDataModule(ModularUserService modularDataService) {
        super(modularDataService);
    }

    public boolean isFlying() {
        getService().getPlayer().ifPresent(player -> this.fly = player.get(Keys.CAN_FLY).orElse(false));
        return this.fly;
    }

    public boolean isFlyingSafe() {
        return this.fly;
    }

    public void setFlying(boolean fly) {
        this.fly = fly;
    }
}

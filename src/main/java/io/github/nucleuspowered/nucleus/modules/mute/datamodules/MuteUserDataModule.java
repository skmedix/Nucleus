/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.mute.datamodules;

import io.github.nucleuspowered.nucleus.dataservices.modular.DataKey;
import io.github.nucleuspowered.nucleus.dataservices.modular.DataModule;
import io.github.nucleuspowered.nucleus.dataservices.modular.ModularUserService;
import io.github.nucleuspowered.nucleus.modules.mute.data.MuteData;

import java.util.Optional;

import javax.annotation.Nullable;

public class MuteUserDataModule extends DataModule<ModularUserService> {

    @DataKey("muteData")
    @Nullable
    private MuteData muteData;

    public Optional<MuteData> getMuteData() {
        return Optional.ofNullable(this.muteData);
    }

    public void setMuteData(@Nullable MuteData mData) {
        this.muteData = mData;
    }

    public void removeMuteData() {
        this.muteData = null;
    }
}

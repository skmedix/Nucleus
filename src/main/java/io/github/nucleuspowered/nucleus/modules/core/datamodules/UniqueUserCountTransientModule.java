/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.core.datamodules;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.dataservices.loaders.UserDataManager;
import io.github.nucleuspowered.nucleus.dataservices.modular.ModularGeneralService;
import io.github.nucleuspowered.nucleus.dataservices.modular.TransientModule;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfigAdapter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.manipulator.mutable.entity.JoinData;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.user.UserStorageService;

import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.Nullable;

public class UniqueUserCountTransientModule extends TransientModule<ModularGeneralService> {

    // This is a session variable - does not get saved on restart.
    private long userCount = 0;
    private boolean userCountIsDirty = false;

    public void resetUniqueUserCount() {
        resetUniqueUserCount(null);
    }

    public void resetUniqueUserCount(@Nullable Consumer<Long> resultConsumer) {
        if (!this.userCountIsDirty) {
            this.userCountIsDirty = true;
            Task.builder().async().execute(task -> {
                boolean accurate = Nucleus.getNucleus().getInternalServiceManager()
                        .getServiceUnchecked(CoreConfigAdapter.class).getNodeOrDefault().isMoreAccurate();
                UserStorageService uss = Sponge.getServiceManager().provideUnchecked(UserStorageService.class);
                UserDataManager userDataManager = Nucleus.getNucleus().getUserDataManager();

                // This could be slow...
                if (accurate) {
                    this.userCount = uss.getAll().stream().filter(GameProfile::isFilled)
                        .map(uss::get).filter(Optional::isPresent)
                        .filter(x ->
                                x.get().getPlayer().isPresent() ||
                                        userDataManager.has(x.get().getUniqueId()) ||
                                        // Temporary until Data is hooked up properly, I hope.
                                        x.get().get(JoinData.class).map(y -> y.firstPlayed().getDirect().isPresent()).orElse(false)).count();
                } else {
                    this.userCount = uss.getAll().stream().filter(GameProfile::isFilled).filter(x -> userDataManager.has(x.getUniqueId())).count();
                }

                this.userCountIsDirty = false;
                if (resultConsumer != null) {
                    resultConsumer.accept(this.userCount);
                }
            }).submit(Nucleus.getNucleus());
        }
    }

    public long getUniqueUserCount() {
        if (this.userCountIsDirty) {
            return this.userCount + 1;
        }

        return this.userCount;
    }
}

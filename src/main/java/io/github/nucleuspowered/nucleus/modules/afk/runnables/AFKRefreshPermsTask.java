/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.afk.runnables;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.TaskBase;
import io.github.nucleuspowered.nucleus.modules.afk.handlers.AFKHandler;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@SuppressWarnings("ALL")
@NonnullByDefault
public class AFKRefreshPermsTask implements TaskBase {

    private final AFKHandler handler = Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(AFKHandler.class);

    @Override public boolean isAsync() {
        return true;
    }

    @Override public Duration interval() {
        return Duration.of(2, ChronoUnit.MINUTES);
    }

    @Override public void accept(Task task) {
        this.handler.invalidateAfkCache();
    }
}

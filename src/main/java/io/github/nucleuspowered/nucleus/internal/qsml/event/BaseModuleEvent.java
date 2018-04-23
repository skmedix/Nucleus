/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.qsml.event;

import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.api.events.NucleusModuleEvent;
import io.github.nucleuspowered.nucleus.api.exceptions.ModulesLoadedException;
import io.github.nucleuspowered.nucleus.api.exceptions.NoModuleException;
import io.github.nucleuspowered.nucleus.api.exceptions.UnremovableModuleException;
import io.github.nucleuspowered.nucleus.api.service.NucleusModuleService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import uk.co.drnaylor.quickstart.enums.LoadingStatus;

import java.util.Map;
import java.util.stream.Collectors;

@NonnullByDefault
public abstract class BaseModuleEvent extends AbstractEvent implements NucleusModuleEvent {

    private final NucleusPlugin plugin;
    private final Cause cause;
    private final Map<String, ModuleEnableState> state;

    private BaseModuleEvent(NucleusPlugin plugin) {
        this.cause = Sponge.getCauseStackManager().getCurrentCause();
        this.plugin = plugin;
        this.state = getState();
    }

    @Override
    public Map<String, ModuleEnableState> getModuleList() {
        return this.state;
    }

    @Override
    public Cause getCause() {
        return this.cause;
    }

    Map<String, ModuleEnableState> getState() {
        return this.plugin.getModuleContainer().getModulesWithLoadingState()
                .entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, v -> getFromLoadingStatus(v.getValue())));
    }

    private ModuleEnableState getFromLoadingStatus(LoadingStatus status) {
        if (status == LoadingStatus.DISABLED) {
            return ModuleEnableState.DISABLED;
        } else if (status == LoadingStatus.ENABLED) {
            return ModuleEnableState.ENABLED;
        } else {
            return ModuleEnableState.FORCE_ENABLED;
        }
    }

    public static class AboutToConstructEvent extends BaseModuleEvent implements NucleusModuleEvent.AboutToConstruct {
        public AboutToConstructEvent(NucleusPlugin plugin) {
            super(plugin);
        }

        @Override
        public Map<String, ModuleEnableState> getModuleList() {
            return getState();
        }

        @Override
        public void disableModule(String module, Object plugin) throws UnremovableModuleException, NoModuleException {
            try {
                Sponge.getServiceManager().provideUnchecked(NucleusModuleService.class).removeModule(module, plugin);
            } catch (ModulesLoadedException e) {
                // This shouldn't happen, as this gets called before the registration
                // But, just in case...
                e.printStackTrace();
            }
        }
    }

    public static class AboutToEnable extends BaseModuleEvent implements NucleusModuleEvent.AboutToEnable {

        public AboutToEnable(NucleusPlugin plugin) {
            super(plugin);
        }
    }

    public static class Complete extends BaseModuleEvent implements NucleusModuleEvent.Complete {

        public Complete(NucleusPlugin plugin) {
            super(plugin);
        }
    }

    public static class PreEnable extends BaseModuleEvent implements NucleusModuleEvent.PreEnable {

        public PreEnable(NucleusPlugin plugin) {
            super(plugin);
        }
    }

    public static class Enabled extends BaseModuleEvent implements NucleusModuleEvent.Enabled {

        public Enabled(NucleusPlugin plugin) {
            super(plugin);
        }
    }
}

/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.dataservices.modular;

import co.aikar.timings.Timing;
import co.aikar.timings.Timings;
import com.google.common.collect.ImmutableMap;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.dataservices.AbstractService;
import io.github.nucleuspowered.nucleus.dataservices.dataproviders.DataProvider;
import ninja.leaping.configurate.ConfigurationNode;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class ModularDataService<S extends ModularDataService<S>> extends AbstractService<ConfigurationNode> {

    private final Map<Class<?>, DataModule<S>> cached = new HashMap<>();
    private final Map<Class<?>, TransientModule<S>> transientCache = new HashMap<>();
    private final Timing saveTimings = Timings.of(Nucleus.getNucleus(), "Data Modules - Saving");
    private final Timing loadTimings = Timings.of(Nucleus.getNucleus(), "Data Modules - Loading");
    private final Timing loadTransientTimings = Timings.of(Nucleus.getNucleus(), "Transient Modules - Loading");

    ModularDataService(DataProvider<ConfigurationNode> dataProvider) throws Exception {
        super(dataProvider);
    }

    public <T extends DataModule<S>, R> R quickGet(Class<T> module, Function<T, R> getter) {
        return getter.apply(get(module));
    }

    public <T extends DataModule<S>> void quickSet(Class<T> module, Consumer<T> setter) {
        T m = get(module);
        setter.accept(m);
        set(m);
    }

    @SuppressWarnings("unchecked")
    public final <T extends TransientModule<S>> T getTransient(Class<T> module) {
        if (transientCache.containsKey(module)) {
            return (T)transientCache.get(module);
        }

        try {
            loadTransientTimings.startTimingIfSync();

            T dm;
            Optional<T> m = tryGetTransient(module);
            if (m.isPresent()) {
                dm = m.get();
            } else {
                Nucleus.getNucleus().getLogger()
                        .warn("Attempting to construct " + module.getSimpleName() + " by reflection. Please add this to the factory.");
                dm = module.newInstance();
            }

            setTransient(dm);
            return dm;
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            loadTransientTimings.stopTimingIfSync();
        }
    }

    abstract <T extends TransientModule<S>> Optional<T> tryGetTransient(Class<T> module);

    @SuppressWarnings({"unchecked", "JavaReflectionMemberAccess"})
    public final <T extends DataModule<S>> T get(Class<T> module) {
        if (cached.containsKey(module)) {
            return (T)cached.get(module);
        }

        try {
            loadTimings.startTimingIfSync();
            Optional<T> m = tryGet(module);
            T dm;
            if (m.isPresent()) {
                dm = m.get();
            } else {
                Nucleus.getNucleus().getLogger()
                        .warn("Attempting to construct " + module.getSimpleName() + " by reflection. Please add this to the factory.");

                if (DataModule.ReferenceService.class.isAssignableFrom(module)) {
                    Constructor s = module.getDeclaredConstructor(this.getClass());
                    s.setAccessible(true);
                    dm = (T)s.newInstance(this);
                } else {
                    dm = module.newInstance();
                }
            }

            dm.loadFrom(this.data);
            set(dm);
            return dm;
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            loadTimings.stopTimingIfSync();
        }
    }

    abstract <T extends DataModule<S>> Optional<T> tryGet(Class<T> module);

    public <T extends DataModule<S>> void set(T dataModule) {
        cached.put(dataModule.getClass(), dataModule);
    }

    private <T extends TransientModule<S>> void setTransient(T dataModule) {
        transientCache.put(dataModule.getClass(), dataModule);
    }

    @Override public void loadInternal() throws Exception {
        super.loadInternal();
        cached.clear(); // Only clear if no exception was caught.
    }

    @Override public boolean save() {
        try {
            saveTimings.startTimingIfSync();

            // If there is nothing in the cache, don't save (because we don't need to).
            if (data != null && (!cached.isEmpty() || !(data.isVirtual() || data.getValue() == null))) {
                ImmutableMap.copyOf(cached).values().forEach(x -> x.saveTo(data));
                return super.save();
            }

            return true;
        } finally {
            saveTimings.stopTimingIfSync();
        }
    }
}

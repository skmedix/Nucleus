/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.dataservices.loaders;

import co.aikar.timings.Timing;
import co.aikar.timings.Timings;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.google.common.collect.Sets;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.dataservices.Service;
import io.github.nucleuspowered.nucleus.dataservices.dataproviders.DataProvider;
import io.github.nucleuspowered.nucleus.internal.TimingsDummy;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class DataManager<I, P, S extends Service> {

    private final Collection<I> bypassSave = Sets.newConcurrentHashSet();
    private final Predicate<I> fileExists;
    private final BiFunction<I, Boolean, DataProvider<P>> dataProviderFactory;
    private final LoadingCache<I, S> cache;

    private Timing GENERAL_LOAD_TIMINGS = TimingsDummy.DUMMY;
    private Timing ACTUAL_LOAD_TIMINGS = TimingsDummy.DUMMY;
    private Timing SAVE_TIMINGS = TimingsDummy.DUMMY;
    @Nullable private String name;

    DataManager(BiFunction<I, Boolean, DataProvider<P>> dataProviderFactory, Predicate<I> fileExistsPredicate) {
        this.dataProviderFactory = dataProviderFactory;
        this.fileExists = fileExistsPredicate;
        this.cache = Caffeine.newBuilder()
                .maximumSize(500)
                .removalListener(new Removal())
                .build(new Loader());

        try {
            Nucleus plugin = Nucleus.getNucleus();
            this.GENERAL_LOAD_TIMINGS = Timings.of(plugin, this.getClass().getSimpleName() + " - General");
            this.ACTUAL_LOAD_TIMINGS = Timings.of(plugin, this.getClass().getSimpleName() + " - Loading");
            this.SAVE_TIMINGS = Timings.of(plugin, this.getClass().getSimpleName() + " - Saving");
        } catch (Exception e) {
            // ignored
        }
    }

    private String getClassName() {
        if (this.name == null) {
            this.name = getClass().getSimpleName();
        }

        return this.name;
    }

    private class Removal implements RemovalListener<I, S> {

        @Override
        public void onRemoval(@Nullable I key, @Nullable S value, @Nonnull RemovalCause cause) {
            if (key != null && DataManager.this.bypassSave.remove(key)) {
                // don't save.
                return;
            }

            if (value != null) {
                try {
                    DataManager.this.SAVE_TIMINGS.startTimingIfSync();
                    value.saveInternal();
                } catch (Exception e) {
                    if (Nucleus.getNucleus().isDebugMode()) {
                        Nucleus.getNucleus().getLogger().error("[" + getClassName()  + "] Could not save " + String.valueOf(key) + ".", e);
                    }

                    // Errored. Put value back in cache if we have the key.
                    if (key != null) {
                        Nucleus.getNucleus().getLogger().warn("[" + getClassName() + "] Could not save " + String.valueOf(key) +
                                ", re-adding to cache to try later.");
                        DataManager.this.cache.put(key, value);
                    }

                    return;
                } finally {
                    DataManager.this.SAVE_TIMINGS.stopTimingIfSync();
                }
            }

            if (key != null && shouldNotExpire(key)) {
                DataManager.this.cache.refresh(key);
            }
        }
    }

    private class Loader implements CacheLoader<I, S> {

        @CheckForNull @Override public S load(@Nonnull I key) throws Exception {
            try {
                DataManager.this.GENERAL_LOAD_TIMINGS.startTimingIfSync();
                DataManager.this.ACTUAL_LOAD_TIMINGS.startTimingIfSync();
                DataProvider<P> d = DataManager.this.dataProviderFactory.apply(key, true);
                if (d == null) {
                    return null;
                }

                return getNew(key, d).orElse(null);
            } finally {
                DataManager.this.GENERAL_LOAD_TIMINGS.stopTimingIfSync();
                DataManager.this.ACTUAL_LOAD_TIMINGS.stopTimingIfSync();
            }
        }
    }

    public final boolean has(I data) {
        return this.cache.getIfPresent(data) != null || this.fileExists.test(data);
    }

    public final Optional<S> get(I data) {
        return this.get(data, true);
    }

    public final Optional<S> get(I data, boolean create) {
        if (create || has(data)) {
            return Optional.ofNullable(this.cache.get(data));
        }

        return Optional.empty();
    }

    public final Map<I, S> getAll(Collection<I> keys) {
        return this.cache.getAllPresent(keys);
    }

    protected abstract boolean shouldNotExpire(I key);

    public abstract Optional<S> getNew(I data, DataProvider<P> dataProvider) throws Exception;

    final void invalidate(I key) {
        S value = this.cache.getIfPresent(key);
        if (value != null) {
            this.bypassSave.add(key);
            this.cache.invalidate(key);
        }
    }

    public final void invalidateOld() {
        this.cache.invalidateAll(
                this.cache.asMap().entrySet().stream().filter(x -> !this.shouldNotExpire(x.getKey())).collect(Collectors.toList())
        );
    }

    public final void saveAll() {
        try {
            this.SAVE_TIMINGS.startTimingIfSync();
            for (S s : this.cache.asMap().values()) {
                s.save();
            }
        } finally {
            this.SAVE_TIMINGS.stopTimingIfSync();
        }
    }
}

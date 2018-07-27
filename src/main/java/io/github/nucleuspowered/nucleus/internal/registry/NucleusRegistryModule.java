/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.registry;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import io.github.nucleuspowered.nucleus.annotationprocessor.Store;
import io.github.nucleuspowered.nucleus.internal.CatalogTypeFinalStaticProcessor;
import io.github.nucleuspowered.nucleus.internal.Constants;
import io.github.nucleuspowered.nucleus.internal.annotations.Registry;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.registry.AdditionalCatalogRegistryModule;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Store(Constants.REGISTRY)
@NonnullByDefault
public abstract class NucleusRegistryModule<T extends CatalogType>
        implements AdditionalCatalogRegistryModule<T> {

    private boolean registered = false;
    private final Map<String, T> entries = new HashMap<>();

    public abstract Class<T> catalogClass();

    public abstract void registerDefaults();

    public final void init() throws Exception {
        if (!this.registered) {
            registerDefaults();
            this.registered = true;
            if (getClass().isAnnotationPresent(Registry.class)) {
                for (Class<?> clazz : getClass().getAnnotation(Registry.class).value()) {
                    CatalogTypeFinalStaticProcessor.setFinalStaticFields(clazz, this.entries);
                }
            }
            Sponge.getRegistry().registerModule(catalogClass(), this);
        }
    }

    @Override
    public void registerAdditionalCatalog(T entry) {
        Preconditions.checkNotNull(entry, "entry");
        if (this.entries.containsKey(entry.getId().toLowerCase(Locale.ENGLISH))) {
            throw new IllegalArgumentException("Cannot register that ID as it already has been registered");
        }

        if (this.registered && entry.getId().toLowerCase(Locale.ENGLISH).startsWith("nucleus:")) {
            // no
            throw new IllegalArgumentException("Cannot register that ID, additional catalogs must not start "
                    + "with the nucleus namespace");
        }

        this.entries.put(entry.getId().toLowerCase(Locale.ENGLISH), entry);
    }

    @Override
    public Optional<T> getById(String id) {
        return Optional.ofNullable(this.entries.get(id.toLowerCase(Locale.ENGLISH)));
    }

    @Override
    public Collection<T> getAll() {
        return ImmutableList.copyOf(this.entries.values());
    }
}

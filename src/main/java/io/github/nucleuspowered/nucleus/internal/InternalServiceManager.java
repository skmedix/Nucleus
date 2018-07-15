/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

public final class InternalServiceManager {

    private final Map<Class<?>, Object> serviceMap = Maps.newConcurrentMap();

    public <I, C extends I> void registerService(Class<I> key, C service) {
        registerService(key, service, false);
    }

    public <I, C extends I> void registerService(Class<I> key, C service, boolean rereg) {
        if (!rereg && this.serviceMap.containsKey(key)) {
            return;
        }

        this.serviceMap.put(key, service);
    }

    @SuppressWarnings("unchecked")
    public <I> Optional<I> getService(Class<I> key) {
        if (this.serviceMap.containsKey(key)) {
            return Optional.of((I) this.serviceMap.get(key));
        }

        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public <I> I getServiceUnchecked(Class<I> key) {
        if (this.serviceMap.containsKey(key)) {
            return (I) this.serviceMap.get(key);
        }

        throw new NoSuchElementException(key.getName());
    }
}

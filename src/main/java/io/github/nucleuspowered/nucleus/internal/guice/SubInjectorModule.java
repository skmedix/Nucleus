/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.guice;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;

import java.util.List;

@SuppressWarnings("unchecked")
public class SubInjectorModule extends AbstractModule {

    private final List<TypeHolder> t = Lists.newArrayList();

    public <T> void addInjection(Class<T> clazz, T toInject) {
        this.t.add(new TypeHolder(clazz, toInject));
    }

    public boolean isEmpty() {
        return this.t.isEmpty();
    }

    @Override
    protected void configure() {
        this.t.forEach(k -> bind(k.clazz).toInstance(k.clazz.cast(k.instance)));
    }

    private class TypeHolder<T> {
        private final Class<T> clazz;
        private final T instance;

        private TypeHolder(Class<T> clazz, T instance) {
            this.clazz = clazz;
            this.instance = instance;
        }
    }
}

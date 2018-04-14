/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.annotations;

import io.github.nucleuspowered.nucleus.internal.InternalServiceManager;
import org.spongepowered.api.service.ServiceManager;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Instructs Nucleus to register a service in the InternalServiceManager,
 * and, if appropriate, the corresponding API in Sponge's
 * {@link ServiceManager}
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(RegisterServices.class)
public @interface RegisterService {

    /**
     * The type of service to register in the {@link InternalServiceManager}
     *
     * @return The service
     */
    Class<?> value();

    /**
     * The API service to register. Must be a superclass of
     * {@link #value()}, or this will be ignored.
     *
     * @return The API service to register, if any.
     */
    Class<?> apiService() default Object.class;

    /**
     * Whether this could replace an internal service.
     *
     * @return true if so
     */
    boolean replaceInternal() default false;

    boolean optional() default false;
}

/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.annotations.command;

import java.lang.annotation.*;

/**
 * Signifies that the name of the command gets cooldowns etc. from another section
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface RedirectModifiers {
    /**
     * The command name to use in the config file.
     *
     * @return The name.
     */
    String value();

    /**
     * Whether to attempt to generate entries in the config file.
     *
     * @return true if so
     */
    boolean requireGeneration() default false;
}

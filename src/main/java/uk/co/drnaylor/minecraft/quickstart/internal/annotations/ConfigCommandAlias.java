/*
 * This file is part of QuickStart, licensed under the MIT License (MIT). See the LICENCE.txt file
 * at the root of this project for more details.
 */
package uk.co.drnaylor.minecraft.quickstart.internal.annotations;

import java.lang.annotation.*;

/**
 * Signifies that the name of the command
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface ConfigCommandAlias {
    /**
     * The command name to use in the config file.
     *
     * @return The name.
     */
    String value();

    /**
     * Whether to ask the system to generate the default config.
     *
     * @return <code>true</code> if so, <code>false</code> otherwise.
     */
    boolean generate() default true;
}

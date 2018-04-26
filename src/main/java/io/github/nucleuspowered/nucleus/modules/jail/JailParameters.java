/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.jail;

import io.github.nucleuspowered.nucleus.argumentparsers.JailArgument;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;

public final class JailParameters {

    private JailParameters() {}

    public static final String JAIL_KEY = "jail";

    public static final CommandElement JAIL = new JailArgument(Text.of(JAIL_KEY));

    public static final CommandElement OPTIONAL_JAIL = GenericArguments.optional(JAIL);
}

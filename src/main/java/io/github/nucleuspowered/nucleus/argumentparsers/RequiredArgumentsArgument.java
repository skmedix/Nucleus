/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.argumentparsers;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;

import java.util.List;

import javax.annotation.Nullable;

public class RequiredArgumentsArgument extends CommandElement {

    private final CommandElement wrapped;
    private final int min;
    private final int max;

    public static RequiredArgumentsArgument r2(CommandElement element) {
        return new RequiredArgumentsArgument(element, 2);
    }

    public RequiredArgumentsArgument(CommandElement element, int minimumRequired) {
        this(element, minimumRequired, Integer.MAX_VALUE);
    }

    public RequiredArgumentsArgument(CommandElement element, int minimumRequired, int maximumRequired) {
        super(element.getKey());
        this.wrapped = element;
        this.min = minimumRequired;
        this.max = maximumRequired;
    }

    @Nullable @Override protected Object parseValue(CommandSource source, CommandArgs args) {
        return null;
    }

    @Override public void parse(CommandSource source, CommandArgs args, CommandContext context) throws ArgumentParseException {
        int size = args.getAll().size();
        if (this.min <= size && this.max >= size) {
            this.wrapped.parse(source, args, context);
        }
    }

    @Override public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        return this.wrapped.complete(src, args, context);
    }
}

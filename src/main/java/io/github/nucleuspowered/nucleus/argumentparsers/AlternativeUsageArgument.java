/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.argumentparsers;

import io.github.nucleuspowered.nucleus.argumentparsers.util.WrappedElement;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.function.Function;

import javax.annotation.Nullable;

public class AlternativeUsageArgument extends WrappedElement {

    private final Function<CommandSource, Text> usage;

    public AlternativeUsageArgument(CommandElement wrappedElement, Function<CommandSource, Text> usage) {
        super(wrappedElement);
        this.usage = usage;
    }

    @Nullable @Override protected Object parseValue(CommandSource source, CommandArgs args) {
        return null;
    }

    @Override public void parse(CommandSource source, CommandArgs args, CommandContext context) throws ArgumentParseException {
        getWrappedElement().parse(source, args, context);
    }

    @Override public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        return getWrappedElement().complete(src, args, context);
    }

    @Override public Text getUsage(CommandSource src) {
        return this.usage.apply(src);
    }
}

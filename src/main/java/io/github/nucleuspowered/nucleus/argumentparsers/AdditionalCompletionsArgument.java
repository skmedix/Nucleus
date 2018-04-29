/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.argumentparsers;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.argumentparsers.util.WrappedElement;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;

import java.util.List;
import java.util.function.BiFunction;

import javax.annotation.Nullable;

public class AdditionalCompletionsArgument extends WrappedElement {

    private final BiFunction<CommandSource, String, List<String>> additional;
    private final int minArgs;
    private final int maxArgs;

    public AdditionalCompletionsArgument(CommandElement wrapped, int min, int max, BiFunction<CommandSource, String, List<String>> additional) {
        super(wrapped);
        this.additional = additional;
        this.maxArgs = max;
        this.minArgs = min;
    }


    @Nullable @Override protected Object parseValue(CommandSource source, CommandArgs args) {
        return null;
    }

    @Override public void parse(CommandSource source, CommandArgs args, CommandContext context) throws ArgumentParseException {
        getWrappedElement().parse(source, args, context);
    }

    @Override public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        List<String> s = getWrappedElement().complete(src, args, context);

        if (args.getAll().size() >= this.minArgs && args.getAll().size() <= this.maxArgs) {
            try {
                String a = args.peek();
                List<String> result = Lists.newArrayList(s);
                result.addAll(this.additional.apply(src, a));
                return result;
            } catch (ArgumentParseException e) {
                // ignored
            }
        }

        return s;
    }
}

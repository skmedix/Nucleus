/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.argumentparsers;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.Nucleus;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import java.util.List;

import javax.annotation.Nullable;

public class PositiveIntegerArgument extends CommandElement {

    private final boolean allowZero;

    public PositiveIntegerArgument(@Nullable Text key) {
        this(key, true);
    }

    public PositiveIntegerArgument(@Nullable Text key, boolean allowZero) {
        super(key);
        this.allowZero = allowZero;
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        try {
            int a = Integer.parseUnsignedInt(args.next());
            if (this.allowZero || a != 0) {
                return a;
            }

            throw args.createError(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("args.positiveint.zero"));
        } catch (NumberFormatException e) {
            throw args.createError(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("args.positiveint.negative"));
        }
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        return Lists.newArrayList();
    }
}

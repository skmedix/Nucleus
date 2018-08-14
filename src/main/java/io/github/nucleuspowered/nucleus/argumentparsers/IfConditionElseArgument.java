/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.argumentparsers;

import io.github.nucleuspowered.nucleus.Nucleus;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;

import java.util.List;
import java.util.function.BiPredicate;

import javax.annotation.Nullable;

public class IfConditionElseArgument extends CommandElement {

    private final BiPredicate<CommandSource, CommandContext> predicate;
    private final CommandElement trueElement;
    private final CommandElement falseElement;

    public static IfConditionElseArgument permission(String permission,
            CommandElement ifSo, CommandElement ifNot) {
        return new IfConditionElseArgument(ifSo, ifNot, (s, c) -> Nucleus.getNucleus().getPermissionResolver().hasPermission(s, permission));
    }

    public IfConditionElseArgument(CommandElement trueElement, CommandElement falseElement,
            BiPredicate<CommandSource, CommandContext> predicate) {
        super(trueElement.getKey());
        this.trueElement = trueElement;
        this.falseElement = falseElement;
        this.predicate = predicate;
    }

    @Nullable @Override protected Object parseValue(CommandSource source, CommandArgs args) {
        return null;
    }

    @Override public void parse(CommandSource source, CommandArgs args, CommandContext context) throws ArgumentParseException {
        if (this.predicate.test(source, context)) {
            this.trueElement.parse(source, args, context);
        } else {
            this.falseElement.parse(source, args, context);
        }
    }

    @Override public List<String> complete(CommandSource source, CommandArgs args, CommandContext context) {
        if (this.predicate.test(source, context)) {
            return this.trueElement.complete(source, args, context);
        } else {
            return this.falseElement.complete(source, args, context);
        }
    }
}

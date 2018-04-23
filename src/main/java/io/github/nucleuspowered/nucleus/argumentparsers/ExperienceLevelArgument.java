/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.argumentparsers;

import com.google.common.collect.ImmutableList;
import io.github.nucleuspowered.nucleus.Nucleus;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

/**
 * Takes an argument of the form "l30" or "lv30" or "l:30" or "lv:30". Returns
 * the integer.
 */
@NonnullByDefault
public class ExperienceLevelArgument extends CommandElement {

    private final Pattern argumentPattern = Pattern.compile("^(l|lv|l:|lv:)?(\\d+)(l|lv)?$", Pattern.CASE_INSENSITIVE);

    public ExperienceLevelArgument(@Nullable Text key) {
        super(key);
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        Matcher m = this.argumentPattern.matcher(args.next());
        if (m.find(0) && m.group(1) != null || m.group(3) != null ) {
            return Integer.parseInt(m.group(2));
        }

        throw args.createError(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("args.explevel.error"));
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        return ImmutableList.of();
    }
}

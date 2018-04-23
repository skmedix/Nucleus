/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.argumentparsers;

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import java.util.List;

import javax.annotation.Nullable;

public class MarkerArgument extends CommandElement {

    public final static String MARKER = "marker";

    public MarkerArgument() {
        super(Text.of(MARKER));
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) {
        return true;
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        return ImmutableList.of();
    }

    @Override public Text getUsage(CommandSource src) {
        return Text.of();
    }
}

/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.command;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.messages.MessageProvider;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.annotation.Nullable;

public class NucleusArgumentParseException extends ArgumentParseException {

    @Nullable private final Text subcommands;
    @Nullable private final Text usage;
    private final boolean isEnd;

    public static NucleusArgumentParseException from(ArgumentParseException exception, @Nullable Text usage, @Nullable Text subcommands) {
        return new NucleusArgumentParseException(
            Text.of(TextColors.RED, exception.getMessage()),
            "",
            exception.getPosition(),
            usage,
            subcommands,
            exception instanceof NucleusArgumentParseException && ((NucleusArgumentParseException) exception).isEnd()
        );
    }

    public NucleusArgumentParseException(Text message, String source, int position, @Nullable Text usage, @Nullable Text subcommands, boolean isEnd) {
        super(message, source, position);
        this.usage = usage;
        this.subcommands = subcommands;
        this.isEnd = isEnd;
    }

    @Override public Text getText() {
        Text t = super.getText();
        if (this.usage == null && this.subcommands == null) {
            return t;
        }

        return Text.join(t, Text.NEW_LINE, getUsage());
    }

    @Nullable public Text getUsage() {
        Text.Builder builder = Text.builder();
        MessageProvider mp = Nucleus.getNucleus().getMessageProvider();
        if (this.usage != null) {
            builder.append(Text.NEW_LINE).append(mp.getTextMessageWithTextFormat("command.exception.usage", this.usage));
        }

        if (this.subcommands != null) {
            builder.append(Text.NEW_LINE).append(mp.getTextMessageWithTextFormat("command.exception.subcommands", this.subcommands));
        }

        return builder.build();
    }

    @Override public boolean shouldIncludeUsage() {
        return false;
    }

    public boolean isEnd() {
        return this.isEnd;
    }
}

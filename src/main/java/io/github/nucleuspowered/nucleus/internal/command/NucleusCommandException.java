/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.command;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.messages.MessageProvider;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandPermissionException;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.Tuple;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

/**
 * Wrapper exception that contains a list of exceptions to pass down that may have been caused during execution.
 */
public class NucleusCommandException extends CommandException {

    private final boolean allowFallback;
    private final List<Tuple<String, CommandException>> exceptions;
    private Boolean overrideUsage = null;

    public NucleusCommandException(List<Tuple<String, CommandException>> exception) {
        this(exception, true);
    }

    public NucleusCommandException(List<Tuple<String, CommandException>> exception, boolean allowFallback) {
        super(Text.EMPTY);
        this.exceptions = exception;
        this.allowFallback = allowFallback;
    }

    public List<Tuple<String, CommandException>> getExceptions() {
        return this.exceptions;
    }

    @Nullable @Override public Text getText() {
        if (this.exceptions.isEmpty()) {
            // Unable to get the error.
            return Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.exception.nomoreinfo");
        }

        MessageProvider mp = Nucleus.getNucleus().getMessageProvider();

        // Is it only command permission exceptions?
        if (this.exceptions.stream().allMatch(x -> CommandPermissionException.class.isInstance(x.getSecond()))) {
            return this.exceptions.get(0).getSecond().getText();
        }

        if (this.exceptions.stream().allMatch(x -> {
            CommandException e = x.getSecond();
            return e instanceof NucleusArgumentParseException && ((NucleusArgumentParseException) e).isEnd();
        })) {
            if (this.exceptions.size() == 1) {
                Tuple<String, CommandException> exceptionTuple = this.exceptions.get(0);
                return Text.of(mp.getTextMessageWithFormat("command.exception.fromcommand", exceptionTuple.getFirst()),
                        Text.NEW_LINE, TextColors.RED, exceptionTuple.getSecond().getText());
            } else {
                return print(this.exceptions);
            }
        }

        List<Tuple<String, CommandException>> lce = this.exceptions.stream()
                .filter(x -> {
                    CommandException e = x.getSecond();
                    return !(e instanceof NucleusArgumentParseException) || !((NucleusArgumentParseException) e).isEnd();
                })
                .filter(x -> !CommandPermissionException.class.isInstance(x))
                .collect(Collectors.toList());
        if (lce.size() == 1) {
            Tuple<String, CommandException> exceptionTuple = this.exceptions.get(0);
            return Text.of(mp.getTextMessageWithFormat("command.exception.fromcommand", exceptionTuple.getFirst()),
                    Text.NEW_LINE, TextColors.RED, exceptionTuple.getSecond().getText());
        }

        return print(lce);
    }

    private Text print(List<Tuple<String, CommandException>> lce) {
        MessageProvider mp = Nucleus.getNucleus().getMessageProvider();
        Text sept = mp.getTextMessageWithFormat("command.exception.separator");
        Text.Builder builder = mp.getTextMessageWithFormat("command.exception.multiple")
                .toBuilder();
        lce.forEach(x -> builder.append(Text.NEW_LINE).append(sept)
                .append(Text.NEW_LINE)
                .append(mp.getTextMessageWithFormat("command.exception.fromcommand", x.getFirst()))
                .append(Text.NEW_LINE)
                .append(x.getSecond().getText()));

        builder.append(Text.NEW_LINE).append(sept);

        return builder.toText();
    }

    public void setOverrideUsage(Boolean overrideUsage) {
        this.overrideUsage = overrideUsage;
    }

    @Override public boolean shouldIncludeUsage() {
        return this.overrideUsage == null ? super.shouldIncludeUsage() : this.overrideUsage;
    }

    public boolean isAllowFallback() {
        return this.allowFallback;
    }
}

/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.argumentparsers;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.internal.traits.InternalServiceManagerTrait;
import io.github.nucleuspowered.nucleus.modules.message.handlers.MessageHandler;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public class MessageTargetArgument extends CommandElement implements InternalServiceManagerTrait {

    private final MessageHandler messageHandler = getServiceUnchecked(MessageHandler.class);

    public MessageTargetArgument(@Nullable Text key) {
        super(key);
    }

    @Nullable @Override protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        return this.messageHandler.getTarget(args.next().toLowerCase()).orElseThrow(() -> args.createError(Text.of("No bot exists with that name")));
    }

    @Override public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        List<String> m = Lists.newArrayList(this.messageHandler.getTargetNames().keySet());
        try {
            String a = args.peek().toLowerCase();
            return m.stream().filter(x -> x.startsWith(a)).collect(Collectors.toList());
        } catch (ArgumentParseException e) {
            return m;
        }
    }
}

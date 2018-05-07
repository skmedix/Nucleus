/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.core.commands;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoHelpSubcommand;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.messages.MessageProvider;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.ChildCommandElementExecutor;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

@NoModifiers
@Permissions
@NoHelpSubcommand
@RegisterCommand("commandinfo")
public class CommandInfoCommand extends AbstractCommand<CommandSource> {

    private final String commandKey = "command";

    @Override
    protected CommandElement[] getArguments() {
        return new CommandElement[] {
                new CommandChoicesArgument()
        };
    }

    @Override
    protected CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        // we have the command, get the mapping
        CommandMapping mapping = args.<CommandMapping>getOne(this.commandKey).get();

        MessageProvider provider = Nucleus.getNucleus().getMessageProvider();
        Text header = provider.getTextMessageWithFormat("command.commandinfo.title", mapping.getPrimaryAlias());

        List<Text> content = Lists.newArrayList();

        // Owner
        content.add(provider.getTextMessageWithFormat("command.commandinfo.owner", Sponge.getCommandManager().getOwner(mapping)
                .map(x -> x.getName() + " (" + x.getId() + ")")
                .orElseGet(() -> provider.getMessageWithFormat("standard.unknown"))));
        content.add(provider.getTextMessageWithFormat("command.commandinfo.aliases", String.join(", ", mapping.getAllAliases())));

        if (mapping.getCallable() instanceof AbstractCommand) {
            nucleusCommand(content, src, provider, (AbstractCommand<? extends CommandSource>) mapping.getCallable());
        } else if (mapping.getCallable() instanceof CommandSpec) {
            specCommand(content, src, provider, mapping.getPrimaryAlias(), (CommandSpec) mapping.getCallable());
        } else {
            lowCommand(content, src, provider, mapping.getPrimaryAlias(), mapping.getCallable());
        }

        Util.getPaginationBuilder(src)
                .title(header)
                .contents(content)
                .sendTo(src);
        return CommandResult.success();
    }

    private void nucleusCommand(List<Text> content, CommandSource source, MessageProvider provider,
            AbstractCommand<? extends CommandSource> abstractCommand) {
        content.add(provider.getTextMessageWithFormat("command.commandinfo.type", provider.getMessageWithFormat("command.commandinfo.nucleus")));
        content.add(Text.EMPTY);
        List<Text> text = abstractCommand.getUsageText(source);
        if (text.isEmpty()) {
            content.add(provider.getTextMessageWithFormat("command.commandinfo.noinfo"));
        } else {
            content.addAll(abstractCommand.getUsageText(source));
        }
    }

    private void specCommand(List<Text> content, CommandSource source, MessageProvider provider, String alias, CommandSpec spec) {
        content.add(provider.getTextMessageWithFormat("command.commandinfo.type", provider.getMessageWithFormat("command.commandinfo.spec")));
        CommandExecutor executor = spec.getExecutor();
        if (executor instanceof ChildCommandElementExecutor) {
            try {
                content.add(provider.getTextMessageWithFormat("command.commandinfo.haschildren"));
                Field field = ChildCommandElementExecutor.class.getDeclaredField("fallbackExecutor");
                field.setAccessible(true);
                content.add(provider.getTextMessageWithFormat("command.commandinfo.execclass", field.get(executor).getClass().getName()));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                content.add(provider.getTextMessageWithFormat("command.commandinfo.execclass", provider.getMessageWithFormat("standard.unknown")));
            }
        }

        content.add(Text.EMPTY);
        content.add(provider.getTextMessageWithFormat("command.commandinfo.description"));

        spec.getShortDescription(source).ifPresent(x -> {
            content.add(provider.getTextMessageWithFormat("command.commandinfo.shortdescription"));
            content.add(x);
            content.add(Text.EMPTY);
        });
        spec.getExtendedDescription(source).ifPresent(x -> {
            content.add(provider.getTextMessageWithFormat("command.commandinfo.description"));
            content.add(x);
            content.add(Text.EMPTY);
        });

        content.add(provider.getTextMessageWithTextFormat("command.commandinfo.usage"));
        content.add(Text.of("/", alias, " ", spec.getUsage(source)));
    }

    private void lowCommand(List<Text> content, CommandSource source, MessageProvider provider, String alias, CommandCallable callable) {
        content.add(provider.getTextMessageWithFormat("command.commandinfo.type", provider.getMessageWithFormat("command.commandinfo.callable")));
        content.add(Text.EMPTY);

        callable.getShortDescription(source).ifPresent(x -> {
            content.add(provider.getTextMessageWithFormat("command.commandinfo.shortdescription"));
            content.add(x);
            content.add(Text.EMPTY);
        });
        callable.getHelp(source).ifPresent(x -> {
            content.add(provider.getTextMessageWithFormat("command.commandinfo.description"));
            content.add(x);
            content.add(Text.EMPTY);
        });

        content.add(provider.getTextMessageWithTextFormat("command.commandinfo.usage"));
        content.add(Text.of("/", alias, " ", callable.getUsage(source)));
    }

    private class CommandChoicesArgument extends CommandElement {

        protected CommandChoicesArgument() {
            super(Text.of(CommandInfoCommand.this.commandKey));
        }

        @Nullable
        @Override
        protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
            String next = args.next();
            return Sponge.getCommandManager().get(next).orElseThrow(() -> args.createError(
                    Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.commandinfo.nocommand", next)
            ));
        }

        @Override
        public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
            try {
                final String s = args.peek().toLowerCase();
                return Sponge.getCommandManager().getAliases().stream().filter(x -> x.toLowerCase().startsWith(s)).collect(Collectors.toList());
            } catch (ArgumentParseException e) {
                return Lists.newArrayList(Sponge.getCommandManager().getAliases());
            }
        }
    }
}

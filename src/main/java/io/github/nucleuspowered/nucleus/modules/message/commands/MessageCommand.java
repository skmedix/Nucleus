/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.message.commands;

import io.github.nucleuspowered.nucleus.argumentparsers.MessageTargetArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.NicknameArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.PlayerConsoleArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.SelectorWrapperArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NotifyIfAFK;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.message.handlers.MessageHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.HashMap;
import java.util.Map;

/**
 * Messages a player.
 */
@Permissions(suggestedLevel = SuggestedLevel.USER, supportsSelectors = true)
@RegisterCommand(value = { "message", "m", "msg", "whisper", "w", "t" }, rootAliasRegister = { "tell" })
@EssentialsEquivalent({"msg", "tell", "m", "t", "whisper"})
@NonnullByDefault
@NotifyIfAFK(MessageCommand.TO)
public class MessageCommand extends AbstractCommand<CommandSource> {
    final static String TO = "to";

    private final MessageHandler handler = getServiceUnchecked(MessageHandler.class);

    @Override
    protected Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> mp = new HashMap<>();
        mp.put("color", PermissionInformation.getWithTranslation("permission.message.color", SuggestedLevel.ADMIN));
        mp.put("colour", PermissionInformation.getWithTranslation("permission.message.colour", SuggestedLevel.ADMIN));
        mp.put("style", PermissionInformation.getWithTranslation("permission.message.style", SuggestedLevel.ADMIN));
        mp.put("magic", PermissionInformation.getWithTranslation("permission.message.magic", SuggestedLevel.ADMIN));
        mp.put("url", PermissionInformation.getWithTranslation("permission.message.urls", SuggestedLevel.ADMIN));
        return mp;
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.firstParsing(
                    new MessageTargetArgument(Text.of(TO)),
                    SelectorWrapperArgument.nicknameSelector(Text.of(TO), NicknameArgument.UnderlyingType.PLAYER_CONSOLE,
                            true, Player.class, (c, p) -> PlayerConsoleArgument.shouldShow(p, c))
            ),
            NucleusParameters.MESSAGE
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) {
        boolean b = this.handler.sendMessage(src, args.<CommandSource>getOne(TO).get(), args.<String>getOne(NucleusParameters.Keys.MESSAGE).get());
        return b ? CommandResult.success() : CommandResult.empty();
    }
}

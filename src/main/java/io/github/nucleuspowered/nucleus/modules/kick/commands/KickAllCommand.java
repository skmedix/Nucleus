/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kick.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Permissions(suggestedLevel = SuggestedLevel.MOD)
@NoModifiers
@NonnullByDefault
@RegisterCommand("kickall")
@EssentialsEquivalent("kickall")
public class KickAllCommand extends AbstractCommand<CommandSource> {

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                GenericArguments.flags()
                        .permissionFlag(this.permissions.getPermissionWithSuffix("whitelist"), "w", "f")
                        .buildWith(NucleusParameters.OPTIONAL_REASON)
        };
    }

    @Override
    public Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> m = new HashMap<>();
        m.put("whitelist", PermissionInformation.getWithTranslation("permission.kickall.whitelist", SuggestedLevel.ADMIN));
        return m;
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) {
        String r = args.<String>getOne(NucleusParameters.Keys.REASON)
                .orElseGet(() -> Nucleus.getNucleus().getMessageProvider().getMessageWithFormat("command.kick.defaultreason"));
        boolean f = args.<Boolean>getOne("w").orElse(false);

        if (f) {
            Sponge.getServer().setHasWhitelist(true);
        }

        // Don't kick self
        Sponge.getServer().getOnlinePlayers().stream()
                .filter(x -> !(src instanceof Player) || !((Player) src).getUniqueId().equals(x.getUniqueId()))
                .collect(Collectors.toList())
                .forEach(x -> x.kick(TextSerializers.FORMATTING_CODE.deserialize(r)));

        MessageChannel mc = MessageChannel.fixed(Sponge.getServer().getConsole(), src);
        mc.send(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.kickall.message"));
        mc.send(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.reason", r));
        if (f) {
            mc.send(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.kickall.whitelist"));
        }

        return CommandResult.success();
    }
}

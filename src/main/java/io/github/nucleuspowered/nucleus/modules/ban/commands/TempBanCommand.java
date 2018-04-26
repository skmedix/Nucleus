/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.ban.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.ban.config.BanConfig;
import io.github.nucleuspowered.nucleus.modules.ban.config.BanConfigAdapter;
import io.github.nucleuspowered.nucleus.util.PermissionMessageChannel;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.ban.BanService;
import org.spongepowered.api.text.channel.MutableMessageChannel;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.util.ban.Ban;
import org.spongepowered.api.util.ban.BanTypes;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@RegisterCommand("tempban")
@Permissions(suggestedLevel = SuggestedLevel.MOD)
@NoModifiers
@EssentialsEquivalent("tempban")
@NonnullByDefault
public class TempBanCommand extends AbstractCommand<CommandSource> implements Reloadable {

    private BanConfig banConfig = new BanConfig();

    @Override public void onReload() {
        this.banConfig = Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(BanConfigAdapter.class).getNodeOrDefault();
    }

    @Override
    public Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> m = new HashMap<>();
        m.put("offline", PermissionInformation.getWithTranslation("permission.tempban.offline", SuggestedLevel.MOD));
        m.put("exempt.target", PermissionInformation.getWithTranslation("permission.tempban.exempt.target", SuggestedLevel.MOD));
        m.put("exempt.length", PermissionInformation.getWithTranslation("permission.tempban.exempt.length", SuggestedLevel.MOD));
        return m;
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                NucleusParameters.ONE_USER,
                NucleusParameters.DURATION,
                NucleusParameters.OPTIONAL_REASON
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        User u = args.<User>getOne(NucleusParameters.Keys.USER).get();
        Long time = args.<Long>getOne(NucleusParameters.Keys.DURATION).get();
        String reason = args.<String>getOne(NucleusParameters.Keys.REASON)
                .orElseGet(() -> Nucleus.getNucleus().getMessageProvider().getMessageWithFormat("ban.defaultreason"));

        if (this.permissions.testSuffix(u, "exempt.target", src, false)) {
            throw new ReturnMessageException(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.tempban.exempt", u.getName()));
        }

        if (!u.isOnline() && !this.permissions.testSuffix(src, "offline")) {
            throw new ReturnMessageException(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.tempban.offline.noperms"));
        }

        if (time > this.banConfig.getMaximumTempBanLength() && this.banConfig.getMaximumTempBanLength() != -1 &&
                !this.permissions.testSuffix(src, "exempt.length")) {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.tempban.length.toolong",
                    Util.getTimeStringFromSeconds(this.banConfig.getMaximumTempBanLength())));
            return CommandResult.success();
        }

        BanService service = Sponge.getServiceManager().provideUnchecked(BanService.class);

        if (service.isBanned(u.getProfile())) {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.ban.alreadyset", u.getName()));
            return CommandResult.empty();
        }

        // Expiration date
        Instant date = Instant.now().plus(time, ChronoUnit.SECONDS);

        // Create the ban.
        Ban bp = Ban.builder().type(BanTypes.PROFILE).profile(u.getProfile()).source(src).expirationDate(date).reason(TextSerializers.FORMATTING_CODE.deserialize(reason)).build();
        service.addBan(bp);

        MutableMessageChannel send = new PermissionMessageChannel(BanCommand.notifyPermission).asMutable();
        send.addMember(src);
        send.send(Nucleus.getNucleus()
                .getMessageProvider().getTextMessageWithFormat("command.tempban.applied", u.getName(), Util.getTimeStringFromSeconds(time), src
                        .getName()));
        send.send(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("standard.reasoncoloured", reason));

        if (Sponge.getServer().getPlayer(u.getUniqueId()).isPresent()) {
            Sponge.getServer().getPlayer(u.getUniqueId()).get().kick(TextSerializers.FORMATTING_CODE.deserialize(reason));
        }

        return CommandResult.success();
    }
}

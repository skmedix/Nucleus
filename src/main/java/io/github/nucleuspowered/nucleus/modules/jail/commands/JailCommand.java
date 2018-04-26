/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.jail.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.LocationData;
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
import io.github.nucleuspowered.nucleus.modules.jail.JailParameters;
import io.github.nucleuspowered.nucleus.modules.jail.config.JailConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.jail.data.JailData;
import io.github.nucleuspowered.nucleus.modules.jail.handlers.JailHandler;
import io.github.nucleuspowered.nucleus.util.CauseStackHelper;
import io.github.nucleuspowered.nucleus.util.PermissionMessageChannel;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MutableMessageChannel;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Locatable;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@NonnullByDefault
@Permissions(suggestedLevel = SuggestedLevel.MOD, supportsSelectors = true)
@NoModifiers
@RegisterCommand({"jail", "unjail", "togglejail"})
@EssentialsEquivalent({"togglejail", "tjail", "unjail", "jail"})
public class JailCommand extends AbstractCommand<CommandSource> implements Reloadable {

    private final JailHandler handler = Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(JailHandler.class);

    private boolean requireUnjailPermission = false;

    @Override
    public Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> m = new HashMap<>();
        m.put("unjail", PermissionInformation.getWithTranslation("permission.jail.unjail", SuggestedLevel.MOD));
        m.put("notify", PermissionInformation.getWithTranslation("permission.jail.notify", SuggestedLevel.MOD));
        m.put("offline", PermissionInformation.getWithTranslation("permission.jail.offline", SuggestedLevel.MOD));
        m.put("teleportjailed", PermissionInformation.getWithTranslation("permission.jail.teleportjailed", SuggestedLevel.ADMIN));
        m.put("teleporttojailed", PermissionInformation.getWithTranslation("permission.jail.teleporttojailed", SuggestedLevel.ADMIN));
        m.put("exempt.target", PermissionInformation.getWithTranslation("permission.jail.exempt.target", SuggestedLevel.ADMIN));
        return m;
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                NucleusParameters.ONE_USER,
                JailParameters.OPTIONAL_JAIL,
                NucleusParameters.OPTIONAL_WEAK_DURATION,
                NucleusParameters.OPTIONAL_REASON
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        // Get the subject.
        User pl = args.<User>getOne(NucleusParameters.Keys.USER).get();
        if (!pl.isOnline() && !this.permissions.testSuffix(src, "offline")) {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.jail.offline.noperms"));
            return CommandResult.empty();
        }

        if (this.handler.isPlayerJailed(pl)) {
            if (!this.requireUnjailPermission || this.permissions.testSuffix(src, "unjail")) {
                return onUnjail(src, args, pl);
            }

            throw ReturnMessageException.fromKey("command.jail.unjail.perm");
        } else {
            if (this.permissions.testSuffix(pl, "exempt.target", src, false)) { // only for jailing
                throw ReturnMessageException.fromKey("command.jail.exempt", pl.getName());
            }

            return onJail(src, args, pl);
        }
    }

    private CommandResult onUnjail(CommandSource src, CommandContext args, User user) throws ReturnMessageException {
        if (CauseStackHelper.createFrameWithCausesWithReturn(c -> this.handler.unjailPlayer(user), src)) {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.jail.unjail.success", user.getName()));
            return CommandResult.success();
        } else {
            throw ReturnMessageException.fromKey("command.jail.unjail.fail", user.getName());
        }
    }

    private CommandResult onJail(CommandSource src, CommandContext args, User user) throws ReturnMessageException {
        Optional<LocationData> owl = args.getOne(JailParameters.JAIL_KEY);
        if (!owl.isPresent()) {
            throw ReturnMessageException.fromKey("command.jail.jail.nojail");
        }

        // This might not be there.
        Optional<Long> duration = args.getOne(NucleusParameters.Keys.DURATION);
        String reason = args.<String>getOne(NucleusParameters.Keys.REASON)
                .orElseGet(() -> Nucleus.getNucleus().getMessageProvider().getMessageWithFormat("command.jail.reason"));
        JailData jd;
        Text message;
        Text messageTo;

        if (duration.isPresent()) {
            if (user.isOnline()) {
                jd = new JailData(Util.getUUID(src), owl.get().getName(), reason, user.getPlayer().get().getLocation(),
                        Instant.now().plusSeconds(duration.get()));
            } else {
                jd = new JailData(Util.getUUID(src), owl.get().getName(), reason, null, Duration.of(duration.get(), ChronoUnit.SECONDS));
            }

            message = Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.checkjail.jailedfor", user.getName(), jd.getJailName(),
                    src.getName(), Util.getTimeStringFromSeconds(duration.get()));
            messageTo = Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.jail.jailedfor", owl.get().getName(), src.getName(),
                    Util.getTimeStringFromSeconds(duration.get()));
        } else {
            jd = new JailData(Util.getUUID(src), owl.get().getName(), reason, user.getPlayer().map(Locatable::getLocation).orElse(null));
            message = Nucleus.getNucleus()
                    .getMessageProvider().getTextMessageWithFormat("command.checkjail.jailedperm", user.getName(), owl.get().getName(), src.getName());
            messageTo = Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.jail.jailedperm", owl.get().getName(), src.getName());
        }

        if (this.handler.jailPlayer(user, jd)) {
            MutableMessageChannel mc = new PermissionMessageChannel(this.permissions.getPermissionWithSuffix("notify")).asMutable();
            mc.addMember(src);
            mc.send(message);
            mc.send(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("standard.reasoncoloured", reason));

            user.getPlayer().ifPresent(x -> {
                x.sendMessage(messageTo);
                x.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("standard.reasoncoloured", reason));
            });

            return CommandResult.success();
        }

        throw ReturnMessageException.fromKey("command.jail.error");
    }

    @Override
    public void onReload() {
        this.requireUnjailPermission = Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(JailConfigAdapter.class)
                .getNodeOrDefault().isRequireUnjailPermission();
    }
}

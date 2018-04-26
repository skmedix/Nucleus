/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.warn.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.warn.config.WarnConfig;
import io.github.nucleuspowered.nucleus.modules.warn.config.WarnConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.warn.data.WarnData;
import io.github.nucleuspowered.nucleus.modules.warn.handlers.WarnHandler;
import io.github.nucleuspowered.nucleus.util.CauseStackHelper;
import io.github.nucleuspowered.nucleus.util.PermissionMessageChannel;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.channel.MutableMessageChannel;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Permissions(suggestedLevel = SuggestedLevel.MOD)
@NoModifiers
@NonnullByDefault
@RegisterCommand({"warn", "warning", "addwarning"})
public class WarnCommand extends AbstractCommand<CommandSource> implements Reloadable {

    private final WarnHandler handler = getServiceUnchecked(WarnHandler.class);

    private WarnConfig warnConfig = new WarnConfig();

    @Override public void onReload() {
        this.warnConfig = getServiceUnchecked(WarnConfigAdapter.class).getNodeOrDefault();
    }

    @Override
    public Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> m = new HashMap<>();
        m.put("exempt.length", PermissionInformation.getWithTranslation("permission.warn.exempt.length", SuggestedLevel.MOD));
        m.put("exempt.target", PermissionInformation.getWithTranslation("permission.warn.exempt.target", SuggestedLevel.MOD));
        m.put("notify", PermissionInformation.getWithTranslation("permission.warn.notify", SuggestedLevel.MOD));
        return m;
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                NucleusParameters.ONE_USER,
                NucleusParameters.OPTIONAL_WEAK_DURATION,
                NucleusParameters.REASON
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        final User user = args.<User>getOne(NucleusParameters.Keys.USER).get();
        Optional<Long> optDuration = args.getOne(NucleusParameters.Keys.DURATION);
        String reason = args.<String>getOne(NucleusParameters.Keys.REASON).get();

        if (this.permissions.testSuffix(user, "exempt.target", src, false)) {
            throw ReturnMessageException.fromKey("command.warn.exempt", user.getName());
        }

        //Set default duration if no duration given
        if (this.warnConfig.getDefaultLength() != -1 && !optDuration.isPresent()) {
            optDuration = Optional.of(this.warnConfig.getDefaultLength());
        }

        UUID warner = Util.getUUID(src);
        WarnData warnData = optDuration.map(aLong -> new WarnData(Instant.now(), warner, reason, Duration.ofSeconds(aLong)))
                .orElseGet(() -> new WarnData(Instant.now(), warner, reason));

        //Check if too long (No duration provided, it is infinite)
        if (!optDuration.isPresent() && this.warnConfig.getMaximumWarnLength() != -1 && !this.permissions.testSuffix(src, "exempt.length")) {
            throw ReturnMessageException.fromKey("command.warn.length.toolong", Util.getTimeStringFromSeconds(this.warnConfig.getMaximumWarnLength()));
        }

        //Check if too long
        if (optDuration.orElse(Long.MAX_VALUE) > this.warnConfig.getMaximumWarnLength() && this.warnConfig
                .getMaximumWarnLength() != -1 && !this.permissions.testSuffix(src, "exempt.length")) {
            throw ReturnMessageException.fromKey("command.warn.length.toolong", Util.getTimeStringFromSeconds(this.warnConfig.getMaximumWarnLength()));
        }

        //Check if too short
        if (optDuration.orElse(Long.MAX_VALUE) < this.warnConfig.getMinimumWarnLength() && this.warnConfig
                .getMinimumWarnLength() != -1 && !this.permissions.testSuffix(src, "exempt.length")) {
            throw ReturnMessageException.fromKey("command.warn.length.tooshort", Util.getTimeStringFromSeconds(this.warnConfig.getMinimumWarnLength
                    ()));
        }

        if (this.handler.addWarning(user, warnData)) {
            MutableMessageChannel messageChannel = new PermissionMessageChannel(this.permissions.getPermissionWithSuffix("notify")).asMutable();
            messageChannel.addMember(src);

            if (optDuration.isPresent()) {
                String time = Util.getTimeStringFromSeconds(optDuration.get());
                messageChannel.send(Nucleus.getNucleus()
                        .getMessageProvider().getTextMessageWithFormat("command.warn.success.time", user.getName(), src.getName(), warnData.getReason(), time));

                if (user.isOnline()) {
                    user.getPlayer().get().sendMessage(
                            Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("warn.playernotify.time", warnData.getReason(), time));
                }
            } else {
                messageChannel.send(
                        Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.warn.success.norm", user.getName(), src.getName(), warnData.getReason()));

                if (user.isOnline()) {
                    user.getPlayer().get().sendMessage(
                            Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("warn.playernotify.standard", warnData.getReason()));
                }
            }

            //Check if the subject has action command should be executed
            if (this.warnConfig.getWarningsBeforeAction() != -1) {
                if (this.handler.getWarningsInternal(user, true, false).size() < this.warnConfig.getWarningsBeforeAction()) {
                    return CommandResult.success();
                }

                //Expire all active warnings
                // The cause is the plugin, as this isn't directly the warning user.
                CauseStackHelper.createFrameWithCausesWithConsumer(c -> this.handler.clearWarnings(user, false, false, c), src);

                //Get and run the action command
                String command = this.warnConfig.getActionCommand().replaceAll("\\{\\{name}}", user.getName());
                Sponge.getCommandManager().process(Sponge.getServer().getConsole(), command);
            }

            return CommandResult.success();
        }

        throw ReturnMessageException.fromKey("command.warn.fail", user.getName());
    }
}

/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.admin.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.util.CauseStackHelper;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.HashMap;
import java.util.Map;

@NoModifiers
@Permissions
@RegisterCommand("sudo")
@EssentialsEquivalent("sudo")
@NonnullByDefault
public class SudoCommand extends AbstractCommand<CommandSource> {

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[]{
                NucleusParameters.ONE_PLAYER,
                NucleusParameters.COMMAND
        };
    }

    @Override
    public Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> m = new HashMap<>();
        m.put("exempt.target", PermissionInformation.getWithTranslation("permission.sudo.exempt", SuggestedLevel.ADMIN));
        return m;
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Player pl = args.<Player>getOne(NucleusParameters.Keys.PLAYER).get();
        String cmd = args.<String>getOne(NucleusParameters.Keys.COMMAND).get();
        if (pl.equals(src) || this.permissions.testSuffix(pl, "exempt.target", src, false)) {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.sudo.noperms"));
            return CommandResult.empty();
        }

        if (cmd.startsWith("c:")) {
            if (cmd.equals("c:")) {
                src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.sudo.chatfail"));
                return CommandResult.empty();
            }

            Text rawMessage = Text.of(cmd.split(":", 2)[1]);
            try (CauseStackManager.StackFrame c = CauseStackHelper.createFrameWithCauses(
                    EventContext.builder()
                        .add(EventContextKeys.PLAYER_SIMULATED, pl.getProfile())
                        .build(), pl, src)) {
                if (pl.simulateChat(rawMessage, Sponge.getCauseStackManager().getCurrentCause()).isCancelled()) {
                    throw ReturnMessageException.fromKey("command.sudo.chatcancelled");
                }
            }

            return CommandResult.success();
        }

        src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.sudo.force", pl.getName(), cmd));
        Sponge.getCommandManager().process(pl, cmd);
        return CommandResult.success();
    }

}

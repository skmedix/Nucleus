/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.message.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.message.handlers.MessageHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.HashMap;
import java.util.Map;

@Permissions(suggestedLevel = SuggestedLevel.MOD)
@RunAsync
@NoModifiers
@RegisterCommand("socialspy")
@EssentialsEquivalent("socialspy")
@NonnullByDefault
public class SocialSpyCommand extends AbstractCommand<Player> {

    private final MessageHandler handler = getServiceUnchecked(MessageHandler.class);

    @Override protected Map<String, PermissionInformation> permissionSuffixesToRegister() {
        return new HashMap<String, PermissionInformation>() {{
            put("force", PermissionInformation.getWithTranslation("permission.socialspy.force", SuggestedLevel.NONE));
        }};
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                NucleusParameters.OPTIONAL_ONE_TRUE_FALSE
        };
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        if (this.handler.forcedSocialSpyState(src).asBoolean()) {
            throw new ReturnMessageException(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.socialspy.forced"));
        }

        boolean spy = args.<Boolean>getOne(NucleusParameters.Keys.BOOL).orElse(!this.handler.isSocialSpy(src));
        if (this.handler.setSocialSpy(src, spy)) {
            Text message = Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat(spy ? "command.socialspy.on" : "command.socialspy.off");
            src.sendMessage(message);
            return CommandResult.success();
        }

        src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.socialspy.unable"));
        return CommandResult.empty();
    }
}

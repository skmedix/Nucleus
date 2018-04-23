/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.commandspy.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.dataservices.modular.ModularUserService;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.messages.MessageProvider;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.commandspy.datamodules.CommandSpyUserDataModule;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Map;

@Permissions
@RegisterCommand("commandspy")
@NonnullByDefault
public class CommandSpyCommand extends AbstractCommand<Player> {

    private final String truefalse = "true/false";

    @Override protected Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> mspi = super.permissionSuffixesToRegister();
        mspi.put("exempt.target", PermissionInformation.getWithTranslation("permission.commandspy.exempt.target", SuggestedLevel.ADMIN));
        return mspi;
    }

    @Override public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.optional(GenericArguments.bool(Text.of(this.truefalse)))
        };
    }

    @Override public CommandResult executeCommand(Player src, CommandContext args) {
        ModularUserService service = Nucleus.getNucleus().getUserDataManager().getUnchecked(src);
        CommandSpyUserDataModule c = service.get(CommandSpyUserDataModule.class);
        boolean to = args.<Boolean>getOne(this.truefalse).orElseGet(() -> !c.isCommandSpy());
        c.setCommandSpy(to);

        MessageProvider mp = Nucleus.getNucleus().getMessageProvider();
        src.sendMessage(mp.getTextMessageWithFormat("command.commandspy.success", mp.getMessageWithFormat(to ? "standard.enabled" : "standard.disabled")));
        return CommandResult.success();
    }
}

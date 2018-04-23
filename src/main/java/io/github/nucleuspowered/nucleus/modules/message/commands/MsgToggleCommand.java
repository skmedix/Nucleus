/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.message.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.dataservices.modular.ModularUserService;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.message.datamodules.MessageUserDataModule;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Map;

@RunAsync
@NoModifiers
@Permissions
@RegisterCommand({"msgtoggle", "messagetoggle", "mtoggle"})
@NonnullByDefault
public class MsgToggleCommand extends AbstractCommand<Player> {

    private final String toggle = "true|false";

    @Override
    protected Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> mpi = super.permissionSuffixesToRegister();
        mpi.put("bypass", PermissionInformation.getWithTranslation("permission.msgtoggle.bypass", SuggestedLevel.ADMIN));
        return mpi;
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.optionalWeak(GenericArguments.bool(Text.of(this.toggle)))
        };
    }

    @Override
    protected CommandResult executeCommand(Player src, CommandContext args) {
        ModularUserService mus = Nucleus.getNucleus().getUserDataManager().getUnchecked(src);
        boolean flip = args.<Boolean>getOne(this.toggle).orElseGet(() -> !mus.get(MessageUserDataModule.class).isMsgToggle());

        mus.get(MessageUserDataModule.class).setMsgToggle(flip);
        src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.msgtoggle.success." + String.valueOf(flip)));

        return CommandResult.success();
    }

}

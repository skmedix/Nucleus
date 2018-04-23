/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.back.commands;

import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.internal.teleport.NucleusTeleportHandler;
import io.github.nucleuspowered.nucleus.modules.back.handlers.BackHandler;
import io.github.nucleuspowered.nucleus.modules.back.listeners.BackListeners;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.World;

import java.util.Map;
import java.util.Optional;

@Permissions
@RegisterCommand({"back", "return"})
@EssentialsEquivalent({"back", "return"})
@NonnullByDefault
public class BackCommand extends AbstractCommand<Player> {

    private final BackHandler handler = Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(BackHandler.class);

    private final String key = "force";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.optional(GenericArguments.literal(Text.of(this.key), "-f"))
        };
    }

    @Override
    protected Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> m = Maps.newHashMap();
        m.put(BackListeners.ON_DEATH, PermissionInformation.getWithTranslation("permission.back.ondeath", SuggestedLevel.USER));
        m.put(BackListeners.ON_TELEPORT, PermissionInformation.getWithTranslation("permission.back.onteleport", SuggestedLevel.USER));
        m.put(BackListeners.ON_PORTAL, PermissionInformation.getWithTranslation("permission.back.onportal", SuggestedLevel.USER));
        return m;
    }

    @Override
    @SuppressWarnings("deprecation")
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        Optional<Transform<World>> ol = this.handler.getLastLocation(src);
        if (!ol.isPresent()) {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.back.noloc"));
            return CommandResult.empty();
        }

        Transform<World> loc = ol.get();
        NucleusTeleportHandler.TeleportResult result = args.hasAny(this.key)
                ? Nucleus.getNucleus().getTeleportHandler().teleportPlayer(src, loc, NucleusTeleportHandler.StandardTeleportMode.NO_CHECK)
                : Nucleus.getNucleus().getTeleportHandler().teleportPlayer(src, loc);
        if (result.isSuccess()) {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.back.success"));
            return CommandResult.success();
        } else if (result == NucleusTeleportHandler.TeleportResult.FAILED_NO_LOCATION) {
            throw ReturnMessageException.fromKey("command.back.nosafe");
        }

        throw ReturnMessageException.fromKey("command.back.cancelled");
    }
}

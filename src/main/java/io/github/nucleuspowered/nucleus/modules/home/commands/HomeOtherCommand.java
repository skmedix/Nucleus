/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.home.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.nucleusdata.Home;
import io.github.nucleuspowered.nucleus.argumentparsers.HomeOtherArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.home.config.HomeConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.home.events.UseHomeEvent;
import io.github.nucleuspowered.nucleus.util.CauseStackHelper;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.HashMap;
import java.util.Map;

@NonnullByDefault
@Permissions(prefix = "home", mainOverride = "other", suggestedLevel = SuggestedLevel.MOD)
@RegisterCommand(value = "other", subcommandOf = HomeCommand.class, rootAliasRegister = "homeother")
public class HomeOtherCommand extends AbstractCommand<Player> implements Reloadable {

    private final String home = "home";
    public static final String OTHER_EXEMPT_PERM_SUFFIX = "exempt.target";
    private boolean isSafeTeleport = true;

    @Override public void onReload() {
        this.isSafeTeleport = Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(HomeConfigAdapter.class).getNodeOrDefault()
                .isSafeTeleport();
    }

    @Override protected Map<String, PermissionInformation> permissionSuffixesToRegister() {
        return new HashMap<String, PermissionInformation>() {{
            put("exempt.target", PermissionInformation.getWithTranslation("permission.home.other.exempt.target", SuggestedLevel.ADMIN));
        }};
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {GenericArguments.onlyOne(new HomeOtherArgument(Text.of(this.home), Nucleus.getNucleus()))};
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        // Get the home.
        Home wl = args.<Home>getOne(this.home).get();

        Sponge.getServer().loadWorld(wl.getWorldProperties()
                .orElseThrow(() -> ReturnMessageException.fromKey("command.home.invalid", wl.getName())));

        Location<World> targetLocation = wl.getLocation().orElseThrow(() -> ReturnMessageException.fromKey("command.home.invalid", wl.getName()));

        UseHomeEvent event = CauseStackHelper.createFrameWithCausesWithReturn(c -> new UseHomeEvent(c, src, wl), src);
        if (Sponge.getEventManager().post(event)) {
            throw new ReturnMessageException(event.getCancelMessage().orElseGet(() ->
                    Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("nucleus.eventcancelled")
            ));
        }

        // Warp to it safely.
        if (Nucleus.getNucleus().getTeleportHandler().teleportPlayer(src, targetLocation, wl.getRotation(), this.isSafeTeleport).isSuccess()) {
            src.sendMessage(
                    Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.homeother.success", wl.getUser().getName(), wl.getName()));
            return CommandResult.success();
        } else {
            throw ReturnMessageException.fromKey("command.homeother.fail", wl.getUser().getName(), wl.getName());
        }
    }
}

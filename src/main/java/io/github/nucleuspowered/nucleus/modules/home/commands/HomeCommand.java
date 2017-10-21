/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.home.commands;

import io.github.nucleuspowered.nucleus.api.nucleusdata.Home;
import io.github.nucleuspowered.nucleus.api.service.NucleusHomeService;
import io.github.nucleuspowered.nucleus.argumentparsers.HomeArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.home.config.HomeConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.home.events.UseHomeEvent;
import io.github.nucleuspowered.nucleus.modules.home.handlers.HomeHandler;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

import javax.inject.Inject;

@Permissions(suggestedLevel = SuggestedLevel.USER)
@RegisterCommand("home")
@EssentialsEquivalent(value = {"home", "homes"}, notes = "'/homes' will list homes, '/home' will teleport like Essentials did.")
@NonnullByDefault
public class HomeCommand extends AbstractCommand<Player> implements AbstractCommand.Reloadable {

    private final String home = "home";

    private final HomeHandler homeHandler;
    private final HomeConfigAdapter homeConfigAdapter;

    private boolean isSafeTeleport = true;
    private boolean isPreventOverhang = true;

    @Inject
    public HomeCommand(HomeHandler homeHandler, HomeConfigAdapter homeConfigAdapter) {
        this.homeHandler = homeHandler;
        this.homeConfigAdapter = homeConfigAdapter;
        try {
            onReload();
        } catch (Exception ignored) {
        }
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.onlyOne(GenericArguments.optional(new HomeArgument(Text.of(home), plugin)))
        };
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        int max = this.homeHandler.getMaximumHomes(src) ;
        int current = this.homeHandler.getHomeCount(src) ;
        if (this.isPreventOverhang && max < current) {
            // If the player has too many homes, tell them
            throw ReturnMessageException.fromKey("command.home.overhang", String.valueOf(max), String.valueOf(current));
        }

        // Get the home.
        Optional<Home> owl = args.getOne(home);
        Home wl;
        if (owl.isPresent()) {
            wl = owl.get();
        } else {
            wl = homeHandler.getHome(src, NucleusHomeService.DEFAULT_HOME_NAME)
                .orElseThrow(() -> new ReturnMessageException(plugin.getMessageProvider().getTextMessageWithFormat("args.home.nohome", "home")));
        }

        Sponge.getServer().loadWorld(wl.getWorldProperties()
                .orElseThrow(() -> ReturnMessageException.fromKey("command.home.invalid", wl.getName())));

        Location<World> targetLocation = wl.getLocation().orElseThrow(() -> ReturnMessageException.fromKey("command.home.invalid", wl.getName()));

        UseHomeEvent event = new UseHomeEvent(Cause.of(NamedCause.owner(src)), src, wl);
        if (Sponge.getEventManager().post(event)) {
            throw new ReturnMessageException(event.getCancelMessage().orElseGet(() ->
                plugin.getMessageProvider().getTextMessageWithFormat("nucleus.eventcancelled")
            ));
        }

        // Warp to it safely.
        if (plugin.getTeleportHandler().teleportPlayer(src, targetLocation, wl.getRotation(),this.isSafeTeleport).isSuccess()) {
            if (!wl.getName().equalsIgnoreCase(NucleusHomeService.DEFAULT_HOME_NAME)) {
                src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.home.success", wl.getName()));
            } else {
                src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.home.successdefault"));
            }

            return CommandResult.success();
        } else {
            throw ReturnMessageException.fromKey("command.home.fail", wl.getName());
        }
    }

    @Override
    public void onReload() {
        this.isSafeTeleport = this.homeConfigAdapter.getNodeOrDefault().isSafeTeleport();
        this.isPreventOverhang = this.homeConfigAdapter.getNodeOrDefault().isPreventHomeCountOverhang();
    }
}

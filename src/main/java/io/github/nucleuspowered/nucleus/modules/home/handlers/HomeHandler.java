/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.home.handlers;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.exceptions.NucleusException;
import io.github.nucleuspowered.nucleus.api.nucleusdata.Home;
import io.github.nucleuspowered.nucleus.api.service.NucleusHomeService;
import io.github.nucleuspowered.nucleus.dataservices.modular.ModularUserService;
import io.github.nucleuspowered.nucleus.modules.home.commands.SetHomeCommand;
import io.github.nucleuspowered.nucleus.modules.home.datamodules.HomeUserDataModule;
import io.github.nucleuspowered.nucleus.modules.home.events.AbstractHomeEvent;
import io.github.nucleuspowered.nucleus.modules.home.events.CreateHomeEvent;
import io.github.nucleuspowered.nucleus.modules.home.events.DeleteHomeEvent;
import io.github.nucleuspowered.nucleus.modules.home.events.ModifyHomeEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;

public class HomeHandler implements NucleusHomeService {

    private final NucleusPlugin plugin;
    private final String unlimitedPermission;

    @Inject
    public HomeHandler(NucleusPlugin plugin) {
        this.plugin = plugin;
        this.unlimitedPermission = plugin.getPermissionRegistry().getPermissionsForNucleusCommand(SetHomeCommand.class).getPermissionWithSuffix("unlimited");
    }

    @Override public List<Home> getHomes(UUID user) {
        Optional<ModularUserService> service = plugin.getUserDataManager().get(user); //.get().getHome;
        if (service.isPresent()) {
            return Lists.newArrayList(service.get().get(HomeUserDataModule.class).getHomes().values());
        }

        return Lists.newArrayList();
    }

    @Override public Optional<Home> getHome(UUID user, String name) {
        Optional<ModularUserService> service = plugin.getUserDataManager().get(user);
        if (service.isPresent()) {
            return service.get().get(HomeUserDataModule.class).getHome(name);
        }

        return Optional.empty();
    }

    @Override public void createHome(Cause cause, User user, String name, Location<World> location, Vector3d rotation) throws NucleusException {
        Preconditions.checkState(cause.root() instanceof PluginContainer, "The root must be a PluginContainer");
        createHomeInternal(cause, user, name, location, rotation);
    }

    public void createHomeInternal(Cause cause, User user, String name, Location<World> location, Vector3d rotation) throws NucleusException {
        if (!NucleusHomeService.HOME_NAME_PATTERN.matcher(name).matches()) {
            throw new NucleusException(
                plugin.getMessageProvider().getTextMessageWithFormat("command.sethome.name"),
                NucleusException.ExceptionType.DISALLOWED_NAME);
        }

        int max = getMaximumHomes(user);
        if (getHomes(user.getUniqueId()).size() >= max) {
            throw new NucleusException(
                plugin.getMessageProvider().getTextMessageWithFormat("command.sethome.limit", String.valueOf(max)),
                NucleusException.ExceptionType.LIMIT_REACHED);
        }

        CreateHomeEvent event = new CreateHomeEvent(name, user, cause, location);
        postEvent(event);

        // Just in case.
        if (!plugin.getUserDataManager().get(user).get().get(HomeUserDataModule.class).setHome(name, location, rotation, false)) {
            throw new NucleusException(
                plugin.getMessageProvider().getTextMessageWithFormat("command.sethome.seterror", name),
                NucleusException.ExceptionType.UNKNOWN_ERROR);
        }
    }

    @Override public void modifyHome(Cause cause, Home home, Location<World> location, Vector3d rotation) throws NucleusException {
        Preconditions.checkState(cause.root() instanceof PluginContainer, "The root must be a PluginContainer");
        modifyHomeInternal(cause, home, location, rotation);
    }

    public void modifyHomeInternal(Cause cause, Home home, Location<World> location, Vector3d rotation) throws NucleusException {
        ModifyHomeEvent event = new ModifyHomeEvent(cause, home, location);
        postEvent(event);

        // Just in case.
        if (!plugin.getUserDataManager().getUnchecked(home.getUser()).get(HomeUserDataModule.class).setHome(home.getName(), location, rotation, true)) {
            throw new NucleusException(
                plugin.getMessageProvider().getTextMessageWithFormat("command.sethome.seterror", home.getName()),
                NucleusException.ExceptionType.UNKNOWN_ERROR);
        }
    }

    @Override public void removeHome(Cause cause, Home home) throws NucleusException {
        Preconditions.checkState(cause.root() instanceof PluginContainer, "The root must be a PluginContainer");
        removeHomeInternal(cause, home);
    }

    public void removeHomeInternal(Cause cause, Home home) throws NucleusException {
        DeleteHomeEvent event = new DeleteHomeEvent(cause, home);
        postEvent(event);

        if (!plugin.getUserDataManager().get(home.getOwnersUniqueId()).get().get(HomeUserDataModule.class).deleteHome(home.getName())) {
            throw new NucleusException(plugin.getMessageProvider().getTextMessageWithFormat("command.home.delete.fail", home.getName()), NucleusException.ExceptionType.UNKNOWN_ERROR);
        }
    }

    @Override public int getMaximumHomes(UUID uuid) throws IllegalArgumentException {
        Optional<User> user = Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(uuid);
        if (!user.isPresent()) {
            throw new IllegalArgumentException("user does not exist.");
        }

        return getMaximumHomes(user.get());
    }

    @Override public int getMaximumHomes(User src) {
        if (src.hasPermission(this.unlimitedPermission)) {
            return Integer.MAX_VALUE;
        }

        return Math.max(Util.getPositiveIntOptionFromSubject(src, "home-count", "homes").orElse(1), 1);
    }

    private void postEvent(AbstractHomeEvent event) throws NucleusException {
        if (Sponge.getEventManager().post(event)) {
            throw new NucleusException(event.getCancelMessage().orElseGet(() ->
                plugin.getMessageProvider().getTextMessageWithFormat("nucleus.eventcancelled")), NucleusException.ExceptionType.EVENT_CANCELLED
            );
        }
    }
}

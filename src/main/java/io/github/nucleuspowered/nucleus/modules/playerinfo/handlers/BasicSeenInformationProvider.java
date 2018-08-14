/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.playerinfo.handlers;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.service.NucleusSeenService;
import io.github.nucleuspowered.nucleus.internal.traits.PermissionTrait;
import io.github.nucleuspowered.nucleus.modules.playerinfo.PlayerInfoModule;
import io.github.nucleuspowered.nucleus.modules.playerinfo.commands.SeenCommand;
import io.github.nucleuspowered.nucleus.modules.playerinfo.config.PlayerInfoConfigAdapter;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import java.util.Collection;
import java.util.function.BiFunction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BasicSeenInformationProvider implements NucleusSeenService.SeenInformationProvider, PermissionTrait {

    private final String permission;
    private final BiFunction<CommandSource, User, Collection<Text>> getterFunction;
    private static PlayerInfoConfigAdapter adapter = null;

    public BasicSeenInformationProvider(@Nullable String permission, BiFunction<CommandSource, User, Collection<Text>> getterFunction) {
        this.permission = permission;
        this.getterFunction = getterFunction;
    }

    @Override
    public boolean hasPermission(@Nonnull CommandSource source, @Nonnull User user) {
        try {
            return hasPermission(source, SeenCommand.EXTENDED_PERMISSION) ||
                (this.permission != null && !getAdapter().getNodeOrDefault().getSeen().isExtendedPermRequired()
                        && hasPermission(source, this.permission));
        } catch (Exception e) {
            e.printStackTrace();

            return false;
        }
    }

    @Nonnull
    @Override
    public Collection<Text> getInformation(@Nonnull CommandSource source, @Nonnull User user) {
        return this.getterFunction.apply(source, user);
    }

    private synchronized static PlayerInfoConfigAdapter getAdapter() throws Exception {
        if (adapter == null) {
            adapter = Nucleus.getNucleus().getModuleContainer().getConfigAdapterForModule(PlayerInfoModule.ID, PlayerInfoConfigAdapter.class);
        }

        return adapter;
    }
}

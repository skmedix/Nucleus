/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.argumentparsers;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.nucleusdata.Kit;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import io.github.nucleuspowered.nucleus.internal.traits.PermissionTrait;
import io.github.nucleuspowered.nucleus.modules.kit.KitModule;
import io.github.nucleuspowered.nucleus.modules.kit.commands.kit.KitCommand;
import io.github.nucleuspowered.nucleus.modules.kit.config.KitConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.kit.handlers.KitHandler;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public class KitArgument extends CommandElement implements PermissionTrait {

    private final KitConfigAdapter config;
    private final KitHandler kitHandler;
    private final boolean permissionCheck;
    private final String showhiddenperm = Nucleus.getNucleus().getPermissionRegistry().getPermissionsForNucleusCommand(KitCommand.class)
            .getPermissionWithSuffix("showhidden");

    public KitArgument(@Nullable Text key, boolean permissionCheck) {
        super(key);
        this.config = Nucleus.getNucleus().getConfigAdapter(KitModule.ID, KitConfigAdapter.class).get();
        this.kitHandler = Nucleus.getNucleus().getInternalServiceManager().getService(KitHandler.class).get();
        this.permissionCheck = permissionCheck;
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        String kitName = args.next();
        if (kitName.isEmpty()) {
            throw args.createError(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("args.kit.noname"));
        }

        Kit kit = this.kitHandler.getKit(kitName)
                .orElseThrow(() -> args.createError(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("args.kit.noexist")));

        if (!checkPermission(source, kit)) {
            throw args.createError(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("args.kit.noperms"));
        }

        return kit;
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        try {
            final boolean showhidden = hasPermission(src, this.showhiddenperm);
            String name = args.peek().toLowerCase();
            return this.kitHandler.getKitNames().stream()
                    .filter(s -> s.toLowerCase().startsWith(name))
                    .limit(20)
                    .map(x -> this.kitHandler.getKit(x).get())
                    .filter(x -> checkPermission(src, x))
                    .filter(x -> this.permissionCheck && (showhidden || !x.isHiddenFromList()))
                    .map(x -> x.getName().toLowerCase())
                    .collect(Collectors.toList());
        } catch (ArgumentParseException e) {
            return Lists.newArrayList();
        }
    }

    private boolean checkPermission(CommandSource src, Kit kit) {
        if (!this.permissionCheck) {
            return true;
        }

        // No permissions, no entry!
        return hasPermission(src, PermissionRegistry.PERMISSIONS_PREFIX + "kits." + kit.getName().toLowerCase());
    }

}

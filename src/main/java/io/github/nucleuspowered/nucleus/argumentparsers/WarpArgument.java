/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.argumentparsers;

import com.google.common.collect.ImmutableList;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.nucleusdata.Warp;
import io.github.nucleuspowered.nucleus.api.service.NucleusWarpService;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.internal.traits.InternalServiceManagerTrait;
import io.github.nucleuspowered.nucleus.internal.traits.PermissionTrait;
import io.github.nucleuspowered.nucleus.modules.warp.config.WarpConfigAdapter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

/**
 * Returns a {@link Warp}
 */
@NonnullByDefault
@SuppressWarnings("all")
public class WarpArgument extends CommandElement implements Reloadable, InternalServiceManagerTrait, PermissionTrait {

    private NucleusWarpService service;
    private final boolean permissionCheck;
    private boolean separate = true;

    public WarpArgument(@Nullable Text key, boolean permissionCheck) {
        super(key);
        this.permissionCheck = permissionCheck;
        if (this.permissionCheck) {
            Nucleus.getNucleus().registerReloadable(this);
        }
    }

    @Override public void onReload() throws Exception {
        this.separate = getServiceUnchecked(WarpConfigAdapter.class).getNodeOrDefault().isSeparatePermissions();
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        getService();

        String warpName = args.next();
        String warp = warpName.toLowerCase();
        if (!service.warpExists(warp)) {
            throw args.createError(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("args.warps.noexist"));
        }

        if (this.permissionCheck && this.separate && !checkPermission(source, warpName) && !checkPermission(source, warpName.toLowerCase())) {
            throw args.createError(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("args.warps.noperms"));
        }

        return service.getWarp(warpName).orElseThrow(() -> args.createError(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("args.warps.notavailable")));
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        getService();

        try {
            String el = args.peek();
            String name = el.toLowerCase();
            List<String> elements = this.service.getWarpNames().stream()
                    .filter(s -> s.startsWith(name))
                    .limit(21).collect(Collectors.toList());
            if (elements.size() >= 21) {
                src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("args.warps.maxselect", el));
                return ImmutableList.of(el);
            } else if (elements.isEmpty()) {
                return ImmutableList.of();
            } else if (!this.permissionCheck) { // permissioncheck and requires location were always the same
                return elements;
            }

            Predicate<String> predicate = s -> this.service.getWarp(s).get().getLocation().isPresent();

            if (this.separate) {
                predicate.and(x -> checkPermission(src, x));
            }

            return elements.stream().filter(predicate::test).collect(Collectors.toList());
        } catch (ArgumentParseException e) {
            return ImmutableList.of();
        }
    }

    private boolean checkPermission(CommandSource src, String name) {
        // No permissions, no entry!
        return hasPermission(src, PermissionRegistry.PERMISSIONS_PREFIX + "warps." + name.toLowerCase());
    }

    private void getService() {
        if (service == null) {
            service = Sponge.getServiceManager().provideUnchecked(NucleusWarpService.class);
        }
    }
}

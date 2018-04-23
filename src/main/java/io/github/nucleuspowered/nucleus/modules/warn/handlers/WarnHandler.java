/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.warn.handlers;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.nucleusdata.Warning;
import io.github.nucleuspowered.nucleus.api.service.NucleusWarningService;
import io.github.nucleuspowered.nucleus.dataservices.loaders.UserDataManager;
import io.github.nucleuspowered.nucleus.dataservices.modular.ModularUserService;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.modules.warn.WarnModule;
import io.github.nucleuspowered.nucleus.modules.warn.config.WarnConfig;
import io.github.nucleuspowered.nucleus.modules.warn.config.WarnConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.warn.data.WarnData;
import io.github.nucleuspowered.nucleus.modules.warn.datamodules.WarnUserDataModule;
import io.github.nucleuspowered.nucleus.modules.warn.events.WarnEvent;
import io.github.nucleuspowered.nucleus.util.CauseStackHelper;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public class WarnHandler implements NucleusWarningService, Reloadable {

    private final Nucleus nucleus = Nucleus.getNucleus();
    private final UserDataManager userDataManager = this.nucleus.getUserDataManager();
    private boolean expireWarnings = false;

    public List<WarnData> getWarningsInternal(User user) {
        return getWarningsInternal(user, true, true);
    }

    public List<WarnData> getWarningsInternal(User user, boolean includeActive, boolean includeExpired) {
        Optional<ModularUserService> userService = this.userDataManager.get(user);
        if (userService.isPresent()) {
            List<WarnData> warnings = userService.get().get(WarnUserDataModule.class).getWarnings();
            if (!includeActive) {
                warnings = warnings.stream().filter(WarnData::isExpired).collect(Collectors.toList());
            }
            if (!includeExpired) {
                warnings = warnings.stream().filter(warnData -> !warnData.isExpired()).collect(Collectors.toList());
            }
            return warnings;
        }
        return Lists.newArrayList();
    }

    public boolean addWarning(User user, WarnData warning) {
        Preconditions.checkNotNull(user);
        Preconditions.checkNotNull(warning);

        Optional<ModularUserService> optUserService = this.userDataManager.get(user);
        if (!optUserService.isPresent()) {
            return false;
        }

        WarnUserDataModule userService = optUserService.get().get(WarnUserDataModule.class);
        Optional<Duration> duration = warning.getTimeFromNextLogin();
        warning.nextLoginToTimestamp();
        userService.addWarning(warning);

        if (!warning.isExpired()) {
            Sponge.getEventManager().post(new WarnEvent.Warned(
                    CauseStackHelper.createCause(warning.getWarner().orElse(Util.consoleFakeUUID)),
                    user,
                    warning.getReason(),
                    duration.orElseGet(() -> warning.getRemainingTime().orElse(null))
            ));
        }

        return true;
    }

    @Override
    public void onReload() {
        this.expireWarnings = Nucleus.getNucleus().getConfigValue(WarnModule.ID, WarnConfigAdapter.class, WarnConfig::isExpireWarnings).orElse(false);
    }

    public boolean removeWarning(User user, WarnData warning) {
        return removeWarning(user, warning, false, CauseStackHelper.createCause(NucleusPlugin.getNucleus()));
    }

    public boolean removeWarning(User user, Warning warning, boolean permanent, Cause of) {
        Optional<ModularUserService> userService = this.userDataManager.get(user);
        if (userService.isPresent()) {
            userService.get().get(WarnUserDataModule.class).removeWarning(warning);
            if (this.expireWarnings && !warning.isExpired() && !permanent) {
                userService.get().get(WarnUserDataModule.class).addWarning(new WarnData(warning.getDate(), warning.getWarner()
                        .orElse(Util.consoleFakeUUID), warning.getReason(), true));
            }

            if (!warning.isExpired()) {
                Sponge.getEventManager().post(new WarnEvent.Expire(
                        CauseStackHelper.createCause(Util.getObjectFromUUID(warning.getWarner().orElse(Util.consoleFakeUUID))),
                        user,
                        warning.getReason(),
                        warning.getWarner().orElse(null)
                ));
            }

            return true;
        }

        return false;
    }

    public boolean clearWarnings(User user, boolean clearActive, boolean clearExpired, Cause of) {
        Optional<ModularUserService> userService = this.userDataManager.get(user);
        if (userService.isPresent()) {
            List<WarnData> warnings = userService.get().get(WarnUserDataModule.class).getWarnings();

            if (!warnings.isEmpty()) {
                if (!clearActive && !clearExpired) {
                    warnings.stream().filter(warnData -> !warnData.isExpired()).forEach(warnData -> removeWarning(user, warnData));
                    return true;
                }

                if (clearActive) {
                    warnings.stream().filter(warnData -> !warnData.isExpired()).forEach(warnData ->
                        removeWarning(user, warnData, true, of));
                }

                if (clearExpired) {
                    warnings.stream().filter(WarnData::isExpired).forEach(warnData -> removeWarning(user, warnData, true, of));
                }

                return true;
            } else {
                return false;
            }
        }

        return false;
    }

    public boolean updateWarnings(User user) {
        Optional<ModularUserService> userService = this.userDataManager.get(user);
        if (!userService.isPresent()) {
            return false;
        }

        for (WarnData warning : getWarningsInternal(user)) {
            warning.nextLoginToTimestamp();

            if (warning.getEndTimestamp().isPresent() && warning.getEndTimestamp().get().isBefore(Instant.now())) {
                removeWarning(user, warning);
            }
        }
        return true;
    }

    @Override public boolean addWarning(User toWarn, CommandSource warner, String reason, @Nullable Duration duration) {
        return addWarning(toWarn, new WarnData(Instant.now(), Util.getUUID(warner), reason, duration));
    }

    @Override public List<Warning> getWarnings(User user) {
        return ImmutableList.copyOf(getWarningsInternal(user));
    }

    @Override public boolean expireWarning(User user, Warning warning, Cause cause) {
        return removeWarning(user, warning, false, cause);
    }
}

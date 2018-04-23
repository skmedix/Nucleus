/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.nameban.handlers;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.exceptions.NucleusException;
import io.github.nucleuspowered.nucleus.api.service.NucleusNameBanService;
import io.github.nucleuspowered.nucleus.modules.nameban.events.NameBanEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Optional;

public class NameBanHandler implements NucleusNameBanService {

    private final Nucleus plugin = Nucleus.getNucleus();

    @Override public boolean addName(String name, String reason, Cause cause) throws NucleusException {
        if (Util.usernameRegex.matcher(name).matches()) {
            if (Nucleus.getNucleus().getNameBanService().setBan(name, reason)) {
                Sponge.getEventManager().post(new NameBanEvent.Banned(name, reason, cause));
                Sponge.getServer().getOnlinePlayers().stream().filter(x -> x.getName().equalsIgnoreCase(name)).findFirst()
                        .ifPresent(x -> x.kick(TextSerializers.FORMATTING_CODE.deserialize(reason)));
                return true;
            }

            return false;
        }

        throw new NucleusException(Text.of("That is not a valid username."), NucleusException.ExceptionType.DISALLOWED_NAME);
    }

    @Override public Optional<String> getReasonForBan(String name) {
        return Nucleus.getNucleus().getNameBanService().getBanReason(name.toLowerCase());
    }

    @Override public boolean removeName(String name, Cause cause) throws NucleusException {
        if (Util.usernameRegex.matcher(name).matches()) {
            Optional<String> reason = getReasonForBan(name);
            if (reason.isPresent() && Nucleus.getNucleus().getNameBanService().removeBan(name)) {
                Sponge.getEventManager().post(new NameBanEvent.Unbanned(name, reason.get(), cause));
                return true;
            }

            return false;
        }

        throw new NucleusException(Text.of("That is not a valid username."), NucleusException.ExceptionType.DISALLOWED_NAME);
    }
}

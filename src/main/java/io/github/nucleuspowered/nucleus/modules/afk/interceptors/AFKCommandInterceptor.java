/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.afk.interceptors;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.text.NucleusTextTemplate;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NotifyIfAFK;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ICommandInterceptor;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.modules.afk.config.AFKConfig;
import io.github.nucleuspowered.nucleus.modules.afk.config.AFKConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.afk.events.AFKEvents;
import io.github.nucleuspowered.nucleus.modules.afk.handlers.AFKHandler;
import io.github.nucleuspowered.nucleus.util.CauseStackHelper;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.text.Text;

import java.util.Objects;

import javax.annotation.Nullable;

public class AFKCommandInterceptor implements ICommandInterceptor, Reloadable {

    private final AFKHandler handler = Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(AFKHandler.class);
    @Nullable private NucleusTextTemplate message = null;
    private boolean send = false;

    @Override
    public void onPreCommand(Class<? extends AbstractCommand<?>> commandClass, CommandSource source, CommandContext context) { }

    @Override
    public void onPostCommand(Class<? extends AbstractCommand<?>> commandClass, CommandSource source, CommandContext context, CommandResult result) {
        if (this.send && result.getSuccessCount().orElse(0) > 0 && commandClass.isAnnotationPresent(NotifyIfAFK.class)) {
            NotifyIfAFK annotation = commandClass.getAnnotation(NotifyIfAFK.class);
            Cause cause = CauseStackHelper.createCause(source);
            for (String key : annotation.value()) {
                context.getAll(key).stream()
                        .filter(x -> x instanceof User)
                        .map(x -> ((User) x).getPlayer().orElse(null))
                        .filter(Objects::nonNull)
                        .filter(this.handler::isAFK)
                        .forEach(x -> {
                            Text messageToSend = this.message == null ? null : this.message.getForCommandSource(x);
                            AFKEvents.Notify event = new AFKEvents.Notify(x, messageToSend, cause);
                            Sponge.getEventManager().post(event);
                            event.getMessage().ifPresent(source::sendMessage);
                        });
            }
        }
    }

    @Override
    public void onReload() {
        AFKConfig config = Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(AFKConfigAdapter.class).getNodeOrDefault();
        if (config.isAlertSenderOnAfk()) {
            NucleusTextTemplate textTemplate = config.getMessages().getOnCommand();
            if (textTemplate == null || textTemplate.isEmpty()) { // NPE has occurred here in the past due to an empty message.
                this.message = null;
            } else {
                this.message = textTemplate;
            }

            this.send = true;
        } else {
            this.message = null;
            this.send = false;
        }
    }
}

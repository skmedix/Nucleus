/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.mail.listeners;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.modules.mail.handlers.MailHandler;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.util.concurrent.TimeUnit;

public class MailListener implements ListenerBase {

    private MailHandler handler = getServiceUnchecked(MailHandler.class);

    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event) {
        Sponge.getScheduler().createAsyncExecutor(Nucleus.getNucleus()).schedule(() -> {
            int mailCount = this.handler.getMailInternal(event.getTargetEntity()).size();
            if (mailCount > 0) {
                event.getTargetEntity().sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("mail.login", String.valueOf(mailCount)));
                event.getTargetEntity().sendMessage(Text.builder()
                        .append(Text.builder("/mail").color(TextColors.AQUA).style(TextStyles.UNDERLINE).onClick(TextActions.runCommand("/mail"))
                                .onHover(TextActions.showText(Text.of("Click here to read your mail."))).build())
                        .append(Text.builder().append(Text.of(TextColors.YELLOW, " ")).append(
                                Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("mail.toread"))
                                .append(Text.of(" ")).build())
                        .append(Text.builder("/mail clear").color(TextColors.AQUA).style(TextStyles.UNDERLINE)
                                .onClick(TextActions.runCommand("/mail clear"))
                                .onHover(TextActions.showText(Text.of("Click here to delete your mail."))).build())
                        .append(Text.builder().append(Text.of(TextColors.YELLOW, " ")).append(
                                Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("mail.toclear")).build())
                        .build());
            }
        } , 1, TimeUnit.SECONDS);
    }
}

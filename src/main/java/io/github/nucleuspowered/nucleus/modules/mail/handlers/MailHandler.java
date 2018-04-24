/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.mail.handlers;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.nucleusdata.MailMessage;
import io.github.nucleuspowered.nucleus.api.service.NucleusMailService;
import io.github.nucleuspowered.nucleus.modules.mail.data.MailData;
import io.github.nucleuspowered.nucleus.modules.mail.datamodules.MailUserDataModule;
import io.github.nucleuspowered.nucleus.modules.mail.events.InternalNucleusMailEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MailHandler implements NucleusMailService {

    @Override
    public final List<MailMessage> getMail(User player, MailFilter... filters) {
        return Lists.newArrayList(getMailInternal(player, filters));
    }

    public final List<MailData> getMailInternal(User player, MailFilter... filters) {
        MailUserDataModule iqsu = Nucleus.getNucleus().getUserDataManager().getUnchecked(player).get(MailUserDataModule.class);

        List<MailData> lmd = iqsu.getMail();
        if (filters.length == 0 || lmd.isEmpty()) {
            return Lists.newArrayList(lmd);
        }

        Optional<Predicate<MailMessage>> lmf = Arrays.stream(filters).map(x -> (Predicate<MailMessage>)x).reduce(Predicate::and);
        return lmf.map(pred -> lmd.stream().filter(pred::test).collect(Collectors.toList())).orElse(lmd);
    }

    @Override
    public boolean removeMail(User player, MailMessage mailData) {
        try {
            return Nucleus.getNucleus().getUserDataManager().getUnchecked(player).get(MailUserDataModule.class).removeMail(mailData);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void sendMail(User playerFrom, User playerTo, String message) {
        MailUserDataModule iqsu;
        try {
            iqsu = Nucleus.getNucleus().getUserDataManager().getUnchecked(playerTo).get(MailUserDataModule.class);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // Message is about to be sent. Send the event out. If canceled, then
        // that's that.
        if (Sponge.getEventManager().post(new InternalNucleusMailEvent(playerFrom, playerTo, message))) {
            playerFrom.getPlayer().ifPresent(x -> x.sendMessage(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("message.cancel")));
            return;
        }

        MailData md = new MailData(playerFrom == null ? Util.consoleFakeUUID : playerFrom.getUniqueId(), Instant.now(), message);
        iqsu.addMail(md);

        Text from = playerFrom == null ? Text.of(Sponge.getServer().getConsole().getName()) : Nucleus.getNucleus().getNameUtil().getName(playerFrom);
        playerTo.getPlayer().ifPresent(x ->
                x.sendMessage(Text.builder().append(NucleusPlugin.getNucleus().getMessageProvider()
                        .getTextMessageWithFormat("mail.youvegotmail")).append(Text.of(" ", from)).build()));
    }

    @Override
    public void sendMailFromConsole(User playerTo, String message) {
        sendMail(null, playerTo, message);
    }

    @Override
    public boolean clearUserMail(User player) {
        MailUserDataModule iqsu;
        try {
            iqsu = Nucleus.getNucleus().getUserDataManager().get(player).get().get(MailUserDataModule.class);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return iqsu.clearMail();
    }
}

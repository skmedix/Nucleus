/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.message;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.service.NucleusPrivateMessagingService;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterService;
import io.github.nucleuspowered.nucleus.internal.messages.MessageProvider;
import io.github.nucleuspowered.nucleus.internal.qsml.module.ConfigurableModule;
import io.github.nucleuspowered.nucleus.modules.message.commands.SocialSpyCommand;
import io.github.nucleuspowered.nucleus.modules.message.config.MessageConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.message.datamodules.MessageUserDataModule;
import io.github.nucleuspowered.nucleus.modules.message.handlers.MessageHandler;
import org.spongepowered.api.text.Text;
import uk.co.drnaylor.quickstart.annotations.ModuleData;

import java.util.List;

@RegisterService(value = MessageHandler.class, apiService = NucleusPrivateMessagingService.class)
@ModuleData(id = MessageModule.ID, name = "Message")
public class MessageModule extends ConfigurableModule<MessageConfigAdapter> {

    public static final String ID = "message";

    @Override
    public MessageConfigAdapter createAdapter() {
        return new MessageConfigAdapter();
    }

    @Override public void performEnableTasks() {
        createSeenModule(SocialSpyCommand.class, (cs, user) -> {
            MessageHandler handler = Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(MessageHandler.class);
            boolean socialSpy = handler.isSocialSpy(user);
            boolean msgToggle = Nucleus.getNucleus().getUserDataManager().get(user)
                    .map(y -> y.get(MessageUserDataModule.class).isMsgToggle()).orElse(true);
            MessageProvider mp = Nucleus.getNucleus().getMessageProvider();
            List<Text> lt = Lists.newArrayList(
                mp.getTextMessageWithFormat("seen.socialspy",
                    mp.getMessageWithFormat("standard.yesno." + Boolean.toString(socialSpy).toLowerCase())));

            getConfigAdapter().ifPresent(x -> lt.add(
                mp.getTextMessageWithFormat("seen.socialspylevel", String.valueOf(Util.getPositiveIntOptionFromSubject(user, MessageHandler.socialSpyOption).orElse(0)))
            ));

            lt.add(mp.getTextMessageWithFormat("seen.msgtoggle",
                    mp.getMessageWithFormat("standard.yesno." + Boolean.toString(msgToggle).toLowerCase())));

            return lt;
        });
    }
}

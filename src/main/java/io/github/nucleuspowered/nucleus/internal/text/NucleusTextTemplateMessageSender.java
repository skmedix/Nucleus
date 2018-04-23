/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.text;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.text.NucleusTextTemplate;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class NucleusTextTemplateMessageSender {

    private final NucleusTextTemplate textTemplate;
    private final Function<Text, Text> postProcess;
    private final CommandSource sender;

    public NucleusTextTemplateMessageSender(NucleusTextTemplate textTemplate, CommandSource sender) {
        this(textTemplate, sender, t -> t);
    }

    public NucleusTextTemplateMessageSender(NucleusTextTemplate textTemplate, CommandSource sender, Function<Text, Text> postProcess) {
        this.textTemplate = textTemplate;
        this.postProcess = postProcess;
        this.sender = sender;
    }

    public void send() {
        List<CommandSource> members = Lists.newArrayList(Sponge.getServer().getConsole());
        members.addAll(Sponge.getServer().getOnlinePlayers());
        send(members);
    }

    public void send(Collection<CommandSource> source) {
        if (!this.textTemplate.containsTokens()) {
            Text text = this.postProcess.apply(this.textTemplate.getForCommandSource(Sponge.getServer().getConsole()));
            source.forEach(x -> x.sendMessage(text));
        } else {
            Map<String, Function<CommandSource, Optional<Text>>> m = Maps.newHashMap();
            m.put("sender", cs -> Nucleus.getNucleus().getMessageTokenService().applyPrimaryToken("displayname", this.sender));

            source.forEach(x -> x.sendMessage(this.postProcess.apply(this.textTemplate.getForCommandSource(x, m, null))));
        }
    }
}

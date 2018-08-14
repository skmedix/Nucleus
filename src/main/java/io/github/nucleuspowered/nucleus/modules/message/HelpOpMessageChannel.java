/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.message;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.chat.NucleusChatChannel;
import io.github.nucleuspowered.nucleus.internal.traits.PermissionTrait;
import io.github.nucleuspowered.nucleus.modules.message.commands.HelpOpCommand;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.channel.MessageReceiver;

import java.util.Collection;
import java.util.List;

public class HelpOpMessageChannel implements NucleusChatChannel.HelpOp, PermissionTrait {

    private static String PERMISSION = Nucleus.getNucleus().getPermissionRegistry()
            .getPermissionsForNucleusCommand(HelpOpCommand.class)
            .getPermissionWithSuffix("receive");

    public static HelpOpMessageChannel INSTANCE = new HelpOpMessageChannel();

    private HelpOpMessageChannel() { }

    @Override
    public Collection<MessageReceiver> getMembers() {
        List<MessageReceiver> members = Lists.newArrayList(Sponge.getServer().getConsole());
        Sponge.getServer().getOnlinePlayers().stream().filter(x -> hasPermission(x, PERMISSION)).forEach(members::add);
        return members;
    }
}

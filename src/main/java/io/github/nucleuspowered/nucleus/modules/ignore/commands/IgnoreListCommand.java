/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.ignore.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.internal.traits.MessageProviderTrait;
import io.github.nucleuspowered.nucleus.modules.ignore.datamodules.IgnoreUserDataModule;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.List;
import java.util.stream.Collectors;

@NoModifiers
@RunAsync
@Permissions(suggestedLevel = SuggestedLevel.USER, supportsOthers = true)
@RegisterCommand({"ignorelist", "listignore", "ignored"})
@NonnullByDefault
public class IgnoreListCommand extends AbstractCommand.SimpleTargetOtherUser implements MessageProviderTrait {

    @Override protected CommandResult executeWithPlayer(CommandSource source, User target, CommandContext args, boolean isSelf) {
        List<Text> ignoredList = Nucleus.getNucleus()
                .getUserDataManager()
                .getUnchecked(target)
                .get(IgnoreUserDataModule.class)
                .getIgnoreList()
                .stream()
                .map(x -> Nucleus.getNucleus().getNameUtil().getName(x).orElseGet(() ->
                        getMessage("command.ignorelist.unknown", x.toString())
                )).collect(Collectors.toList());

        if (ignoredList.isEmpty()) {
            if (isSelf) {
                source.sendMessage(getMessage("command.ignorelist.noignores.self"));
            } else {
                source.sendMessage(getMessage("command.ignorelist.noignores.other", target));
            }
        } else {
            Util.getPaginationBuilder(source)
                    .contents(ignoredList)
                    .title(getMessage("command.ignorelist.header", target))
                    .sendTo(source);
        }
        return CommandResult.success();
    }

}

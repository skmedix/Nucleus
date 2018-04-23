/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.info.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.TextFileController;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.info.InfoModule;
import io.github.nucleuspowered.nucleus.modules.info.config.InfoConfig;
import io.github.nucleuspowered.nucleus.modules.info.config.InfoConfigAdapter;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Optional;

@Permissions(suggestedLevel = SuggestedLevel.USER)
@RunAsync
@NoModifiers
@NonnullByDefault
@RegisterCommand("motd")
@EssentialsEquivalent("motd")
public class MotdCommand extends AbstractCommand<CommandSource> implements Reloadable {

    private Text title = Text.EMPTY;
    private boolean usePagination = true;

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) {
        Optional<TextFileController> otfc = Nucleus.getNucleus().getTextFileController(InfoModule.MOTD_KEY);
        if (!otfc.isPresent()) {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.motd.nocontroller"));
            return CommandResult.empty();
        }

        if (this.usePagination) {
            otfc.get().sendToPlayer(src, this.title);
        } else {
            otfc.get().getTextFromNucleusTextTemplates(src).forEach(src::sendMessage);
        }

        return CommandResult.success();
    }

    @Override public void onReload() {
        InfoConfig config = Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(InfoConfigAdapter.class).getNodeOrDefault();
        String title = config.getMotdTitle();
        if (title.isEmpty()) {
            this.title = Text.EMPTY;
        } else {
            this.title = TextSerializers.FORMATTING_CODE.deserialize(title);
        }

        this.usePagination = config.isMotdUsePagination();
    }
}

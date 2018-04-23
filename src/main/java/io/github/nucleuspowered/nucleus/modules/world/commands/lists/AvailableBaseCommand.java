/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.world.commands.lists;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.messages.MessageProvider;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.List;
import java.util.stream.Collectors;

@NonnullByDefault
public abstract class AvailableBaseCommand extends AbstractCommand<CommandSource> {

    private final Class<? extends CatalogType> catalogType;
    private final String titleKey;

    AvailableBaseCommand(Class<? extends CatalogType> catalogType, String titleKey) {
        this.catalogType = catalogType;
        this.titleKey = titleKey;
    }

    @Override
    protected CommandResult executeCommand(CommandSource src, CommandContext args) {
        MessageProvider mp = Nucleus.getNucleus().getMessageProvider();

        List<Text> types = Sponge.getRegistry().getAllOf(this.catalogType).stream()
                .map(x -> mp.getTextMessageWithFormat("command.world.presets.item", x.getId(), x.getName()))
                .collect(Collectors.toList());

        Util.getPaginationBuilder(src).title(mp.getTextMessageWithTextFormat(this.titleKey))
                .contents(types).sendTo(src);

        return CommandResult.success();
    }
}

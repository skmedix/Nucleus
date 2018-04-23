/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.jail.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.nucleusdata.NamedLocation;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.jail.handlers.JailHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

@NoModifiers
@NonnullByDefault
@RunAsync
@RegisterCommand(value = "jails")
@Permissions(prefix = "jail", mainOverride = "list", suggestedLevel = SuggestedLevel.MOD)
@EssentialsEquivalent("jails")
public class JailsCommand extends AbstractCommand<CommandSource> {

    private final JailHandler handler = Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(JailHandler.class);

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) {
        Map<String, NamedLocation> mjs = this.handler.getJails();
        if (mjs.isEmpty()) {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.jails.nojails"));
            return CommandResult.empty();
        }

        List<Text> lt = mjs.entrySet().stream()
                .map(x -> createJail(x.getValue(), x.getKey()))
                .collect(Collectors.toList());

        Util.getPaginationBuilder(src)
            .title(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.jails.list.header")).padding(Text.of(TextColors.GREEN, "-"))
            .contents(lt).sendTo(src);
        return CommandResult.success();
    }

    private Text createJail(@Nullable NamedLocation data, String name) {
        if (data == null || !data.getLocation().isPresent()) {
            return Text.builder(name).color(TextColors.RED).onHover(TextActions.showText(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat
                    ("command.jails.unavailable"))).build();
        }

        Location<World> world = data.getLocation().get();
        Text.Builder inner = Text.builder(name).color(TextColors.GREEN).style(TextStyles.ITALIC)
                .onClick(TextActions.runCommand("/jails tp " + name))
                .onHover(TextActions.showText(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.jails.warpprompt", name)));

        return Text.builder().append(inner.build())
                .append(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.warps.warploc",
                        world.getExtent().getName(), world.getBlockPosition().toString()
                )).build();
    }
}

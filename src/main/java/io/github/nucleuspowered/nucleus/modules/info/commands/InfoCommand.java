/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.info.commands;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.argumentparsers.InfoArgument;
import io.github.nucleuspowered.nucleus.internal.TextFileController;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.info.config.InfoConfig;
import io.github.nucleuspowered.nucleus.modules.info.config.InfoConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.info.handlers.InfoHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Permissions(suggestedLevel = SuggestedLevel.USER)
@RunAsync
@NoModifiers
@NonnullByDefault
@RegisterCommand({"info", "einfo"})
@EssentialsEquivalent({"info", "ifo", "news", "about", "inform"})
public class InfoCommand extends AbstractCommand<CommandSource> implements Reloadable {

    private final InfoHandler infoHandler = Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(InfoHandler.class);
    private InfoConfig infoConfig = new InfoConfig();

    private final String key = "section";

    @Override public void onReload() {
        this.infoConfig = Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(InfoConfigAdapter.class).getNodeOrDefault();
    }

    @Override protected Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> map = new HashMap<>();
        map.put("list", PermissionInformation.getWithTranslation("permission.info.list", SuggestedLevel.ADMIN));
        return map;
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.flags().permissionFlag(this.permissions.getPermissionWithSuffix("list"), "l", "-list").buildWith(
                GenericArguments.optional(new InfoArgument(Text.of(this.key), this.infoHandler)))
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Optional<InfoArgument.Result> oir = args.getOne(this.key);
        if (this.infoConfig.isUseDefaultFile() && !oir.isPresent() && !args.hasAny("l")) {
            // Do we have a default?
            String def = this.infoConfig.getDefaultInfoSection();
            Optional<TextFileController> list = this.infoHandler.getSection(def);
            if (list.isPresent()) {
                oir = Optional.of(new InfoArgument.Result(this.infoHandler.getInfoSections().stream().filter(def::equalsIgnoreCase).findFirst().get(), list.get()));
            }
        }

        if (oir.isPresent()) {
            TextFileController controller = oir.get().text;
            Text def = TextSerializers.FORMATTING_CODE.deserialize(oir.get().name);
            Text title = Nucleus.getNucleus().getMessageProvider().getTextMessageWithTextFormat("command.info.title.section",
                    controller.getTitle(src).orElseGet(() -> Text.of(def)));

            controller.sendToPlayer(src, title);
            return CommandResult.success();
        }

        // Create a list of pages to load.
        Set<String> sections = this.infoHandler.getInfoSections();
        if (sections.isEmpty()) {
            throw new ReturnMessageException(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.info.none"));
        }

        // Create the text.
        List<Text> s = Lists.newArrayList();
        sections.forEach(x -> {
            Text.Builder tb = Text.builder().append(Text.builder(x)
                    .color(TextColors.GREEN).style(TextStyles.ITALIC)
                    .onHover(TextActions.showText(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.info.hover", x)))
                    .onClick(TextActions.runCommand("/info " + x)).build());

            // If there is a title, then add it.
            this.infoHandler.getSection(x).get().getTitle(src).ifPresent(sub ->
                tb.append(Text.of(TextColors.GOLD, " - ")).append(sub)
            );

            s.add(tb.build());
        });

        Util.getPaginationBuilder(src).contents()
                .header(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.info.header.default"))
                .title(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.info.title.default"))
                .contents(s.stream().sorted(Comparator.comparing(Text::toPlain)).collect(Collectors.toList()))
                .padding(Text.of(TextColors.GOLD, "-")).sendTo(src);
        return CommandResult.success();
    }
}

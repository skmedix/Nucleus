/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.commands.kit;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.nucleusdata.Kit;
import io.github.nucleuspowered.nucleus.dataservices.loaders.UserDataManager;
import io.github.nucleuspowered.nucleus.internal.CommandPermissionHandler;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.StandardAbstractCommand;
import io.github.nucleuspowered.nucleus.internal.permissions.SubjectPermissionCache;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.kit.config.KitConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.kit.datamodules.KitUserDataModule;
import io.github.nucleuspowered.nucleus.modules.kit.handlers.KitHandler;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.service.pagination.PaginationService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Set;

import javax.annotation.Nullable;
import javax.inject.Inject;

@Permissions(prefix = "kit", suggestedLevel = SuggestedLevel.ADMIN)
@RegisterCommand(value = {"list", "ls"}, subcommandOf = KitCommand.class, rootAliasRegister = "kits")
@RunAsync
@NoModifiers
@NonnullByDefault
public class KitListCommand extends StandardAbstractCommand<CommandSource> {

    private final KitHandler kitConfig;
    private final KitConfigAdapter kca;
    private final UserDataManager userConfigLoader;
    private final CommandPermissionHandler kitPermissionHandler = Nucleus.getNucleus().getPermissionRegistry()
            .getPermissionsForNucleusCommand(KitCommand.class);

    @Inject
    public KitListCommand(KitHandler kitConfig, KitConfigAdapter kca, UserDataManager userConfigLoader) {
        this.kitConfig = kitConfig;
        this.kca = kca;
        this.userConfigLoader = userConfigLoader;
    }

    @Override
    public CommandResult executeCommand(final SubjectPermissionCache<CommandSource> spc, CommandContext args) throws Exception {
        final CommandSource src = spc.getSubject();
        Set<String> kits = kitConfig.getKitNames();
        if (kits.isEmpty()) {
            src.sendMessage(plugin.getMessageProvider().getTextMessageWithFormat("command.kit.list.empty"));
            return CommandResult.empty();
        }

        PaginationService paginationService = Sponge.getServiceManager().provideUnchecked(PaginationService.class);
        ArrayList<Text> kitText = Lists.newArrayList();

        final KitUserDataModule user =
                src instanceof Player ? userConfigLoader.getUnchecked(((Player)src).getUniqueId()).get(KitUserDataModule.class) : null;

        // Only show kits that the user has permission for, if needed. This is the permission "plugin.kits.<kit>".
        final boolean showHidden = kitPermissionHandler.testSuffix(src, "showhidden");
        kitConfig.getKitNames(showHidden, src).stream()
            .filter(kit -> !kca.getNodeOrDefault().isSeparatePermissions() ||
                    src.hasPermission(KitHandler.getPermissionForKit(kit.toLowerCase())))
            .forEach(kit -> kitText.add(createKit(spc, user, kit, kitConfig.getKit(kit).get())));

        PaginationList.Builder paginationBuilder = paginationService.builder().contents(kitText)
                .title(plugin.getMessageProvider().getTextMessageWithFormat("command.kit.list.kits")).padding(Text.of(TextColors.GREEN, "-"));
        paginationBuilder.sendTo(src);

        return CommandResult.success();
    }

    private Text createKit(SubjectPermissionCache<CommandSource> source, @Nullable KitUserDataModule user, String kitName, Kit kitObj) {
        Text.Builder tb = Text.builder(kitName);

        if (user != null && Util.getKeyIgnoreCase(user.getKitLastUsedTime(), kitName).isPresent()) {
            // If one time used...
            if (kitObj.isOneTime() && !kitPermissionHandler.testSuffix(source, "exempt.onetime")) {
                return tb.color(TextColors.RED)
                        .onHover(TextActions.showText(plugin.getMessageProvider().getTextMessageWithFormat("command.kit.list.onetime", kitName)))
                        .style(TextStyles.STRIKETHROUGH).build();
            }

            // If an intervalOld is used...
            Duration interval = kitObj.getCooldown().orElse(Duration.ZERO);
            if (!interval.isZero() && !kitPermissionHandler.testCooldownExempt(source)) {

                // Get the next time the kit can be used.
                Instant next = Util.getValueIgnoreCase(user.getKitLastUsedTime(), kitName).get().plus(interval);
                if (next.isAfter(Instant.now())) {
                    // Get the time to next usage.
                    String time = Util.getTimeToNow(next);
                    return tb.color(TextColors.RED)
                            .onHover(TextActions.showText(plugin.getMessageProvider().getTextMessageWithFormat("command.kit.list.interval", kitName, time)))
                            .style(TextStyles.STRIKETHROUGH).build();
                }
            }
        }

        // Can use.
        Text.Builder builder = tb.color(TextColors.AQUA).onClick(TextActions.runCommand("/kit \"" + kitName + "\""))
                .onHover(TextActions.showText(plugin.getMessageProvider().getTextMessageWithFormat("command.kit.list.text", kitName)))
                .style(TextStyles.ITALIC);
        if (kitObj.getCost() > 0 && plugin.getEconHelper().economyServiceExists() && !kitPermissionHandler.testCostExempt(source)) {
            builder = Text.builder().append(builder.build())
                .append(plugin.getMessageProvider().getTextMessageWithFormat("command.kit.list.cost", plugin.getEconHelper().getCurrencySymbol(kitObj.getCost())));
        }

        return builder.build();
    }
}

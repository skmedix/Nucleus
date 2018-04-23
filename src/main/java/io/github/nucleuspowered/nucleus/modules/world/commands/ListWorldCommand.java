/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.world.commands;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Permissions(prefix = "world", suggestedLevel = SuggestedLevel.ADMIN)
@RegisterCommand(value = {"list", "ls"}, subcommandOf = WorldCommand.class, rootAliasRegister = "worlds")
@NonnullByDefault
public class ListWorldCommand extends AbstractCommand<CommandSource> {

    // Use a space over EMPTY so pagination doesn't mess up.
    private final static Text SPACE = Text.of(" ");

    @Override protected Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> m = super.permissionSuffixesToRegister();
        m.put("seed", PermissionInformation.getWithTranslation("permission.world.seed", SuggestedLevel.ADMIN));
        return m;
    }

    static void getWorldInfo(List<Text> listContent, WorldProperties x, boolean canSeeSeeds) {
        Nucleus plugin = Nucleus.getNucleus();
        // Name of world
        if (!listContent.isEmpty()) {
            listContent.add(SPACE);
        }

        listContent.add(plugin.getMessageProvider().getTextMessageWithFormat("command.world.list.worlditem", x.getWorldName()));

        // As requested by Pixelmon for use in their config.
        x.getAdditionalProperties().getInt(DataQuery.of("SpongeData", "dimensionId")).ifPresent(i ->
            listContent.add(plugin.getMessageProvider().getTextMessageWithFormat("command.world.list.dimensionid", String.valueOf(i))));
        Vector3i spawnPosition = x.getSpawnPosition();
        listContent.add(plugin.getMessageProvider().getTextMessageWithFormat("command.world.list.spawnpoint",
                String.valueOf(spawnPosition.getX()), String.valueOf(spawnPosition.getY()), String.valueOf(spawnPosition.getZ())));

        listContent.add(plugin.getMessageProvider().getTextMessageWithFormat("command.world.list.uuid", x.getUniqueId().toString()));
        if (x.isEnabled()) {
            boolean worldLoaded = Sponge.getServer().getWorld(x.getUniqueId()).isPresent();
            String message =
                (worldLoaded ? "&a" : "&c") + plugin.getMessageProvider().getMessageWithFormat(worldLoaded ? "standard.true" : "standard.false");
            listContent.add(plugin.getMessageProvider().getTextMessageWithFormat("command.world.list.enabled", message));
        } else {
            listContent.add(plugin.getMessageProvider().getTextMessageWithFormat("command.world.list.disabled"));
        }

        if (canSeeSeeds) {
            listContent.add(plugin.getMessageProvider().getTextMessageWithFormat("command.world.list.seed", String.valueOf(x.getSeed())));
        }

        listContent.add(plugin.getMessageProvider().getTextMessageWithFormat("command.world.list.params",
            x.getDimensionType().getName(),
            x.getGeneratorType().getName(),
            CreateWorldCommand.modifierString(x.getGeneratorModifiers()),
            x.getGameMode().getName(),
            x.getDifficulty().getName()));

        listContent.add(plugin.getMessageProvider().getTextMessageWithFormat("command.world.list.params2",
            String.valueOf(x.isHardcore()),
            String.valueOf(x.loadOnStartup()),
            String.valueOf(x.isPVPEnabled()),
            String.valueOf(x.doesKeepSpawnLoaded())
        ));
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) {
        // Get all the worlds
        Collection<WorldProperties> cwp = Sponge.getServer().getAllWorldProperties();
        final List<Text> listContent = Lists.newArrayList();

        final boolean canSeeSeeds = this.permissions.testSuffix(src, "seed");
        cwp.stream().sorted(Comparator.comparing(WorldProperties::getWorldName)).forEach(x -> getWorldInfo(listContent, x, canSeeSeeds));

        Util.getPaginationBuilder(src)
            .contents(listContent).title(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.world.list.title"))
            .sendTo(src);

        return CommandResult.success();
    }
}

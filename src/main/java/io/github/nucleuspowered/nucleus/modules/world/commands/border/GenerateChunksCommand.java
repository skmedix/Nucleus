/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.world.commands.border;

import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.argumentparsers.BoundedIntegerArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.TimespanArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.world.WorldHelper;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.HashMap;
import java.util.Map;

@Permissions(prefix = "world.border")
@RegisterCommand(value = {"gen", "genchunks", "generatechunks", "chunkgen"}, subcommandOf = BorderCommand.class)
@NonnullByDefault
public class GenerateChunksCommand extends AbstractCommand<CommandSource> {

    private static final String ticksKey = "tickPercent";
    private static final String tickFrequency = "tickFrequency";
    private static final String saveTimeKey = "time between saves";

    private final WorldHelper worldHelper = getServiceUnchecked(WorldHelper.class);

    @Override
    protected Map<String, PermissionInformation> permissionSuffixesToRegister() {
        return new HashMap<String, PermissionInformation>() {{
            put("notify", PermissionInformation.getWithTranslation("permission.world.border.gen.notify", SuggestedLevel.ADMIN));
        }};
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                GenericArguments.flags()
                    .flag("a")
                    .flag("r")
                    .valueFlag(new TimespanArgument(Text.of(saveTimeKey)), "-save")
                    .valueFlag(new BoundedIntegerArgument(Text.of(ticksKey), 0, 100), "t", "-tickpercent")
                    .valueFlag(new BoundedIntegerArgument(Text.of(tickFrequency), 1, 100), "f", "-frequency")
                    .buildWith(NucleusParameters.OPTIONAL_WORLD_PROPERTIES_ENABLED_ONLY)
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        WorldProperties wp = getWorldFromUserOrArgs(src, NucleusParameters.Keys.WORLD, args);
        if (this.worldHelper.isPregenRunningForWorld(wp.getUniqueId())) {
            throw ReturnMessageException.fromKey("command.world.gen.alreadyrunning", wp.getWorldName());
        }

        World w = Sponge.getServer().getWorld(wp.getUniqueId())
                .orElseThrow(() -> ReturnMessageException.fromKey("command.world.gen.notloaded", wp.getWorldName()));
        this.worldHelper.startPregenningForWorld(w,
                args.hasAny("a"),
                args.<Long>getOne(Text.of(GenerateChunksCommand.saveTimeKey)).orElse(20L) * 1000L,
                args.<Integer>getOne(ticksKey).orElse(null),
                args.<Integer>getOne(tickFrequency).orElse(null),
                args.hasAny("r"));

        src.sendMessage(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("command.world.gen.started", wp.getWorldName()));

        return CommandResult.success();
    }
}

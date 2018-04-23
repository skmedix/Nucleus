/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.teleport.commands;

import com.flowpowered.math.vector.Vector3i;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.argumentparsers.BoundedIntegerArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.NicknameArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.SelectorWrapperArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.internal.teleport.NucleusTeleportHandler;
import io.github.nucleuspowered.nucleus.util.CauseStackHelper;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.CommandFlags;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.HashMap;
import java.util.Map;

@Permissions(prefix = "teleport", supportsOthers = true)
@NoModifiers
@NonnullByDefault
@RegisterCommand({"tppos"})
@EssentialsEquivalent("tppos")
public class TeleportPositionCommand extends AbstractCommand<CommandSource> {

    private final String key = "subject";
    private final String location = "world";
    private final String x = "x";
    private final String y = "y";
    private final String z = "z";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                GenericArguments.flags()
                    .setUnknownShortFlagBehavior(CommandFlags.UnknownFlagBehavior.IGNORE)
                    .flag("f", "-force")
                    .flag("c", "-chunk")
                    .permissionFlag(this.permissions.getPermissionWithSuffix("exempt.bordercheck"),"b", "-border")
                    .buildWith(
                        GenericArguments.seq(
                            // Actual arguments
                            GenericArguments.optionalWeak(
                                    GenericArguments.requiringPermission(
                                            GenericArguments.onlyOne(
                                                    SelectorWrapperArgument.nicknameSelector(Text.of(this.key), NicknameArgument.UnderlyingType.PLAYER)),

                                            this.permissions.getOthers())),
                            GenericArguments.onlyOne(GenericArguments.optionalWeak(GenericArguments.world(Text.of(this.location)))),
                            GenericArguments.onlyOne(new BoundedIntegerArgument(Text.of(this.x), Integer.MIN_VALUE, Integer.MAX_VALUE)),
                            GenericArguments.onlyOne(new BoundedIntegerArgument(Text.of(this.y), 0, 255)),
                            GenericArguments.onlyOne(new BoundedIntegerArgument(Text.of(this.z), Integer.MIN_VALUE, Integer.MAX_VALUE))
                        )
                )
        };
    }

    @Override
    protected Map<String, PermissionInformation> permissionSuffixesToRegister() {
        return new HashMap<String, PermissionInformation>() {{
            put("exempt.bordercheck", PermissionInformation.getWithTranslation("permission.tppos.border", SuggestedLevel.ADMIN));
        }};
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Player pl = this.getUserFromArgs(Player.class, src, this.key, args);
        WorldProperties wp = args.<WorldProperties>getOne(this.location).orElse(pl.getWorld().getProperties());
        World world = Sponge.getServer().loadWorld(wp.getUniqueId()).get();

        int xx = args.<Integer>getOne(this.x).get();
        int zz = args.<Integer>getOne(this.z).get();
        int yy = args.<Integer>getOne(this.y).get();
        if (yy < 0) {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.tppos.ysmall"));
            return CommandResult.empty();
        }

        // Chunks are 16 in size, chunk 0 is from 0 - 15, -1 from -1 to -16.
        if (args.hasAny("c")) {
            xx = xx * 16 + 8;
            yy = yy * 16 + 8;
            zz = zz * 16 + 8;
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.tppos.fromchunk",
                    String.valueOf(xx), String.valueOf(yy), String.valueOf(zz)));
        }

        Vector3i max = world.getBlockMax();
        Vector3i min = world.getBlockMin();
        if (!(isBetween(xx, max.getX(), min.getX()) && isBetween(yy, max.getY(), min.getY()) && isBetween(zz, max.getZ(), min.getZ()))) {
            throw ReturnMessageException.fromKey("command.tppos.invalid");
        }

        // Create the location
        Location<World> loc = new Location<>(world, xx, yy, zz);
        if (!args.hasAny("b") && !Util.isLocationInWorldBorder(loc)) {
            throw ReturnMessageException.fromKey("command.tppos.worldborder");
        }

        NucleusTeleportHandler teleportHandler = Nucleus.getNucleus().getTeleportHandler();
        Cause cause = CauseStackHelper.createCause(src);

        // Don't bother with the safety if the flag is set.
        if (args.<Boolean>getOne("f").orElse(false)) {
            if (teleportHandler.teleportPlayer(pl, loc, NucleusTeleportHandler.StandardTeleportMode.NO_CHECK, cause).isSuccess()) {
                pl.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.tppos.success.self"));
                if (!src.equals(pl)) {
                    src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.tppos.success.other", pl.getName()));
                }

                return CommandResult.success();
            }

            throw ReturnMessageException.fromKey("command.tppos.cancelledevent");
        }

        // If we have a chunk, scan the whole chunk.
        NucleusTeleportHandler.StandardTeleportMode mode = teleportHandler.getTeleportModeForPlayer(pl);
        if (args.hasAny("c") && mode == NucleusTeleportHandler.StandardTeleportMode.FLYING_THEN_SAFE) {
            mode = NucleusTeleportHandler.StandardTeleportMode.FLYING_THEN_SAFE_CHUNK;
        }

        NucleusTeleportHandler.TeleportResult result = teleportHandler.teleportPlayer(pl, loc, mode, cause, true);
        if (result.isSuccess()) {
            pl.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.tppos.success.self"));
            if (!src.equals(pl)) {
                src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.tppos.success.other", pl.getName()));
            }

            return CommandResult.success();
        } else if (result == NucleusTeleportHandler.TeleportResult.FAILED_NO_LOCATION) {
            throw ReturnMessageException.fromKey("command.tppos.nosafe");
        }

        throw ReturnMessageException.fromKey("command.tppos.cancelledevent");
    }

    private boolean isBetween(int toCheck, int max, int min) {
        return toCheck >= min && toCheck <= max;
    }
}

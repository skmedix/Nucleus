/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.jump.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.argumentparsers.NicknameArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.SelectorWrapperArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.internal.teleport.NucleusTeleportHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.util.blockray.BlockRay;
import org.spongepowered.api.util.blockray.BlockRayHit;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.HashMap;
import java.util.Map;

@Permissions(supportsSelectors = true)
@RegisterCommand({"top", "tosurface", "totop"})
@EssentialsEquivalent("top")
@NonnullByDefault
public class TopCommand extends AbstractCommand<CommandSource> {

    private final String playerKey = "subject";

    @Override protected Map<String, PermissionInformation> permissionSuffixesToRegister() {
        return new HashMap<String, PermissionInformation>() {{
            put("others", PermissionInformation.getWithTranslation("permission.top.others", SuggestedLevel.ADMIN));
        }};
    }

    @Override public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.flags().flag("f").buildWith(
                GenericArguments.optional(
                GenericArguments.requiringPermission(
                    GenericArguments.onlyOne(
                        SelectorWrapperArgument.nicknameSelector(Text.of(this.playerKey), NicknameArgument.UnderlyingType.PLAYER)),
                        this.permissions.getOthers())))
        };
    }

    @Override public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Player playerToTeleport = this.getUserFromArgs(Player.class, src, this.playerKey, args);

        // Get the topmost block for the subject.
        Location<World> location = playerToTeleport.getLocation();
        double x = location.getX();
        double z = location.getZ();
        Location<World> start = new Location<>(location.getExtent(), x, location.getExtent().getBlockMax().getY(), z);
        BlockRayHit<World> end = BlockRay.from(start).stopFilter(BlockRay.onlyAirFilter())
            .to(playerToTeleport.getLocation().getPosition().sub(0, 1, 0)).end()
            .orElseThrow(() -> new ReturnMessageException(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.top.nothingfound")));

        if (playerToTeleport.getLocation().getBlockPosition().equals(end.getBlockPosition())) {
            if (!playerToTeleport.equals(src)) {
                throw new ReturnMessageException(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.top.attop.other",
                        Nucleus.getNucleus().getNameUtil().getSerialisedName(playerToTeleport)));
            } else {
                throw new ReturnMessageException(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.top.attop.self"));
            }
        }

        NucleusTeleportHandler.TeleportResult result = Nucleus.getNucleus().getTeleportHandler()
                .teleportPlayer(playerToTeleport, end.getLocation(), !args.hasAny("f"));
        if (result.isSuccess()) {
            // OK
            if (!playerToTeleport.equals(src)) {
                src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.top.success.other",
                        Nucleus.getNucleus().getNameUtil().getSerialisedName(playerToTeleport)));
            }

            playerToTeleport.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.top.success.self"));
            return CommandResult.success();
        }

        if (result == NucleusTeleportHandler.TeleportResult.FAILED_NO_LOCATION) {
            throw new ReturnMessageException(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.top.notsafe"));
        } else {
            throw new ReturnMessageException(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.top.cancelled"));
        }
    }
}

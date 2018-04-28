/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.admin.commands.gamemode;

import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.argumentparsers.ImprovedGameModeArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.NoneThrowOnCompleteArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameMode;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Map;
import java.util.Optional;

@Permissions
@RegisterCommand({"gamemode", "gm"})
@NonnullByDefault
@EssentialsEquivalent(value = {"gamemode", "gm"}, isExact = false, notes = "/gm does not toggle between survival and creative, use /gmt for that")
public class GamemodeCommand extends GamemodeBase<CommandSource> {

    private final String gamemodeKey = "gamemode";
    private final String gamemodeself = "gamemode_self";

    @Override
    protected Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> mpi = Maps.newHashMap();
        mpi.put("others", PermissionInformation.getWithTranslation("permission.gamemode.other", SuggestedLevel.ADMIN));
        mpi.put("modes.survival", PermissionInformation.getWithTranslation("permission.gamemode.modes.survival", SuggestedLevel.ADMIN));
        mpi.put("modes.creative", PermissionInformation.getWithTranslation("permission.gamemode.modes.creative", SuggestedLevel.ADMIN));
        mpi.put("modes.adventure", PermissionInformation.getWithTranslation("permission.gamemode.modes.adventure", SuggestedLevel.ADMIN));
        mpi.put("modes.spectator", PermissionInformation.getWithTranslation("permission.gamemode.modes.spectator", SuggestedLevel.ADMIN));
        return mpi;
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                GenericArguments.firstParsing(
                        GenericArguments.requiringPermission(GenericArguments.seq(
                                NucleusParameters.ONE_PLAYER,
                                GenericArguments.onlyOne(new ImprovedGameModeArgument(Text.of(this.gamemodeKey)))
                        ), this.permissions.getOthers()),
                        GenericArguments.onlyOne(new ImprovedGameModeArgument(Text.of(this.gamemodeself))),
                        NoneThrowOnCompleteArgument.INSTANCE
                )
        };
    }

    @Override
    protected CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Player user;
        Optional<GameMode> ogm;
        if (args.hasAny(this.gamemodeself)) {
            user = this.getUserFromArgs(Player.class, src, "thisisjunk", args);
            ogm = args.getOne(this.gamemodeself);
        } else {
            user = this.getUserFromArgs(Player.class, src, NucleusParameters.Keys.PLAYER, args);
            ogm = args.getOne(this.gamemodeKey);
        }

        if (!ogm.isPresent()) {
            String mode = user.get(Keys.GAME_MODE).orElse(GameModes.SURVIVAL).getName();
            if (src.equals(user)) {
                src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.gamemode.get.base", mode));
            } else {
                src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.gamemode.get.other", user.getName(), mode));
            }

            return CommandResult.success();
        }

        GameMode gm = ogm.get();
        return baseCommand(src, user, gm);
    }
}

/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.teleport.commands;

import io.github.nucleuspowered.nucleus.argumentparsers.AlternativeUsageArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.IfConditionElseArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.NicknameArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.PlayerConsoleArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.SelectorWrapperArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ContinueMode;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.messages.MessageProvider;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SubjectPermissionCache;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.internal.teleport.NucleusTeleportHandler;
import io.github.nucleuspowered.nucleus.modules.core.datamodules.CoreUserDataModule;
import io.github.nucleuspowered.nucleus.modules.teleport.config.TeleportConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.teleport.handlers.TeleportHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import javax.inject.Inject;

@Permissions(prefix = "teleport", mainOverride = "teleport", suggestedLevel = SuggestedLevel.MOD, supportsOthers = true)
@RegisterCommand(value = "teleport", rootAliasRegister = "tp")
@EssentialsEquivalent(value = {"tp", "tele", "tp2p", "teleport", "tpo"}, isExact = false,
        notes = "If you have permission, this will override '/tptoggle' automatically.")
@NonnullByDefault
public class TeleportCommand extends AbstractCommand<CommandSource> {

    private final String playerToKey = "Player to warp to";
    private final String playerKey = "subject";
    private final String quietKey = "quiet";

    @SuppressWarnings("NullableProblems") @Inject private TeleportHandler handler;
    @SuppressWarnings("NullableProblems") @Inject private TeleportConfigAdapter tca;

    @Override
    public Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> m = new HashMap<>();
        m.put("offline", PermissionInformation.getWithTranslation("permission.teleport.offline", SuggestedLevel.ADMIN));
        m.put("quiet", PermissionInformation.getWithTranslation("permission.teleport.quiet", SuggestedLevel.ADMIN));
        return m;
    }

    @Override
    public CommandElement[] getArguments() {
       return new CommandElement[]{
                GenericArguments.flags().flag("f")
                    .setAnchorFlags(true)
                    .valueFlag(GenericArguments.requiringPermission(GenericArguments.bool(Text.of(quietKey)), permissions.getPermissionWithSuffix("quiet")), "q")
                    .buildWith(GenericArguments.none()),

                    new AlternativeUsageArgument(
                        GenericArguments.seq(
                            GenericArguments.onlyOne(
                                IfConditionElseArgument.permission(this.permissions.getPermissionWithSuffix("offline"),
                                    SelectorWrapperArgument.nicknameSelector(Text.of(playerKey), NicknameArgument.UnderlyingType.USER),
                                    SelectorWrapperArgument.nicknameSelector(Text.of(playerKey), NicknameArgument.UnderlyingType.PLAYER,
                                        true, Player.class, (c, p) -> PlayerConsoleArgument.shouldShow(p, c)))),

                            new IfConditionElseArgument(
                                GenericArguments.optionalWeak(
                                    SelectorWrapperArgument.nicknameSelector(Text.of(playerToKey), NicknameArgument.UnderlyingType.PLAYER,
                                    true, Player.class, (c, p) -> PlayerConsoleArgument.shouldShow(p, c))),
                                GenericArguments.none(),
                                this::testForSecondPlayer)),

                        src -> {
                            StringBuilder sb = new StringBuilder();
                            sb.append("<player to warp to>");
                            if (permissions.testOthers(src)) {
                                sb.append("|<player to warp> <player to warp to>");
                            }

                            if (permissions.testOthers(src)) {
                                sb.append("|<offline player to warp to>");
                            }

                            return Text.of(sb.toString());
                        }
                    )
       };
    }

    private boolean testForSecondPlayer(CommandSource source, CommandContext context) {
        try {
            if (context.hasAny(playerKey) && this.permissions.testOthers(source)) {
                return context.<User>getOne(playerKey).map(y -> y.getPlayer().isPresent()).orElse(false);
            }
        } catch (Exception e) {
            // ignored
        }

        return false;
    }

    @Override protected ContinueMode preProcessChecks(SubjectPermissionCache<CommandSource> source, CommandContext args) {
        return TeleportHandler.canTeleportTo(source, args.<User>getOne(playerKey).get()) ? ContinueMode.CONTINUE : ContinueMode.STOP;
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        boolean beQuiet = args.<Boolean>getOne(quietKey).orElse(tca.getNodeOrDefault().isDefaultQuiet());
        Optional<Player> oTo = args.getOne(playerToKey);
        User to;
        Player from;
        if (oTo.isPresent()) { // Two player argument.
            from = args.<User>getOne(playerKey).map(x -> x.getPlayer().orElse(null))
                .orElseThrow(() -> ReturnMessageException.fromKey("command.playeronly"));
            to = oTo.get();
            if (to.equals(src)) {
                throw ReturnMessageException.fromKey("command.teleport.player.noself");
            }
        } else if (src instanceof Player) {
            from = (Player) src;
            to = args.<User>getOne(playerKey).get();
        } else {
            throw ReturnMessageException.fromKey("command.playeronly");
        }

        if (to.getPlayer().isPresent()) {
            if (handler.getBuilder().setSource(src).setFrom(from).setTo(to.getPlayer().get()).setSafe(!args.<Boolean>getOne("f").orElse(false))
                    .setSilentTarget(beQuiet).startTeleport()) {
                return CommandResult.success();
            }

            return CommandResult.empty();
        }

        // We have an offline player.
        permissions.checkSuffix(src, "offline", () -> ReturnMessageException.fromKey("command.teleport.noofflineperms"));

        // Can we get a location?
        Supplier<ReturnMessageException> r = () -> ReturnMessageException.fromKey("command.teleport.nolastknown", to.getName());
        Location<World> l = plugin.getUserDataManager().get(to.getUniqueId()).orElseThrow(r).get(CoreUserDataModule.class).getLogoutLocation()
                .orElseThrow(r);

        MessageProvider provider = plugin.getMessageProvider();
        if (plugin.getTeleportHandler()
                .teleportPlayer(from, l, NucleusTeleportHandler.TeleportMode.FLYING_THEN_SAFE, Cause.of(NamedCause.owner(src))).isSuccess()) {
            if (!(src instanceof Player && ((Player) src).getUniqueId().equals(from.getUniqueId()))) {
                src.sendMessage(provider.getTextMessageWithFormat("command.teleport.offline.other", from.getName(), to.getName()));
            }

            from.sendMessage(provider.getTextMessageWithFormat("command.teleport.offline.self", to.getName()));
            return CommandResult.success();
        }

        throw ReturnMessageException.fromKey("command.teleport.error");
    }

}

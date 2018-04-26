/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.commands.kit;

import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.exceptions.KitRedeemException;
import io.github.nucleuspowered.nucleus.api.nucleusdata.Kit;
import io.github.nucleuspowered.nucleus.api.service.NucleusKitService;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.kit.commands.KitFallbackBase;
import io.github.nucleuspowered.nucleus.modules.kit.config.KitConfigAdapter;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Map;

/**
 * Gives a kit to a subject.
 */
@Permissions(prefix = "kit")
@RegisterCommand(value = "give", subcommandOf = KitCommand.class)
@NonnullByDefault
public class KitGiveCommand extends KitFallbackBase<CommandSource> implements Reloadable {

    private boolean mustGetAll;
    private boolean isDrop;

    @Override protected Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> mspi = Maps.newHashMap();
        mspi.put("overridecheck", PermissionInformation.getWithTranslation("permission.kit.give.override", SuggestedLevel.ADMIN));
        return mspi;
    }

    @Override public CommandElement[] getArguments() {
        return new CommandElement[] {
                GenericArguments.flags().permissionFlag(this.permissions.getPermissionWithSuffix("overridecheck"), "i", "-ignore")
                    .buildWith(GenericArguments.seq(
                        NucleusParameters.ONE_PLAYER,
                        KitFallbackBase.KIT_PARAMETER_NO_PERM_CHECK
                ))
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {

        Kit kit = args.<Kit>getOne(KIT_PARAMETER_KEY).get();
        Player player = args.<Player>getOne(NucleusParameters.Keys.PLAYER).get();
        boolean skip = args.hasAny("i");
        if (src instanceof Player && player.getUniqueId().equals(((Player) src).getUniqueId())) {
            throw new ReturnMessageException(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.kit.give.self"));
        }

        Text playerName = Nucleus.getNucleus().getNameUtil().getName(player);
        Text kitName = Text.of(kit.getName());
        try {
            NucleusKitService.RedeemResult redeemResult = KIT_HANDLER.redeemKit(kit, player, !skip, this.mustGetAll);
            if (!redeemResult.rejected().isEmpty()) {
                // If we drop them, tell the user
                if (this.isDrop) {
                    player.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithTextFormat("command.kit.give.itemsdropped", playerName));
                    redeemResult.rejected().forEach(x -> Util.dropItemOnFloorAtLocation(x, player.getLocation()));
                } else {
                    player.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithTextFormat("command.kit.give.fullinventory", playerName));
                }
            }

            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithTextFormat("command.kit.give.spawned", playerName, kitName));
            if (kit.isDisplayMessageOnRedeem()) {
                player.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.kit.spawned", kit.getName()));
            }

            return CommandResult.success();
        } catch (KitRedeemException ex) {
            switch (ex.getReason()) {
                case ALREADY_REDEEMED:
                    throw ReturnMessageException.fromKeyText("command.kit.give.onetime.alreadyredeemed", kitName, playerName);
                case COOLDOWN_NOT_EXPIRED:
                    KitRedeemException.Cooldown kre = (KitRedeemException.Cooldown) ex;
                    throw ReturnMessageException.fromKeyText("command.kit.give.cooldown",
                            playerName,
                            Text.of(Util.getTimeStringFromSeconds(kre.getTimeLeft().getSeconds())),
                            kitName);
                case PRE_EVENT_CANCELLED:
                    KitRedeemException.PreCancelled krepe = (KitRedeemException.PreCancelled) ex;
                    throw new ReturnMessageException(krepe.getCancelMessage()
                            .orElseGet(() -> (Nucleus.getNucleus()
                                    .getMessageProvider().getTextMessageWithFormat("command.kit.cancelledpre", kit.getName()))));
                case NO_SPACE:
                    throw ReturnMessageException.fromKeyText("command.kit.give.fullinventorynosave", playerName);
                case UNKNOWN:
                default:
                    throw ReturnMessageException.fromKeyText("command.kit.give.fail", playerName, kitName);
            }
        }

    }

    @Override
    public void onReload() {
        KitConfigAdapter kca = getServiceUnchecked(KitConfigAdapter.class);
        this.isDrop = kca.getNodeOrDefault().isDropKitIfFull();
        this.mustGetAll = kca.getNodeOrDefault().isMustGetAll();
    }
}

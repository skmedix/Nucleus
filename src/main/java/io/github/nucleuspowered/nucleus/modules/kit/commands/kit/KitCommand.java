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
import io.github.nucleuspowered.nucleus.internal.EconHelper;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoCooldown;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoCost;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.kit.commands.KitFallbackBase;
import io.github.nucleuspowered.nucleus.modules.kit.config.KitConfigAdapter;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Map;

/**
 * Allows a user to redeem a kit.
 */
@Permissions(suggestedLevel = SuggestedLevel.USER)
@RegisterCommand("kit")
@NoCooldown // This is determined by the kit itself.
@NoCost // This is determined by the kit itself.
@NonnullByDefault
@EssentialsEquivalent(value = "kit, kits", isExact = false, notes = "'/kit' redeems, '/kits' lists.")
public class KitCommand extends KitFallbackBase<Player> implements Reloadable {

    private boolean isDrop;
    private boolean mustGetAll;

    @Override
    protected boolean allowFallback(CommandSource source, CommandArgs args, CommandContext context) {
        return true;
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                KitFallbackBase.KIT_PARAMETER_PERM_CHECK
        };
    }

    @Override
    protected Map<String, PermissionInformation> permissionsToRegister() {
        Map<String, PermissionInformation> pi = Maps.newHashMap();
        pi.put(PermissionRegistry.PERMISSIONS_PREFIX + "kits",
                PermissionInformation.getWithTranslation("permission.kits", SuggestedLevel.ADMIN));
        return pi;
    }

    @Override
    protected Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> pi = Maps.newHashMap();
        pi.put("exempt.cooldown", PermissionInformation.getWithTranslation("permission.kit.exempt.cooldown", SuggestedLevel.ADMIN));
        pi.put("exempt.onetime", PermissionInformation.getWithTranslation("permission.kit.exempt.onetime", SuggestedLevel.ADMIN));
        pi.put("showhidden", PermissionInformation.getWithTranslation("permission.kit.showhidden", SuggestedLevel.ADMIN));
        return pi;
    }

    @Override
    public CommandResult executeCommand(Player player, CommandContext args) throws ReturnMessageException {
        Kit kit = args.<Kit>getOne(KIT_PARAMETER_KEY).get();

        EconHelper econHelper = Nucleus.getNucleus().getEconHelper();
        double cost = econHelper.economyServiceExists() ? kit.getCost() : 0;
        if (this.permissions.testCostExempt(player)) {
            // If exempt - no cost.
            cost = 0;
        }

        // If we have a cost for the kit, check we have funds.
        if (cost > 0 && !econHelper.hasBalance(player, cost)) {
            throw ReturnMessageException.fromKey("command.kit.notenough", kit.getName(), econHelper.getCurrencySymbol(cost));
        }

        try {
            NucleusKitService.RedeemResult redeemResult =
                    KIT_HANDLER.redeemKit(kit, player, true, this.mustGetAll);
            if (!redeemResult.rejected().isEmpty()) {
                // If we drop them, tell the user
                if (this.isDrop) {
                    player.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.kit.itemsdropped"));
                    redeemResult.rejected().forEach(x -> Util.dropItemOnFloorAtLocation(x, player.getLocation()));
                } else {
                    player.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.kit.fullinventory"));
                }
            }

            if (kit.isDisplayMessageOnRedeem()) {
                player.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.kit.spawned", kit.getName()));
            }

            // Charge, if necessary
            if (cost > 0 && econHelper.economyServiceExists()) {
                econHelper.withdrawFromPlayer(player, cost);
            }

            return CommandResult.success();
        } catch (KitRedeemException ex) {
            switch (ex.getReason()) {
                case ALREADY_REDEEMED:
                    throw ReturnMessageException.fromKey("command.kit.onetime.alreadyredeemed", kit.getName());
                case COOLDOWN_NOT_EXPIRED:
                    KitRedeemException.Cooldown kre = (KitRedeemException.Cooldown) ex;
                    throw ReturnMessageException.fromKey("command.kit.cooldown",
                            Util.getTimeStringFromSeconds(kre.getTimeLeft().getSeconds()), kit.getName());
                case PRE_EVENT_CANCELLED:
                    KitRedeemException.PreCancelled krepe = (KitRedeemException.PreCancelled) ex;
                    throw new ReturnMessageException(krepe.getCancelMessage()
                            .orElseGet(() -> (Nucleus.getNucleus()
                                    .getMessageProvider().getTextMessageWithFormat("command.kit.cancelledpre", kit.getName()))));
                case NO_SPACE:
                    throw ReturnMessageException.fromKey("command.kit.fullinventorynosave", kit.getName());
                case UNKNOWN:
                default:
                    throw ReturnMessageException.fromKey("command.kit.fail", kit.getName());
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

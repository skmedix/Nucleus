/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal;

import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.util.CauseStackHelper;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;

import java.math.BigDecimal;
import java.util.Optional;

public class EconHelper {

    public boolean economyServiceExists() {
        return Sponge.getServiceManager().provide(EconomyService.class).isPresent();
    }

    public String getCurrencySymbol(double cost) {
        Optional<EconomyService> oes = Sponge.getServiceManager().provide(EconomyService.class);
        return oes.map(economyService -> economyService.getDefaultCurrency().format(BigDecimal.valueOf(cost)).toPlain())
                .orElseGet(() -> String.valueOf(cost));

    }

    public boolean hasBalance(Player src, double balance) {
        Optional<EconomyService> oes = Sponge.getServiceManager().provide(EconomyService.class);
        if (oes.isPresent()) {
            // Check balance.
            EconomyService es = oes.get();
            Optional<UniqueAccount> ua = es.getOrCreateAccount(src.getUniqueId());
            return ua.isPresent() && ua.get().getBalance(es.getDefaultCurrency()).doubleValue() >= balance;
        }

        // No economy
        return true;
    }

    public boolean withdrawFromPlayer(Player src, double cost) {
        return withdrawFromPlayer(src, cost, true);
    }

    public boolean withdrawFromPlayer(Player src, double cost, boolean message) {
        Optional<EconomyService> oes = Sponge.getServiceManager().provide(EconomyService.class);
        if (oes.isPresent()) {
            // Check balance.
            EconomyService es = oes.get();
            Optional<UniqueAccount> a = es.getOrCreateAccount(src.getUniqueId());
            if (!a.isPresent()) {
                src.sendMessage(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("cost.noaccount"));
                return false;
            }

            TransactionResult tr = a.get().withdraw(es.getDefaultCurrency(), BigDecimal.valueOf(cost), CauseStackHelper.createCause(src));
            if (tr.getResult() == ResultType.ACCOUNT_NO_FUNDS) {
                if (message) {
                    src.sendMessage(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("cost.nofunds", getCurrencySymbol(cost)));
                }

                return false;
            } else if (tr.getResult() != ResultType.SUCCESS) {
                src.sendMessage(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("cost.error"));
                return false;
            }

            if (message) {
                src.sendMessage(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("cost.complete", getCurrencySymbol(cost)));
            }
        }

        return true;
    }

    public boolean depositInPlayer(User src, double cost) {
        return depositInPlayer(src, cost, true);
    }

    public boolean depositInPlayer(User src, double cost, boolean message) {
        Optional<EconomyService> oes = Sponge.getServiceManager().provide(EconomyService.class);
        if (oes.isPresent()) {
            // Check balance.
            EconomyService es = oes.get();
            Optional<UniqueAccount> a = es.getOrCreateAccount(src.getUniqueId());
            if (!a.isPresent()) {
                src.getPlayer().ifPresent(x ->
                        x.sendMessage(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("cost.noaccount")));
                return false;
            }

            TransactionResult tr = a.get().deposit(es.getDefaultCurrency(), BigDecimal.valueOf(cost), CauseStackHelper.createCause(src));
            if (tr.getResult() != ResultType.SUCCESS && src.isOnline()) {
                src.getPlayer().ifPresent(x ->
                        x.sendMessage(NucleusPlugin.getNucleus().getMessageProvider().getTextMessageWithFormat("cost.error")));
                return false;
            }

            if (message && src.isOnline()) {
                src.getPlayer().ifPresent(x ->
                        x.sendMessage(NucleusPlugin.getNucleus().getMessageProvider()
                                .getTextMessageWithFormat("cost.refund", getCurrencySymbol(cost))));
            }
        }

        return true;
    }
}

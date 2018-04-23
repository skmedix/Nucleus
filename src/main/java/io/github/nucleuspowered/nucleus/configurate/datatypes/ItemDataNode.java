/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.configurate.datatypes;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import io.github.nucleuspowered.neutrino.annotations.ProcessSetting;
import io.github.nucleuspowered.nucleus.configurate.datatypes.item.PriceNode;
import io.github.nucleuspowered.nucleus.configurate.settingprocessor.ItemAliasSettingProcessor;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@ConfigSerializable
public class ItemDataNode {

    public final static Pattern ALIAS_PATTERN = Pattern.compile("^[a-z0-9_-]+$");

    public ItemDataNode() {}

    private ItemDataNode(Set<String> aliases, double buy, double sell) {
        this.aliases = aliases;
        this.shop = new PriceNode(buy, sell);
    }

    public ItemDataNode(ItemDataNode copy) {
        this(copy.aliases, copy.shop.getBuy(), copy.shop.getSell());
    }

    @Setting(comment = "loc:config.itemdatanode.aliases")
    @ProcessSetting(ItemAliasSettingProcessor.class)
    private Set<String> aliases = new HashSet<>();

    @Setting
    private PriceNode shop = new PriceNode();

    public List<String> getAliases() {
        return ImmutableList.copyOf(this.aliases);
    }

    public void addAlias(String alias) {
        String lowerCase = alias.toLowerCase();
        Preconditions.checkArgument(ALIAS_PATTERN.matcher(lowerCase).matches());

        this.aliases.add(lowerCase);
    }

    public void removeAlias(String alias) {
        alias = alias.toLowerCase();
        this.aliases.remove(alias);
    }

    public void clearAliases() {
        this.aliases.clear();
    }

    public double getServerBuyPrice() {
        return this.shop.getBuy();
    }

    public void setServerBuyPrice(double serverBuyPrice) {
        this.shop.setBuy(serverBuyPrice);
    }

    public double getServerSellPrice() {
        return this.shop.getSell();
    }

    public void setServerSellPrice(double serverSellPrice) {
        this.shop.setSell(serverSellPrice);
    }

}

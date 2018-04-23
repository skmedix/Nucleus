/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.dataservices;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.configurate.datatypes.ItemDataNode;
import io.github.nucleuspowered.nucleus.dataservices.dataproviders.DataProvider;
import io.github.nucleuspowered.nucleus.util.Action;
import io.github.nucleuspowered.nucleus.util.Tuples;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.Tuple;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ItemDataService extends AbstractService<Map<String, ItemDataNode>> {

    private Map<String, String> aliasToItemIdCache = null;
    private Map<CatalogType, Double> buyCache = null;
    private Map<CatalogType, Double> sellCache = null;
    private final Set<Action> onItemUpdate = Sets.newHashSet();

    public ItemDataService(DataProvider<Map<String, ItemDataNode>> dataProvider) {
        super(dataProvider);
    }

    public void addOnItemUpdate(Action onUpdate) {
        this.onItemUpdate.add(onUpdate);
    }

    @Override
    protected String serviceName() {
        return "Item Information";
    }

    @Override
    public boolean load() {
        if (super.load()) {
            clearCache();
            return true;
        }

        return false;
    }

    @Override
    public void saveInternal() throws Exception {
        clearCache();
        super.saveInternal();
    }

    public Set<String> getAliases() {
        return getCache().keySet();
    }

    public ItemDataNode getDataForItem(ItemStackSnapshot itemStackSnapshot) {
        return getDataForItem(getIdFromSnapshot(itemStackSnapshot));
    }

    public ItemDataNode getDataForItem(String id) {
        Preconditions.checkNotNull(id);
        return this.data.getOrDefault(id.toLowerCase(), new ItemDataNode());
    }

    public void setDataForItem(ItemStackSnapshot itemStackSnapshot, ItemDataNode node) {
        setDataForItem(getIdFromSnapshot(itemStackSnapshot), node);
    }

    public void setDataForItem(String id, ItemDataNode node) {
        Preconditions.checkNotNull(id);
        Preconditions.checkNotNull(node);
        this.data.put(id, node);
        save();
    }

    public void resetDataForItem(ItemStackSnapshot itemStackSnapshot) {
        resetDataForItem(getIdFromSnapshot(itemStackSnapshot));
    }

    private void resetDataForItem(String id) {
        Preconditions.checkNotNull(id);
        this.data.remove(id);
        save();
    }

    public Optional<String> getIdFromAlias(String alias) {
        return Optional.ofNullable(getCache().get(alias));
    }

    public Map<CatalogType, Double> getServerBuyPrices() {
        if (this.buyCache == null) {
            this.buyCache = this.data.entrySet().stream().filter(x -> x.getValue().getServerBuyPrice() >= 0)
                    .map(this::toCt)
                    .collect(Collectors.toMap(Tuple::getFirst, x -> x.getSecond().getServerBuyPrice()));
        }

        return this.buyCache;
    }

    public Map<CatalogType, Double> getServerSellPrices() {
        if (this.sellCache == null) {
            this.sellCache = this.data.entrySet().stream().filter(x -> x.getValue().getServerSellPrice() >= 0)
                    .map(this::toCt)
                    .collect(Collectors.toMap(Tuple::getFirst, x -> x.getSecond().getServerSellPrice()));
        }

        return this.sellCache;
    }

    private <T> Tuple<CatalogType, T> toCt(Map.Entry<String, T> x) {
        Optional<CatalogType> catalogType = Util.getCatalogTypeForItemFromId(x.getKey());
        return catalogType.map(catalogType1 -> Tuples.of(catalogType1, x.getValue())).orElseGet(() -> Tuples.of(ItemTypes.NONE, x.getValue()));
    }

    private Map<String, String> getCache() {
        if (this.aliasToItemIdCache == null) {
            this.aliasToItemIdCache = this.data.entrySet().stream()
                    .flatMap(k -> k.getValue().getAliases().stream().map(i -> Tuple.of(i, k.getKey())))
                    .collect(Collectors.toMap(Tuple::getFirst, Tuple::getSecond));
        }

        return this.aliasToItemIdCache;
    }

    private String getIdFromSnapshot(ItemStackSnapshot stackSnapshot) {
        Optional<BlockState> blockState = stackSnapshot.get(Keys.ITEM_BLOCKSTATE);
        return blockState.map(blockState1 -> blockState1.getId().toLowerCase()).orElseGet(() -> stackSnapshot.getType().getId());

    }

    private void clearCache() {
        this.aliasToItemIdCache = null;
        this.buyCache = null;
        this.sellCache = null;
        this.onItemUpdate.forEach(Action::action);
    }
}

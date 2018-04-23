/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.configurate.typeserialisers;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.nucleus.configurate.wrappers.NucleusItemStackSnapshot;
import io.github.nucleuspowered.nucleus.util.TypeHelper;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.persistence.DataTranslators;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.Tuple;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class NucleusItemStackSnapshotSerialiser implements TypeSerializer<NucleusItemStackSnapshot> {

    private final TypeToken<ItemStackSnapshot> iss = TypeToken.of(ItemStackSnapshot.class);

    @Override
    public NucleusItemStackSnapshot deserialize(TypeToken<?> type, ConfigurationNode value) {
        // Process enchantments, temporary fix before Sponge gets a more general fix in.
        boolean emptyEnchant = false;
        ConfigurationNode ench = value.getNode("UnsafeData", "ench");
        if (!ench.isVirtual()) {
            List<? extends ConfigurationNode> enchantments = ench.getChildrenList();
            if (enchantments.isEmpty()) {
                // Remove empty enchantment list.
                value.getNode("UnsafeData").removeChild("ench");
            } else {
                enchantments.forEach(x -> {
                    try {
                        short id = Short.parseShort(x.getNode("id").getString());
                        short lvl = Short.parseShort(x.getNode("lvl").getString());

                        x.getNode("id").setValue(id);
                        x.getNode("lvl").setValue(lvl);
                    } catch (NumberFormatException e) {
                        x.setValue(null);
                    }
                });
            }
        }

        ConfigurationNode data = value.getNode("Data");
        if (!data.isVirtual() && data.hasListChildren()) {
            List<? extends ConfigurationNode> n = data.getChildrenList().stream()
                .filter(x ->
                        !x.getNode("DataClass").getString("").endsWith("SpongeEnchantmentData")
                    || (!x.getNode("ManipulatorData", "ItemEnchantments").isVirtual() && x.getNode("ManipulatorData", "ItemEnchantments").hasListChildren()))
                .collect(Collectors.toList());
            emptyEnchant = n.size() != data.getChildrenList().size();

            if (emptyEnchant) {
                if (n.isEmpty()) {
                    value.removeChild("Data");
                } else {
                    value.getNode("Data").setValue(n);
                }
            }
        }

        DataContainer dataContainer = DataTranslators.CONFIGURATION_NODE.translate(value);
        Set<DataQuery> ldq = dataContainer.getKeys(true);

        for (DataQuery dataQuery : ldq) {
            String el = dataQuery.asString(".");
            if (el.contains("$Array$")) {
                try {
                    Tuple<DataQuery, Object> r = TypeHelper.getArray(dataQuery, dataContainer);
                    dataContainer.set(r.getFirst(), r.getSecond());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                dataContainer.remove(dataQuery);
            }
        }

        ItemStack snapshot;
        try {
            snapshot = ItemStack.builder().fromContainer(dataContainer).build();
        } catch (Exception e) {
            return NucleusItemStackSnapshot.NONE;
        }

        if (emptyEnchant) {
            snapshot.offer(Keys.ITEM_ENCHANTMENTS, Lists.newArrayList());
            return new NucleusItemStackSnapshot(snapshot.createSnapshot());
        }

        if (snapshot.get(Keys.ITEM_ENCHANTMENTS).isPresent()) {
            // Reset the data.
            snapshot.offer(Keys.ITEM_ENCHANTMENTS, snapshot.get(Keys.ITEM_ENCHANTMENTS).get());
            return new NucleusItemStackSnapshot(snapshot.createSnapshot());
        }

        return new NucleusItemStackSnapshot(snapshot.createSnapshot());
    }

    @Override
    public void serialize(TypeToken<?> type, NucleusItemStackSnapshot obj, ConfigurationNode value) {
        DataContainer view = obj.getSnapshot().toContainer();
        Map<DataQuery, Object> dataQueryObjectMap = view.getValues(true);
        for (Map.Entry<DataQuery, Object> entry : dataQueryObjectMap.entrySet()) {
            if (entry.getValue().getClass().isArray()) {
                // Convert to a list with type, make it the key.
                if (entry.getValue().getClass().getComponentType().isPrimitive()) {
                    // Create the list of the primitive type.
                    DataQuery old = entry.getKey();
                    Tuple<DataQuery, List<?>> dqo = TypeHelper.getList(old, entry.getValue());
                    view.remove(old);
                    view.set(dqo.getFirst(), dqo.getSecond());
                } else {
                    // create a list type
                    view.set(entry.getKey(), Lists.newArrayList((Object[]) entry.getValue()));
                }
            }
        }

        value.setValue(DataTranslators.CONFIGURATION_NODE.translate(view));
    }

}

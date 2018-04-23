/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal;

import com.google.common.collect.ImmutableMap;
import io.github.nucleuspowered.nucleus.Nucleus;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A small utility class that does the following:
 *
 * <ul>
 *     <li>
 *         Gets all keys from the {@link org.spongepowered.api.data.key.Keys} class.
 *     </li>
 *     <li>
 *         Takes the field createName, and converts it into a readable createName.
 *     </li>
 *     <li>
 *         Puts the actual key into the map.
 *     </li>
 * </ul>
 *
 * <p>
 *     This is a lazy loaded singleton that should not really be accessed until required, that way, we get all the
 *     keys we need. This is why it is not injected.
 * </p>
 */
public final class DataScanner {

    private static DataScanner instance = null;
    private static final NumberFormat nf = new DecimalFormat("0.000");

    public static DataScanner getInstance() {
        if (instance == null) {
            instance = new DataScanner();
        }

        return instance;
    }

    private final Map<String, Key<? extends BaseValue<?>>> reportableKeys;

    private DataScanner() {
        this.reportableKeys = new HashMap<>();

        // Start Key Scanning

        // From the Keys class, get all fields
        Field[] f = Keys.class.getFields();
        for (Field fi : f) {
            try {
                Object o = fi.get(null);
                if ((o instanceof Key)) {
                    this.reportableKeys.put(createName(fi.getName()), (Key<? extends BaseValue<?>>) o);
                }
            } catch (IllegalAccessException e) {
                // Don't know, don't care.
            }
        }

        // End Key Scanning.
    }

    private String createName(String name) {
        String[] n = name.split("_");
        for (int i = 0; i < n.length ; i++) {
            n[i] = n[i].substring(0, 1).toUpperCase() + n[i].substring(1).toLowerCase();
        }

        return String.join(" ", (CharSequence[]) n);
    }

    public ImmutableMap<String, Key<? extends BaseValue<?>>> getKeysForHolder(DataHolder holder) {
        try {
            Set<ImmutableValue<?>> siv = holder.getValues();
            Map<String, Key<? extends BaseValue<?>>> m = siv.stream().collect(Collectors.toMap(x -> {
                List<String> ld = x.getKey().getQuery().getParts();
                return ld.get(ld.size() - 1);
            }, BaseValue::getKey));

            return ImmutableMap.copyOf(m);
        } catch (AbstractMethodError e) {
            return getKeys();
        }
    }

    private ImmutableMap<String, Key<? extends BaseValue<?>>> getKeys() {
        return ImmutableMap.copyOf(this.reportableKeys);
    }

    public static Optional<Text> getText(CommandSource src, String translationKey, String key, Object x) {
        // src - for any translation that may be required later.
        String v = String.valueOf(x);
        Class<?> c = x.getClass();
        if (!v.equals(String.format("%s@%s", c.getName(), Integer.toHexString(x.hashCode())))) {
            if (x instanceof Double || x instanceof Float || x instanceof BigDecimal) {
                return Optional.of(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat(translationKey, key, nf.format(x)));
            }

            if (x instanceof Text) {
                return Optional.of(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat(translationKey, key, TextSerializers.FORMATTING_CODE.serialize((Text)x)));
            }

            return Optional.of(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat(translationKey, key, v));
        }

        return Optional.empty();
    }
}

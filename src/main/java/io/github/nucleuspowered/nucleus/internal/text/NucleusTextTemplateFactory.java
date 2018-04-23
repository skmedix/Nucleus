/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.text;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.util.Tuple;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

public class NucleusTextTemplateFactory {

    public static final NucleusTextTemplateFactory INSTANCE = new NucleusTextTemplateFactory();

    public static NucleusTextTemplateImpl createFromString(String string) throws Throwable {
        return INSTANCE.create(string);
    }

    public static NucleusTextTemplateImpl createFromAmpersandString(String string) {
        return new NucleusTextTemplateImpl.Ampersand(string);
    }

    private final Set<Tuple<String, String>> registered = Sets.newHashSet();
    private final List<Function<String, String>> replacements = Lists.newArrayList();

    private NucleusTextTemplateFactory() {}

    boolean registerTokenTranslator(String tokenStart, String tokenEnd, String replacement) {
        String s = tokenStart.trim();
        String e = tokenEnd.trim();
        Preconditions.checkArgument(!(s.contains("{{") || e.contains("}}")));
        if (this.registered.stream().anyMatch(x -> x.getFirst().equalsIgnoreCase(s) || x.getSecond().equalsIgnoreCase(e))) {
            return false;
        }

        // Create replacement regex.
        String replacementRegex = Pattern.quote(tokenStart.trim()) + "([^\\s{}]+)" + Pattern.quote(tokenEnd.trim());
        this.replacements.add(st -> st.replaceAll(replacementRegex, "{{" + replacement + "}}"));
        this.registered.add(Tuple.of(s, e));
        return true;
    }

    public NucleusTextTemplateImpl create(String string) throws Throwable {
        if (string.isEmpty()) {
            return NucleusTextTemplateImpl.Empty.INSTANCE;
        }

        try {
            return new NucleusTextTemplateImpl.Json(string);
        } catch (NullPointerException e) {
            return createFromAmpersand(string);
        } catch (RuntimeException e) {
            if (e.getCause() != null && e.getCause() instanceof ObjectMappingException) {
                return createFromAmpersand(string);
            } else if (e.getCause() != null) {
                throw e.getCause();
            } else {
                throw e;
            }
        }
    }

    public NucleusTextTemplateImpl createFromAmpersand(String string) {
        return new NucleusTextTemplateImpl.Ampersand(string);
    }

    String performReplacements(String string) {
        for (Function<String, String> replacementFunction : this.replacements) {
            string = replacementFunction.apply(string);
        }

        return string;
    }
}

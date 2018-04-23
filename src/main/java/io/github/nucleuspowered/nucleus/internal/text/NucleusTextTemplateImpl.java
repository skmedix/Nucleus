/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.text;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.text.NucleusTextTemplate;
import io.github.nucleuspowered.nucleus.util.Tuples;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextRepresentable;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

@NonnullByDefault
public abstract class NucleusTextTemplateImpl implements NucleusTextTemplate {

    private static final Map<String, Object> emptyVariables = Maps.newHashMap();

    private final String representation;
    private final TextTemplate textTemplate;
    private final Map<String, Function<CommandSource, Text>> tokenMap = Maps.newHashMap();

    public NucleusTextTemplateImpl(String representation) {
        this.representation = representation;
        Tuple<TextTemplate, Map<String, Function<CommandSource, Text>>> t = parse(representation);
        this.textTemplate = t.getFirst();

        this.tokenMap.putAll(t.getSecond());
    }

    @Override public boolean isEmpty() {
        return false;
    }

    public String getRepresentation() {
        return this.representation;
    }

    @Override public TextTemplate getTextTemplate() {
        return this.textTemplate;
    }

    abstract Tuple<TextTemplate, Map<String, Function<CommandSource, Text>>> parse(String parser);

    @Override public boolean containsTokens() {
        return !this.textTemplate.getArguments().isEmpty();
    }

    @Override @SuppressWarnings("SameParameterValue")
    public Text getForCommandSource(CommandSource source, @Nullable Map<String, Function<CommandSource, Optional<Text>>> tokensArray,
            @Nullable Map<String, Object> variables) {
        final Map<String, Object> variables2 = variables == null ? emptyVariables : variables;

        Map<String, TextTemplate.Arg> tokens = this.textTemplate.getArguments();
        Map<String, Text> finalArgs = Maps.newHashMap();

        tokens.forEach((k, v) -> {
            String key = k.toLowerCase();

            Text t;
            if (this.tokenMap.containsKey(key)) {
                t = this.tokenMap.get(key).apply(source);
            } else if (tokensArray != null && tokensArray.containsKey(key)) {
                t = tokensArray.get(key).apply(source).orElse(null);
            } else {
                t = Nucleus.getNucleus().getMessageTokenService().parseToken(key, source, variables2).orElse(null);
            }

            if (t != null) {
                finalArgs.put(k, t);
            }
        });

        return this.textTemplate.apply(finalArgs).build();
    }

    public Text toText() {
        return this.textTemplate.toText();
    }

    /**
     * Creates a {@link TextTemplate} from an Ampersand encoded string.
     */
    static class Ampersand extends NucleusTextTemplateImpl {

        private static final Pattern pattern =
            Pattern.compile("(?<url>\\[[^\\[]+]\\(/[^)]*?)?(?<match>\\{\\{(?!subject)(?<name>[^\\s{}]+)}})"
                    + "(?<urltwo>[^(]*?\\))?");

        Ampersand(String representation) {
            super(representation);
        }

        @Override Tuple<TextTemplate, Map<String, Function<CommandSource, Text>>> parse(String input) {
            // regex!
            final String string = NucleusTextTemplateFactory.INSTANCE.performReplacements(input);
            Matcher mat = pattern.matcher(string);
            List<String> map = Lists.newArrayList();

            List<String> s = Lists.newArrayList(pattern.split(string));
            int index = 0;

            while (mat.find()) {
                if (mat.group("url") != null && mat.group("url2") != null) {
                    String toUpdate = s.get(index);
                    toUpdate = toUpdate + mat.group();
                    if (s.size() < index + 1) {
                        toUpdate += s.get(index + 1);
                        s.remove(index + 1);
                        s.set(index, toUpdate);
                    }
                } else {
                    String out = mat.group("url");
                    if (out != null) {
                        if (s.isEmpty()) {
                            s.add(out);
                        } else {
                            s.set(index, s.get(index) + out);
                        }
                    }

                    index++;
                    out = mat.group("urltwo");
                    if (out != null) {
                        if (s.size() <= index) {
                            s.add(out);
                        } else {
                            s.set(index, out + s.get(index));
                        }
                    }

                    map.add(mat.group("name").toLowerCase());
                }
            }


            // Generic hell.
            ArrayDeque<TextRepresentable> texts = new ArrayDeque<>();
            Map<String, Function<CommandSource, Text>> tokens = Maps.newHashMap();

            // TextParsingUtils URL parsing needed here.
            TextParsingUtils cu = Nucleus.getNucleus().getTextParsingUtils();

            // This condition only occurs if you _just_ use the token. Otherwise, you get a part either side - so it's either 0 or 2.
            if (s.size() > 0) {
                cu.createTextTemplateFragmentWithLinks(s.get(0)).mapIfPresent(texts::addAll, tokens::putAll);
            }

            for (int i = 0; i < map.size(); i++) {
                TextTemplate.Arg.Builder arg = TextTemplate.arg(map.get(i)).optional();
                TextRepresentable r = texts.peekLast();
                TextParsingUtils.StyleTuple style = null;
                if (r != null) {
                    // Create the argument
                    style = TextParsingUtils.getLastColourAndStyle(r, null);
                    style.applyTo(st -> arg.color(st.colour).style(st.style));
                }

                texts.add(arg.build());
                if (s.size() > i + 1) {
                    Tuples.NullableTuple<List<TextRepresentable>, Map<String, Function<CommandSource, Text>>> tt =
                        cu.createTextTemplateFragmentWithLinks(s.get(i + 1));
                    if (style != null && tt.getFirst().isPresent()) {
                        texts.push(style.getTextOf());
                    }

                    cu.createTextTemplateFragmentWithLinks(s.get(i + 1)).mapIfPresent(texts::addAll, tokens::putAll);
                }
            }

            return Tuple.of(TextTemplate.of(texts.toArray(new Object[0])), tokens);
        }
    }

    static class Json extends NucleusTextTemplateImpl {

        @Nullable private static TypeSerializer<TextTemplate> textTemplateTypeSerializer = null;

        Json(String representation) {
            super(representation);
        }

        @Override Tuple<TextTemplate, Map<String, Function<CommandSource, Text>>> parse(String parser) {
            if (textTemplateTypeSerializer == null) {
                textTemplateTypeSerializer = ConfigurationOptions.defaults().getSerializers().get(TypeToken.of(TextTemplate.class));
            }

            try {
                return Tuple.of(textTemplateTypeSerializer.deserialize(TypeToken.of(TextTemplate.class), SimpleConfigurationNode.root().setValue(parser)),
                        Maps.newHashMap());
            } catch (ObjectMappingException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class Empty extends NucleusTextTemplateImpl {

        public static final NucleusTextTemplateImpl INSTANCE = new Empty();

        private Empty() {
            super("");
        }

        @Override Tuple<TextTemplate, Map<String, Function<CommandSource, Text>>> parse(String parser) {
            return Tuple.of(TextTemplate.EMPTY, Maps.newHashMap());
        }

        @Override public boolean isEmpty() {
            return true;
        }
    }
}

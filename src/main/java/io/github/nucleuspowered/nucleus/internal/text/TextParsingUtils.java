/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.text;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.modules.core.CoreModule;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfigAdapter;
import io.github.nucleuspowered.nucleus.util.Tuples;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextRepresentable;
import org.spongepowered.api.text.TextTemplate;
import org.spongepowered.api.text.action.HoverAction;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyle;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

public class TextParsingUtils {

    private final NucleusPlugin plugin;

    private static final Pattern colours = Pattern.compile(".*?(?<colour>(&[0-9a-flmnrok])+)$");

    private final Pattern enhancedUrlParser =
            Pattern.compile("(?<first>(^|\\s))(?<reset>&r)?(?<colour>(&[0-9a-flmnrok])+)?"
                + "((?<options>\\{[a-z]+?})?(?<url>(http(s)?://)?([A-Za-z0-9]+\\.)+[A-Za-z0-9-]{2,}\\S*)|"
                + "(?<specialUrl>(\\[(?<msg>.+?)](?<optionssurl>\\{[a-z]+})?\\((?<sUrl>(http(s)?://)?([A-Za-z0-9-]+\\.)+[A-Za-z0-9]{2,}[^\\s)]*)\\)))|"
                + "(?<specialCmd>(\\[(?<sMsg>.+?)](?<optionsscmd>\\{[a-z]+})?\\((?<sCmd>/.+?)\\))))",
                Pattern.CASE_INSENSITIVE);

    private CoreConfigAdapter cca = null;

    private static final Pattern urlParser =
            Pattern.compile("(?<first>(^|\\s))(?<reset>&r)?(?<colour>(&[0-9a-flmnrok])+)?"
                            + "(?<options>\\{[a-z]+?})?(?<url>(http(s)?://)?([A-Za-z0-9-]+\\.)+[A-Za-z0-9]{2,}\\S*)",
                    Pattern.CASE_INSENSITIVE);
    private static final StyleTuple EMPTY = new StyleTuple(TextColors.NONE, TextStyles.NONE);

    public TextParsingUtils(NucleusPlugin plugin) {
        this.plugin = plugin;
    }

    public static Text addUrls(String message) {
        if (message == null || message.isEmpty()) {
            return Text.EMPTY;
        }

        Matcher m = urlParser.matcher(message);
        if (!m.find()) {
            return TextSerializers.FORMATTING_CODE.deserialize(message);
        }

        List<Text> texts = Lists.newArrayList();
        String remaining = message;
        TextParsingUtils.StyleTuple st = TextParsingUtils.EMPTY;
        do {
            // We found a URL. We split on the URL that we have.
            String[] textArray = remaining.split(urlParser.pattern(), 2);
            Text first = Text.builder().color(st.colour).style(st.style)
                    .append(TextSerializers.FORMATTING_CODE.deserialize(textArray[0])).build();

            // Add this text to the list regardless.
            texts.add(first);

            // If we have more to do, shove it into the "remaining" variable.
            if (textArray.length == 2) {
                remaining = textArray[1];
            } else {
                remaining = null;
            }

            // Get the last colour & styles
            String colourMatch = m.group("colour");
            if (colourMatch != null && !colourMatch.isEmpty()) {

                // If there is a reset, explicitly do it.
                TextStyle reset = TextStyles.NONE;
                if (m.group("reset") != null) {
                    reset = TextStyles.RESET;
                }

                first = Text.of(reset, TextSerializers.FORMATTING_CODE.deserialize(m.group("colour") + " "));
            }

            st = TextParsingUtils.getLastColourAndStyle(first, st);

            // Build the URL
            String whiteSpace = m.group("first");
            String url = m.group("url");
            texts.add(TextParsingUtils.getTextForUrl(url, url, whiteSpace, st, m.group("options")));
        } while (remaining != null && m.find());

        // Add the last bit.
        if (remaining != null) {
            texts.add(Text.builder().color(st.colour).style(st.style)
                    .append(TextSerializers.FORMATTING_CODE.deserialize(remaining)).build());
        }

        // Join it all together.
        return Text.join(texts);
    }

    public static Text oldLegacy(String message) {
        Matcher colourMatcher = colours.matcher(message);
        if (colourMatcher.matches()) {
            Text first = TextSerializers.FORMATTING_CODE.deserialize(message.replace(colourMatcher.group("colour"), ""));
            String match = colourMatcher.group("colour") + " ";
            Text t = TextSerializers.FORMATTING_CODE.deserialize(match);
            return Text.of(first, t.getColor(), first.getStyle().and(t.getStyle()));
        }

        return TextSerializers.FORMATTING_CODE.deserialize(message);
    }

    public Tuples.NullableTuple<List<TextRepresentable>, Map<String, Function<CommandSource, Text>>> createTextTemplateFragmentWithLinks(String message) {
        Preconditions.checkNotNull(message, "message");
        if (message.isEmpty()) {
            return new Tuples.NullableTuple<>(Lists.newArrayList(Text.EMPTY), null);
        }

        Matcher m = this.enhancedUrlParser.matcher(message);
        if (!m.find()) {
            return new Tuples.NullableTuple<>(Lists.newArrayList(oldLegacy(message)), null);
        }

        Map<String, Function<CommandSource, Text>> args = Maps.newHashMap();
        List<TextRepresentable> texts = Lists.newArrayList();
        String remaining = message;
        StyleTuple st = TextParsingUtils.EMPTY;
        do {
            // We found a URL. We split on the URL that we have.
            String[] textArray = remaining.split(this.enhancedUrlParser.pattern(), 2);
            TextRepresentable first = Text.builder().color(st.colour).style(st.style)
                    .append(oldLegacy(textArray[0])).build();

            // Add this text to the list regardless.
            texts.add(first);

            // If we have more to do, shove it into the "remaining" variable.
            if (textArray.length == 2) {
                remaining = textArray[1];
            } else {
                remaining = null;
            }

            // Get the last colour & styles
            String colourMatch = m.group("colour");
            if (colourMatch != null && !colourMatch.isEmpty()) {

                // If there is a reset, explicitly do it.
                TextStyle reset = TextStyles.NONE;
                if (m.group("reset") != null) {
                    reset = TextStyles.RESET;
                }

                first = Text.of(reset, oldLegacy(m.group("colour")));
            }

            st = getLastColourAndStyle(first, st);

            // Build the URL
            String whiteSpace = m.group("first");
            if (m.group("url") != null) {
                String url = m.group("url");
                texts.add(getTextForUrl(url, url, whiteSpace, st, m.group("options")));
            } else if (m.group("specialUrl") != null) {
                String url = m.group("sUrl");
                String msg = m.group("msg");
                texts.add(getTextForUrl(url, msg, whiteSpace, st, m.group("optionssurl")));
            } else {
                // Must be commands.
                String cmd = m.group("sCmd");
                String msg = m.group("sMsg");
                String optionList = m.group("optionsscmd");

                if (cmd.contains("{{subject}}")) {
                    String arg = UUID.randomUUID().toString();
                    args.put(arg, cs -> {
                        String command = cmd.replace("{{subject}}", cs.getName());
                        return getCmd(msg, command, optionList, whiteSpace);
                    });

                    texts.add(TextTemplate.arg(arg).color(st.colour).style(st.style).build());
                } else {
                    texts.add(Text.of(st.colour, st.style, getCmd(msg, cmd, optionList, whiteSpace)));
                }
            }
        } while (remaining != null && m.find());

        // Add the last bit.
        if (remaining != null) {
            Text.Builder tb = Text.builder().color(st.colour).style(st.style).append(TextSerializers.FORMATTING_CODE.deserialize(remaining));
            if (remaining.matches("^\\s+&r.*")) {
                tb.style(TextStyles.RESET);
            }

            texts.add(tb.build());
        }

        // Return the list.
        return new Tuples.NullableTuple<>(texts, args);
    }

    private Text getCmd(String msg, String cmd, String optionList, String whiteSpace) {
        Text.Builder textBuilder = Text.builder(msg)
                .onClick(TextActions.runCommand(cmd))
                .onHover(setupHoverOnCmd(cmd, optionList));
        if (optionList != null && optionList.contains("s")) {
            textBuilder.onClick(TextActions.suggestCommand(cmd));
        }

        Text toAdd = textBuilder.build();
        if (!whiteSpace.isEmpty()) {
            toAdd = Text.join(Text.of(whiteSpace), toAdd);
        }

        return toAdd;
    }

    @Nullable
    private HoverAction<?> setupHoverOnCmd(String cmd, @Nullable String optionList) {
        if (optionList != null) {
            if (optionList.contains("h")) {
                return null;
            }

            if (optionList.contains("s")) {
                return TextActions.showText(
                        this.plugin.getMessageProvider().getTextMessageWithFormat("chat.command.clicksuggest", cmd)
                );
            }
        }

        return TextActions.showText(this.plugin.getMessageProvider().getTextMessageWithFormat("chat.command.click", cmd));
    }

    private static Text getTextForUrl(String url, String msg, String whiteSpace, StyleTuple st, @Nullable String optionString) {
        String toParse = TextSerializers.FORMATTING_CODE.stripCodes(url);
        Nucleus plugin = Nucleus.getNucleus();

        try {
            URL urlObj;
            if (!toParse.startsWith("http://") && !toParse.startsWith("https://")) {
                urlObj = new URL("http://" + toParse);
            } else {
                urlObj = new URL(toParse);
            }

            Text.Builder textBuilder = Text.builder(msg).color(st.colour).style(st.style).onClick(TextActions.openUrl(urlObj));
            if (optionString == null || !optionString.contains("h")) {
                textBuilder.onHover(TextActions.showText(plugin.getMessageProvider().getTextMessageWithFormat("chat.url.click", url)));
            }

            if (!whiteSpace.isEmpty()) {
                return Text.builder(whiteSpace).append(textBuilder.build()).build();
            }

            return textBuilder.build();
        } catch (MalformedURLException e) {
            // URL parsing failed, just put the original text in here.
            plugin.getLogger().warn(plugin.getMessageProvider().getMessageWithFormat("chat.url.malformed", url));
            if (Nucleus.getNucleus().isDebugMode()) {
                e.printStackTrace();
            }

            Text ret = Text.builder(url).color(st.colour).style(st.style).build();
            if (!whiteSpace.isEmpty()) {
                return Text.builder(whiteSpace).append(ret).build();
            }

            return ret;
        }
    }

    public static Text joinTextsWithColoursFlowing(Text... texts) {
        return joinTextsWithColoursFlowing("", Arrays.asList(texts));
    }

    private static Text joinTextsWithColoursFlowing(String joining, Iterable<Text> texts) {
        List<Text> result = Lists.newArrayList();
        Iterator<Text> t = texts.iterator();
        Text last = null;
        while (t.hasNext()) {
            Text n = t.next();
            if (last != null) {
                getLastColourAndStyle(last, null).applyTo(x -> result.add(Text.of(x.colour, x.style, joining, n)));
            } else {
                result.add(n);
            }

            last = n;
        }

        return Text.join(result);
    }

    public static StyleTuple getLastColourAndStyle(TextRepresentable text, @Nullable StyleTuple current) {
        List<Text> texts = flatten(text.toText());
        if (texts.isEmpty()) {
            return current == null ? new StyleTuple(TextColors.NONE, TextStyles.NONE) : current;
        }

        TextColor tc = TextColors.NONE;
        TextStyle ts =  texts.get(texts.size() - 1).getStyle();

        for (int i = texts.size() - 1; i > -1; i--) {
            // If we have both a Text Colour and a Text Style, then break out.
            if (tc == TextColors.NONE) {
                tc = texts.get(i).getColor();
                break;
            }
        }

        if (current == null) {
            return new StyleTuple(tc, ts);
        }

        return new StyleTuple(tc != TextColors.NONE ? tc : current.colour, ts);
    }

    private static List<Text> flatten(Text text) {
        List<Text> texts = Lists.newArrayList(text);
        if (!text.getChildren().isEmpty()) {
            text.getChildren().forEach(x -> texts.addAll(flatten(x)));
        }

        return texts;
    }

    public Text addCommandToName(CommandSource p) {
        Text.Builder text = Text.builder(p.getName());
        if (p instanceof User) {
            return addCommandToNameInternal(text, (User)p);
        }

        return text.build();
    }

    public Text addCommandToDisplayName(CommandSource p) {
        Text name = getName(p);
        if (p instanceof User) {
            return addCommandToNameInternal(name, (User)p);
        }

        return name;
    }

    private Text addCommandToNameInternal(Text name, User user) {
        return addCommandToNameInternal(name.toBuilder(), user);
    }

    private Text addCommandToNameInternal(Text.Builder name, User user) {
        initCoreConfigAdapter();
        String cmd = this.cca.getNodeOrDefault().getCommandOnNameClick();
        if (cmd == null || cmd.isEmpty()) {
            return name.build();
        }

        if (!cmd.startsWith("/")) {
            cmd = "/" + cmd;
        }

        if (!cmd.endsWith(" ")) {
            cmd = cmd + " ";
        }

        final String commandToRun = cmd.replace("{{subject}}", user.getName()).replace("{{player}}", user.getName());
        Optional<HoverAction<?>> ha = name.getHoverAction();
        Text.Builder hoverAction;
        if (ha.isPresent() && (ha.get() instanceof HoverAction.ShowText)) {
            HoverAction.ShowText h = (HoverAction.ShowText)ha.get();
            hoverAction = h.getResult().toBuilder();
            hoverAction.append(Text.NEW_LINE);
        } else {
            hoverAction = Text.builder();
        }

        hoverAction.append(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("name.hover.command", commandToRun));
        return name.onClick(TextActions.suggestCommand(commandToRun)).onHover(TextActions.showText(hoverAction.toText())).build();
    }

    private Text getName(CommandSource cs) {
        if (cs instanceof Player) {
            return this.plugin.getNameUtil().getName((Player)cs);
        }

        return Text.of(cs.getName());
    }

    private void initCoreConfigAdapter() {
        if (this.cca == null) {
            this.cca = this.plugin.getConfigAdapter(CoreModule.ID, CoreConfigAdapter.class).get();
        }
    }

    @NonnullByDefault
    public static final class StyleTuple {
        public final TextColor colour;
        public final TextStyle style;

        StyleTuple(TextColor colour, TextStyle style) {
            this.colour = colour;
            this.style = style;
        }

        public void applyTo(Consumer<StyleTuple> consumer) {
            consumer.accept(this);
        }

        public Text getTextOf() {
            Text.Builder tb = Text.builder();
            if (this.colour != TextColors.NONE) {
                tb.color(this.colour);
            }

            tb.style(this.style);
            return tb.toText();
        }
    }
}

/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.argumentparsers;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.argumentparsers.util.WrappedElement;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.selector.Selector;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.List;
import java.util.function.BiPredicate;

import javax.annotation.Nullable;

@NonnullByDefault
public class SelectorWrapperArgument<T extends Entity> extends WrappedElement {

    private final Class<T> entityFilter;

    public SelectorWrapperArgument(CommandElement wrappedElement, final Class<T> selectorFilter) {
        super(wrappedElement);
        this.entityFilter = selectorFilter;
    }

    public static SelectorWrapperArgument<Player> nicknameSelector(@Nullable Text key, NicknameArgument.UnderlyingType<?> type) {
        return nicknameSelector(key, type, true, Player.class, (c, s) -> true);
    }

    public static <S extends Entity> SelectorWrapperArgument<S> nicknameSelector(@Nullable Text key, NicknameArgument.UnderlyingType<?> type,
            boolean onlyOne, Class<S> selectorFilter) {
        return nicknameSelector(key, type, onlyOne, selectorFilter, (c, s) -> true);
    }

    public static <S extends Entity, T extends User> SelectorWrapperArgument<S> nicknameSelector(@Nullable Text key,
            NicknameArgument.UnderlyingType<T> type, boolean onlyOne, Class<S> selectorFilter, BiPredicate<CommandSource, T> filter) {
        return new SelectorWrapperArgument<>(new NicknameArgument<>(key, type, onlyOne, filter), selectorFilter);
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void parse(CommandSource source, CommandArgs args, CommandContext context) throws ArgumentParseException {
        String a = args.peek();
        if (a.startsWith("@")) {
            // Time to try to eek it all out.
            Selector.parse(a).resolve(source).stream().filter(this.entityFilter::isInstance)
                    .forEach(x ->
                            context.putArg(getKey(), x)
                    );

            args.next();

            if (context.hasAny(getKey())) {
                return;
            }

            throw args.createError(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("args.selector.notarget"));
        }

        getWrappedElement().parse(source, args, context);
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        return getWrappedElement().complete(src, args, context);
    }

    @Override
    public Text getUsage(CommandSource src) {
        return getWrappedElement().getUsage(src);
    }
}

/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.argumentparsers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.modules.nickname.services.NicknameService;
import io.github.nucleuspowered.nucleus.util.QuadFunction;
import io.github.nucleuspowered.nucleus.util.ThrownTriFunction;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

@NonnullByDefault
public class  NicknameArgument<T extends User> extends CommandElement {

    private final ThrownTriFunction<String, CommandSource, CommandArgs, List<?>, ArgumentParseException> parser;
    private final QuadFunction<String, CommandSource, CommandArgs, CommandContext, List<String>> completer;
    private final boolean onlyOne;
    private final UnderlyingType type;
    private final BiPredicate<CommandSource, T> filter;
    @Nullable private final NicknameService nicknameService = Nucleus.getNucleus().getInternalServiceManager().getService(NicknameService.class)
            .orElse(null);

    public NicknameArgument(@Nullable Text key, UnderlyingType<T> type) {
        this(key, type, true);
    }

    public NicknameArgument(@Nullable Text key, UnderlyingType<T> type, boolean onlyOne) {
        this(key, type, onlyOne, (s, c) -> true);
    }

    @SuppressWarnings("unchecked")
    public NicknameArgument(@Nullable Text key, UnderlyingType<T> type, boolean onlyOne,
            BiPredicate<CommandSource, T> filter) {
        super(key);

        this.onlyOne = onlyOne;
        this.type = type;
        this.filter = filter;

        PlayerConsoleArgument pca = new PlayerConsoleArgument(key, type == UnderlyingType.PLAYER_CONSOLE,
                (BiPredicate<CommandSource, Player>)filter);

        if (type == UnderlyingType.USER) {
            UserParser p = new UserParser(onlyOne);
            this.parser = (name, cs, a) -> {
                List<?> i = p.accept(name, cs, a);
                if (i.isEmpty()) {
                    i = pca.parseInternal(name, cs, a);
                }

                return i;
            };

            this.completer = (s, cs, a, c) -> {
                List<String> toReturn = pca.completeInternal(s, cs, a, c);

                if (!s.isEmpty()) {
                    UserStorageService uss = Sponge.getServiceManager().provideUnchecked(UserStorageService.class);
                    List<String> offline = Sponge.getServiceManager().provideUnchecked(UserStorageService.class)
                            .match(s)
                            .stream()
                            .filter(x -> !Sponge.getServer().getPlayer(x.getUniqueId()).isPresent())
                            .filter(x -> uss.get(x).map(y -> filter.test(cs, (T) y)).orElse(false))
                            .filter(x -> PlayerConsoleArgument.shouldShow(x.getUniqueId(), cs))
                            .map(x -> x.getName().get())
                            .limit(20)
                            .collect(Collectors.toList());

                    toReturn.addAll(offline);
                }

                return toReturn;
            };
        } else {
            this.parser = pca::parseInternal;
            this.completer = pca::completeInternal;
        }
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        String name = args.next().toLowerCase();
        return parseInternal(name, source, args);
    }

    @SuppressWarnings("unchecked")
    List<?> parseInternal(String name, CommandSource src, CommandArgs args) throws ArgumentParseException {
        boolean playerOnly = name.startsWith("p:");

        final String fName;
        if (playerOnly) {
            fName = name.split(":", 2)[1];
        } else {
            fName = name;
        }

        List<?> obj = null;
        try {
            obj = this.parser.accept(fName, src, args);
        } catch (ArgumentParseException ex) {
            // ignored
        }

        if (obj != null && !obj.isEmpty()) {
            return obj;
        } else if (playerOnly) {
            // Rethrow;
            throw args.createError(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("args.user.nouser", fName));
        }

        // Now check user names
        // TODO: Display name
        Map<String, UUID> allPlayers;
        if (this.nicknameService != null) {
            Optional<Player> op = this.nicknameService.getFromCache(fName.toLowerCase());
            if (op.isPresent()) {
                return Lists.newArrayList(op.get());
            }
            allPlayers = this.nicknameService.getAllCached();
        } else {
            allPlayers = Maps.newHashMap();
        }

        List<Player> players = allPlayers.entrySet().stream()
            .filter(x -> x.getKey().toLowerCase().startsWith(fName))
            .sorted(Comparator.comparing(Map.Entry::getKey))
            .flatMap(x -> Sponge.getServer().getPlayer(x.getValue()).map(Stream::of).orElseGet(Stream::empty))
            .filter(x -> this.filter.test(src, (T)x))
            .collect(Collectors.toList());

        if (players.isEmpty()) {
            throw args.createError(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat(
                    this.type == UnderlyingType.PLAYER_CONSOLE ? "args.playerconsole.nouser" : "args.user.nouser", fName));
        } else if (players.size() > 1 && this.onlyOne) {
            throw args.createError(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("args.user.toomany", fName));
        }

        // We know they are online.
        return players;
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        String name;
        try {
            name = args.peek().toLowerCase();
        } catch (ArgumentParseException e) {
            name = "";
        }

        boolean playerOnly = name.startsWith("p:");
        final String fName;
        if (playerOnly) {
            fName = name.split(":", 2)[1];
        } else {
            fName = name;
        }

        Set<String> original = Sets.newHashSet(this.completer.accept(fName, src, args, context));
        if (playerOnly) {
            return original.stream().map(x -> "p:" + x).collect(Collectors.toList());
        } else if (this.nicknameService != null) {
                this.nicknameService
                    .startsWithGetMap(fName.toLowerCase())
                    .entrySet()
                    .stream()
                    .flatMap(x -> Sponge.getServer().getPlayer(x.getValue()).map(y -> Stream.of(x.getKey())).orElseGet(Stream::empty))
                    .forEach(original::add);
        }

        return Lists.newArrayList(original);
    }

    public static class UnderlyingType<U extends User> {
        public static final UnderlyingType<Player> PLAYER = new UnderlyingType<>();
        public static final UnderlyingType<Player> PLAYER_CONSOLE = new UnderlyingType<>();
        public static final UnderlyingType<User> USER = new UnderlyingType<>();
    }

    public static final class UserParser implements ThrownTriFunction<String, CommandSource, CommandArgs, List<?>, ArgumentParseException> {

        private final boolean onlyOne;
        private final BiPredicate<CommandSource, User> filter;

        public UserParser(boolean onlyOne) {
            this(onlyOne, (c, s) -> true);
        }

        UserParser(boolean onlyOne, BiPredicate<CommandSource, User> filter) {
            this.onlyOne = onlyOne;
            this.filter = filter;
        }

        @Override
        public List<?> accept(String s, CommandSource cs, CommandArgs a) throws ArgumentParseException {
            UserStorageService uss = Sponge.getServiceManager().provideUnchecked(UserStorageService.class);
            if (this.onlyOne) {
                try {
                    return Lists.newArrayList(uss.get(s)
                            .orElseThrow(
                                    () -> a.createError(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("args.user.toomany", s))));
                } catch (IllegalArgumentException e) {
                    // ignored
                }
            }

            List<User> users = uss.match(s)
                    .stream()
                    // Get the players who start with the string.
                    .map(uss::get)
                    // Remove players who have no user
                    .flatMap(x -> x.map(Stream::of).orElseGet(Stream::empty))
                    .filter(x -> this.filter.test(cs, x))
                    .filter(x -> PlayerConsoleArgument.shouldShow(x.getUniqueId(), cs))
                    .map(x -> x.getPlayer().map(y -> (User) y).orElse(x))
                    .limit(20) // stop after 20
                    .collect(Collectors.toList());

            if (!users.isEmpty()) {
                List<User> exactUser = users.stream().filter(x -> x.getName().equalsIgnoreCase(s)).collect(Collectors.toList());
                if (exactUser.size() == 1) {
                    return exactUser;
                }

                return users;
            }

            // If users is empty, then we should check online players.
            return Lists.newArrayList();
        }
    }

}

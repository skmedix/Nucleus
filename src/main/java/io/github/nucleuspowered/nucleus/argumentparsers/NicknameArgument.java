/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.argumentparsers;

import com.google.common.collect.ImmutableSet;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.traits.InternalServiceManagerTrait;
import io.github.nucleuspowered.nucleus.internal.traits.MessageProviderTrait;
import io.github.nucleuspowered.nucleus.internal.traits.PermissionTrait;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.nickname.services.NicknameService;
import io.github.nucleuspowered.nucleus.modules.vanish.commands.VanishCommand;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.Identifiable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public class NicknameArgument extends CommandElement implements MessageProviderTrait, InternalServiceManagerTrait, PermissionTrait {

    private static boolean init = false;
    private static int USER_LIMIT = 20;
    private static final String VANISH_PERMISSION = Nucleus.getNucleus().getPermissionRegistry()
            .getPermissionsForNucleusCommand(VanishCommand.class).getPermissionWithSuffix("see");

    public static void onReload() {
        USER_LIMIT =
                Math.max(Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(CoreConfigAdapter.class).getNodeOrDefault()
                        .getNicknameArgOfflineLimit(), 0);
    }

    private final Target target;
    @Nullable private final NicknameService nicknameService = getService(NicknameService.class).orElse(null);

    public NicknameArgument(@Nullable Text key, Target target) {
        super(key);
        this.target = target;
        if (!init) {
            init = true;
            Nucleus.getNucleus().registerReloadable(NicknameArgument::onReload);
        }
    }

    public enum Target {
        PLAYER,
        PLAYER_CONSOLE,
        USER
    }

    @Nullable
    @Override
    public Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        String toParse = args.next();
        return parseValue(source, toParse, (key, entry) -> args.createError(getMessageFor(source.getLocale(), key, entry)));
    }

    public Set<?> parseValue(CommandSource source,
        String toParse, BiFunction<String, String, ArgumentParseException> exceptionSupplier) throws ArgumentParseException {

        if (target == Target.PLAYER_CONSOLE && toParse.equalsIgnoreCase("-")) {
            return ImmutableSet.of(Sponge.getServer().getConsole());
        }

        final Predicate<Player> shouldShow = determinePredicate(source);

        boolean playerOnly = toParse.startsWith("p:");
        if (playerOnly) {
            toParse = toParse.substring(2);
        }

        // Does the player exist?
        Optional<Player> player = Sponge.getServer().getPlayer(toParse);
        if (player.isPresent() && shouldShowPlayer(player.get())) {
            return ImmutableSet.of(player.get()); // exact match.
        }

        if (playerOnly) {
            throw exceptionSupplier.apply("args.user.nouser", toParse);
        }

        // offline users take precedence over nicknames
        UserStorageService uss = Sponge.getServiceManager().provideUnchecked(UserStorageService.class);
        if (this.target == Target.USER) {
            Optional<User> user = uss.get(toParse);
            if (user.isPresent()) {
                return ImmutableSet.of(user.get());
            }
        }

        if (this.nicknameService != null) {
            Optional<Player> op = this.nicknameService.getFromCache(toParse.toLowerCase());
            if (op.isPresent() && shouldShowPlayer(op.get())) {
                return ImmutableSet.of(op.get());
            }
        }

        if (toParse.length() < 3) {
            throw exceptionSupplier.apply("args.user.nouserfuzzy", toParse);
        }

        Set<User> users = new HashSet<>();
        final String parse = toParse.toLowerCase();
        // fuzzy matching time.
        // players that match
        Sponge.getServer().getOnlinePlayers().stream()
                .filter(x -> x.getName().toLowerCase().startsWith(parse))
                .filter(shouldShow)
                .forEach(users::add);
        if (this.nicknameService != null) {
            this.nicknameService.startsWith(parse).stream()
                    .map(x -> Sponge.getServer().getPlayer(x).orElse(null))
                    .filter(shouldShow)
                    .filter(Objects::nonNull)
                    .forEach(users::add);
        }

        List<UUID> uuids = users.stream().map(Identifiable::getUniqueId).collect(Collectors.toList());
        if (this.target == Target.USER) {
            // This may add vanished players, but that's OK because we're showing all users anyway,
            // AND if they were hidden, it could give away that they were vanished.
            uss.match(parse).stream()
                    .map(x -> uss.get(x).orElse(null))
                    .filter(Objects::nonNull)
                    .filter(x -> !uuids.contains(x.getUniqueId()))
                    .forEach(users::add);
        }

        if (users.isEmpty()) {
            throw exceptionSupplier.apply("args.user.nouser", toParse);
        }

        return ImmutableSet.copyOf(users);
    }

    @Override
    public List<String> complete(CommandSource source, CommandArgs args, CommandContext context) {
        List<String> names = new ArrayList<>();
        try {
            String toParse = args.peek();
            final boolean playerOnly = toParse.startsWith("p:");
            if (playerOnly) {
                toParse = toParse.substring(2);
            }

            final Predicate<Player> shouldShow = determinePredicate(source);
            String parse = toParse.toLowerCase();
            UserStorageService uss = Sponge.getServiceManager().provideUnchecked(UserStorageService.class);
            Sponge.getServer().getOnlinePlayers().stream()
                    .filter(x -> x.getName().toLowerCase().startsWith(parse))
                    .filter(shouldShow)
                    .forEach(player -> {
                        if (playerOnly) {
                            names.add("p:" + player.getName());
                        } else {
                            names.add(player.getName());
                        }
                    });

            if (!playerOnly) {
                if (this.nicknameService != null) {
                    this.nicknameService.startsWithGetMap(parse).entrySet().stream()
                            .map(x -> Sponge.getServer().getPlayer(x.getValue())
                                    .filter(shouldShow)
                                    .map(y -> x.getKey()).orElse(null))
                            .filter(Objects::nonNull)
                            .forEach(names::add);
                }

                if (USER_LIMIT > 0 && this.target == Target.USER) {
                    uss.match(parse).stream()
                            .map(x -> uss.get(x).map(User::getName).orElse(null))
                            .filter(Objects::nonNull)
                            .limit(USER_LIMIT)
                            .forEach(names::add);
                }
            }
            return names;
        } catch (ArgumentParseException ex) {
            return names;
        }
    }

    private Predicate<Player> determinePredicate(CommandSource source) {
        if (hasPermission(source, VANISH_PERMISSION)) {
            return p -> true;
        } else {
            return NicknameArgument::shouldShowPlayer;
        }
    }

    private static boolean shouldShowPlayer(Player player) {
        return !player.get(Keys.VANISH).orElse(false);
    }

}

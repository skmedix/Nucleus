/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.argumentparsers;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.Nucleus;
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
import org.spongepowered.api.text.Text;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerConsoleArgument extends CommandElement {

    private final boolean console;
    private final Supplier<Collection<Player>> onlinePlayersSupplier;
    private static final String vanishPermission = Nucleus.getNucleus().getPermissionRegistry().getPermissionsForNucleusCommand(VanishCommand.class).getPermissionWithSuffix("see");
    private final BiPredicate<CommandSource, Player> filter;

    public PlayerConsoleArgument(@Nullable Text key, boolean console) {
        this(key, console, () -> Sponge.getServer().getOnlinePlayers());
    }

    public PlayerConsoleArgument(@Nullable Text key, boolean console, BiPredicate<CommandSource, Player> filter) {
        this(key, console, () -> Sponge.getServer().getOnlinePlayers(), filter);
    }

    // For testing.
    public PlayerConsoleArgument(@Nullable Text key, boolean console, @Nonnull Supplier<Collection<Player>> onlinePlayerSupplier) {
        this(key, console, onlinePlayerSupplier, (c, s) -> true);
    }

    public PlayerConsoleArgument(@Nullable Text key, boolean console, @Nonnull Supplier<Collection<Player>> onlinePlayerSupplier,
            BiPredicate<CommandSource, Player> filter) {
        super(key);
        this.console = console;
        this.onlinePlayersSupplier = onlinePlayerSupplier;
        this.filter = filter;
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        String name = args.next().toLowerCase();
        return parseInternal(name, source, args);
    }

    public List<CommandSource> parseInternal(String name, CommandSource src, CommandArgs args) throws ArgumentParseException {
        if (this.console && name.equals("-")) {
            return Lists.newArrayList(Sponge.getServer().getConsole());
        }

        List<CommandSource> players = this.onlinePlayersSupplier.get().stream()
                .filter(x -> x.getName().toLowerCase().startsWith(name))
                .filter(x -> this.filter.test(src, x))
                .sorted(Comparator.comparing(User::getName)).collect(Collectors.toList());
        if (players.isEmpty()) {
            throw args.createError(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("args.playerconsole.noexist"));
        }

        List<CommandSource> exactUser = players.stream().filter(x -> x.getName().equalsIgnoreCase(name))
                .collect(Collectors.toList());
        if (exactUser.size() == 1) {
            return exactUser;
        }

        return players;
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        try {
            return completeInternal(args.peek().toLowerCase(), src, args, context);
        } catch (ArgumentParseException e) {
            return completeInternal("", src, args, context);
        }
    }

    List<String> completeInternal(final String name, CommandSource src, CommandArgs args, CommandContext context) {
        List<String> list = Sponge.getServer().getOnlinePlayers().stream()
            .filter(x -> PlayerConsoleArgument.shouldShow(x, src))
            .filter(x -> this.filter.test(src, x))
            .map(User::getName).collect(Collectors.toList());
        // Console.
        if (this.console) {
            list.add("-");
        }

        return list.stream()
            .filter(x -> x.toLowerCase().startsWith(name.toLowerCase())).collect(Collectors.toList());
    }

    static boolean shouldShow(UUID player, CommandSource cs) {
        if (!cs.hasPermission(vanishPermission)) {
            Optional<Player> player1 = Sponge.getServer().getPlayer(player);
            if (player1.isPresent()) {
                return !player1.get().get(Keys.VANISH).orElse(false);
            }
        }

        return true;
    }

    public static boolean shouldShow(Player player, CommandSource cs) {
        return cs.hasPermission(vanishPermission) || !player.get(Keys.VANISH).orElse(false);
    }
}

/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.argumentparsers;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.Nucleus;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import javax.annotation.Nullable;

public class UUIDArgument<T> extends CommandElement {

    @Nullable private final Function<UUID, Optional<T>> validator;

    public static UUIDArgument<GameProfile> gameProfile(Text key) {
        return new UUIDArgument<>(key, x -> Sponge.getServiceManager().provideUnchecked(UserStorageService.class).getAll()
                .stream().filter(y -> y.getUniqueId().equals(x)).findFirst());
    }

    public static UUIDArgument<User> user(Text key) {
        return new UUIDArgument<>(key, x -> Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(x));
    }

    public static UUIDArgument<Player> player(Text key) {
        return new UUIDArgument<>(key, x -> Sponge.getServer().getPlayer(x));
    }

    public UUIDArgument(@Nullable Text key, @Nullable Function<UUID, Optional<T>> validator) {
        super(key);
        this.validator = validator;
    }

    @Nullable @Override protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        String a = args.next().toLowerCase();
        try {
            if (!a.contains("-") && a.matches("[0-9a-f]{32}")) {
                a = String.format("%s-%s-%s-%s-%s", a.substring(0, 8), a.substring(8, 12), a.substring(12, 16), a.substring(16, 20), a.substring(20));
            }

            UUID uuid = UUID.fromString(a);
            if (this.validator != null) {
                return this.validator.apply(uuid).orElseThrow(() ->
                    args.createError(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("args.uuid.notvalid.nomatch")));
            }

            return uuid;
        } catch (IllegalArgumentException e) {
            throw args.createError(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("args.uuid.notvalid.malformed"));
        }
    }

    @Override public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        return Lists.newArrayList();
    }
}

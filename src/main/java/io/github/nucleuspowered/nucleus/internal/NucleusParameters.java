/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal;

import io.github.nucleuspowered.nucleus.argumentparsers.GameProfileArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.NicknameArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.SelectorWrapperArgument;
import io.github.nucleuspowered.nucleus.argumentparsers.UUIDArgument;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

/**
 * A selection of common paremeters for making things consistent
 */
public class NucleusParameters {

    public static class Keys {

        private Keys() {}


        public static final String S_PLAYER = "player";

        public static final Text BOOL = Text.of("true|false");
        public static final Text COMMAND = Text.of("command");
        public static final Text DESCRIPTION = Text.of("description");
        public static final Text LORE = Text.of("lore");
        public static final Text MESSAGE = Text.of("message");
        public static final Text PLAYER = Text.of("player");
        public static final Text PLAYER_OR_CONSOLE = Text.of("player|-");
        public static final Text REASON = Text.of("reason");
        public static final Text SUBJECT = Text.of("subject");
        public static final Text USER = Text.of("user");
        public static final Text USER_UUID = Text.of("user uuid");
    }

    private NucleusParameters() {} // entirely static

    public static final CommandElement ONE_TRUE_FALSE = GenericArguments.onlyOne(GenericArguments.bool(Keys.BOOL));

    public static final CommandElement MANY_ENTITY =
            SelectorWrapperArgument.nicknameSelector(Text.of(Keys.SUBJECT), NicknameArgument.UnderlyingType.PLAYER, false, Entity.class);

    public static final CommandElement MANY_LIVING =
            SelectorWrapperArgument.nicknameSelector(Text.of(Keys.SUBJECT), NicknameArgument.UnderlyingType.PLAYER, false, Living.class);

    // users
    public static final CommandElement ONE_PLAYER =
            GenericArguments.onlyOne(
                    SelectorWrapperArgument.nicknameSelector(Keys.PLAYER, NicknameArgument.UnderlyingType.PLAYER)
            );

    public static final CommandElement MANY_PLAYER =
            SelectorWrapperArgument.nicknameSelector(Keys.PLAYER, NicknameArgument.UnderlyingType.PLAYER, false, Player.class);

    public static final CommandElement ONE_PLAYER_OR_CONSOLE =
            GenericArguments.onlyOne(
                    SelectorWrapperArgument.nicknameSelector(Keys.PLAYER_OR_CONSOLE, NicknameArgument.UnderlyingType.PLAYER_CONSOLE)
            );

    public static final CommandElement MANY_PLAYER_OR_CONSOLE =
            SelectorWrapperArgument.nicknameSelector(Keys.PLAYER_OR_CONSOLE, NicknameArgument.UnderlyingType.PLAYER_CONSOLE, false, Player.class);

    public static final CommandElement ONE_USER =
            GenericArguments.onlyOne(
                    SelectorWrapperArgument.nicknameSelector(Keys.USER, NicknameArgument.UnderlyingType.USER)
            );

    public static final CommandElement ONE_USER_PLAYER_KEY =
            GenericArguments.onlyOne(
                    SelectorWrapperArgument.nicknameSelector(Keys.PLAYER, NicknameArgument.UnderlyingType.USER)
            );

    public static final CommandElement ONE_USER_UUID = GenericArguments.onlyOne(UUIDArgument.user(Keys.USER_UUID));

    public static final CommandElement ONE_GAME_PROFILE_UUID = GenericArguments.onlyOne(UUIDArgument.gameProfile(Keys.USER_UUID));

    public static final CommandElement ONE_GAME_PROFILE = GenericArguments.onlyOne(new GameProfileArgument(Keys.USER));

    public static final CommandElement COMMAND = GenericArguments.remainingRawJoinedStrings(Keys.COMMAND);

    public static final CommandElement OPTIONAL_COMMAND = GenericArguments.optional(COMMAND);

    public static final CommandElement DESCRIPTION = GenericArguments.remainingRawJoinedStrings(Keys.DESCRIPTION);

    public static final CommandElement OPTIONAL_DESCRIPTION = GenericArguments.optional(DESCRIPTION);

    public static final CommandElement LORE = GenericArguments.remainingRawJoinedStrings(Keys.LORE);

    public static final CommandElement MESSAGE = GenericArguments.remainingRawJoinedStrings(Keys.MESSAGE);

    public static final CommandElement OPTIONAL_MESSAGE = GenericArguments.optional(MESSAGE);

    public static final CommandElement REASON = GenericArguments.remainingRawJoinedStrings(Keys.REASON);

    public static final CommandElement OPTIONAL_REASON = GenericArguments.optional(REASON);
}

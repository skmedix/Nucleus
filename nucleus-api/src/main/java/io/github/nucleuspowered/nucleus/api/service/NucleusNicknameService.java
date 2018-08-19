/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.api.service;

import io.github.nucleuspowered.nucleus.api.exceptions.NicknameException;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import java.util.Optional;

import javax.annotation.Nullable;

/**
 * Allows plugins to set and inspect a {@link User}'s current nickname.
 */
public interface NucleusNicknameService {

    /**
     * Gets the current nickname for a user with prefix, if it exists.
     *
     * @param user The {@link User} to inspect.
     * @return The nickname in {@link Text} form, if it exists.
     */
    Optional<Text> getNicknameWithPrefix(User user);

    /**
     * Gets the current nickname for a user, if it exists.
     *
     * @param user The {@link User} to inspect.
     * @return The nickname in {@link Text} form, if it exists.
     */
    Optional<Text> getNickname(User user);

    /**
     * Sets a user's nickname.
     *
     * @param user The {@link User} to change the nickname of
     * @param nickname The nickname, or {@code null} to remove it.
     * @throws NicknameException if the nickname could not be set.
     */
    default void setNickname(User user, @Nullable Text nickname) throws NicknameException {
        setNickname(user, nickname, false);
    }

    /**
     * Sets a user's nickname.
     *
     * @param user The {@link User} to change the nickname of
     * @param nickname The nickname, or {@code null} to remove it.
     * @param bypassRestrictions Whether to bypass the configured restrictions.
     * @throws NicknameException if the nickname could not be set.
     */
    void setNickname(User user, @Nullable Text nickname, boolean bypassRestrictions) throws NicknameException;

    /**
     * Removes the nickname for the specified user.
     *
     * @param user The nickname to set.
     * @throws NicknameException if the nickname could not be set.
     */
    default void removeNickname(User user) throws NicknameException {
        setNickname(user, null);
    }

}

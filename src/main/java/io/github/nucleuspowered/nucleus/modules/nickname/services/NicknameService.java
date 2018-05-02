/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.nickname.services;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.NameUtil;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.exceptions.NicknameException;
import io.github.nucleuspowered.nucleus.api.service.NucleusNicknameService;
import io.github.nucleuspowered.nucleus.dataservices.modular.ModularUserService;
import io.github.nucleuspowered.nucleus.internal.CommandPermissionHandler;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.internal.messages.MessageProvider;
import io.github.nucleuspowered.nucleus.modules.nickname.NicknameModule;
import io.github.nucleuspowered.nucleus.modules.nickname.commands.NicknameCommand;
import io.github.nucleuspowered.nucleus.modules.nickname.config.NicknameConfig;
import io.github.nucleuspowered.nucleus.modules.nickname.config.NicknameConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.nickname.datamodules.NicknameUserDataModule;
import io.github.nucleuspowered.nucleus.modules.nickname.events.ChangeNicknameEvent;
import io.github.nucleuspowered.nucleus.util.CauseStackHelper;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.Tuple;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

public class NicknameService implements NucleusNicknameService, Reloadable {

    private Pattern pattern;
    private int min = 3;
    private int max = 16;
    private final Map<String[], Tuple<Matcher, Text>> replacements = Maps.newHashMap();
    private boolean registered = false;
    private final BiMap<UUID, String> cache = HashBiMap.create();
    private final BiMap<UUID, Text> textCache = HashBiMap.create();

    public void updateCache(UUID player, Text text) {
        this.cache.put(player, text.toPlain());
        this.textCache.put(player, text);
    }

    public Optional<Player> getFromCache(String text) {
        UUID u = this.cache.inverse().get(text);
        if (u != null) {
            Optional<Player> ret = Sponge.getServer().getPlayer(u);
            if (!ret.isPresent()) {
                this.cache.remove(u);
            }

            return ret;
        }

        return Optional.empty();
    }

    public Map<String, UUID> getAllCached() {
        return Maps.newHashMap(this.cache.inverse());
    }

    public Map<String, UUID> startsWithGetMap(String text) {
        return this.cache.inverse().entrySet().stream().filter(x -> x.getKey().startsWith(text.toLowerCase()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public List<UUID> startsWith(String text) {
        return this.cache.inverse().entrySet().stream().filter(x -> x.getKey().startsWith(text.toLowerCase()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    public void removeFromCache(UUID player) {
        this.cache.remove(player);
        this.textCache.remove(player);
    }

    public void register() {
        if (this.registered) {
            return;
        }

        MessageProvider mp = Nucleus.getNucleus().getMessageProvider();
        CommandPermissionHandler permissions = Nucleus.getNucleus().getPermissionRegistry().getPermissionsForNucleusCommand(NicknameCommand.class);

        String colPerm = permissions.getPermissionWithSuffix("colour.");
        String colPerm2 = permissions.getPermissionWithSuffix("color.");

        NameUtil.getColours().forEach((key, value) -> this.replacements.put(new String[]{colPerm + value.getName(), colPerm2 + value.getName()},
                Tuple.of(Pattern.compile("[&]+" + key.toString().toLowerCase(), Pattern.CASE_INSENSITIVE).matcher(""),
                        mp.getTextMessageWithFormat("command.nick.colour.nopermswith", value.getName()))));

        String stylePerm = permissions.getPermissionWithSuffix("style.");
        NameUtil.getStyleKeys().entrySet().stream().filter(x -> x.getKey() != 'k').forEach((k) -> this.replacements
                .put(new String[] { stylePerm + k.getValue().toLowerCase() },
                Tuple.of(Pattern.compile("[&]+" + k.getKey().toString().toLowerCase(), Pattern.CASE_INSENSITIVE).matcher(""),
                        mp.getTextMessageWithFormat("command.nick.style.nopermswith", k.getValue().toLowerCase()))));

        this.replacements.put(new String[] { permissions.getPermissionWithSuffix("magic") },
                Tuple.of(Pattern.compile("[&]+k", Pattern.CASE_INSENSITIVE).matcher(""),
                        mp.getTextMessageWithFormat("command.nick.style.nopermswith", "magic")));
        this.registered = true;
    }

    @Override
    public Optional<Text> getNickname(User user) {
        if (user.isOnline()) {
            return Optional.ofNullable(this.textCache.get(user.getUniqueId()));
        }
        return Nucleus.getNucleus().getUserDataManager().get(user).map(x -> x.get(NicknameUserDataModule.class).getNicknameAsText().orElse(null));
    }

    @Override
    public void setNickname(User user, @Nullable Text nickname, boolean bypassRestrictions) throws NicknameException {
        Cause cause = Sponge.getCauseStackManager().getCurrentCause();
        if (nickname != null) {
            setNick(user, cause, nickname, bypassRestrictions);
        } else {
            removeNick(user, cause);
        }

    }

    public void removeNick(User user, CommandSource src) throws NicknameException {
        removeNick(user, CauseStackHelper.createCause(src));
    }

    private void removeNick(User user, Cause cause) throws NicknameException {
        Text currentNickname = getNickname(user).orElse(null);
        ChangeNicknameEvent cne = new ChangeNicknameEvent(cause, currentNickname, null, user);
        if (Sponge.getEventManager().post(cne)) {
            throw new NicknameException(
                    Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.nick.eventcancel", user.getName()),
                    NicknameException.Type.EVENT_CANCELLED
            );
        }

        Nucleus.getNucleus().getUserDataManager().get(user)
                .orElseThrow(() -> new NicknameException(
                        Nucleus.getNucleus().getMessageProvider()
                                .getTextMessageWithFormat("standard.error.nouser"),
                        NicknameException.Type.NO_USER
                )).get(NicknameUserDataModule.class).removeNickname();
        removeFromCache(user.getUniqueId());

        if (user.isOnline()) {
            user.getPlayer().ifPresent(x ->
                    x.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.delnick.success.base")));
        }
    }

    public void setNick(User pl, CommandSource src, Text nickname, boolean bypass) throws NicknameException {
        setNick(pl, CauseStackHelper.createCause(src), nickname, bypass);
    }

    private void setNick(User pl, Cause cause, Text nickname, boolean bypass) throws NicknameException {
        String plain = nickname.toPlain().trim();
        if (plain.isEmpty()) {
            throw new NicknameException(
                    Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.nick.tooshort"),
                    NicknameException.Type.TOO_SHORT
            );
        }

        // Does the user exist?
        try {
            Optional<User> match =
                    Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(nickname.toPlain().trim());

            // The only person who can use such a name is oneself.
            if (match.isPresent() && !match.get().getUniqueId().equals(pl.getUniqueId())) {
                // Fail - cannot use another's name.
                throw new NicknameException(
                        Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.nick.nameinuse", plain),
                        NicknameException.Type.NOT_OWN_IGN);
            }
        } catch (IllegalArgumentException ignored) {
            // We allow some other nicknames too.
        }

        if (!bypass) {
            // Giving subject must have the colour permissions and whatnot. Also,
            // colour and color are the two spellings we support. (RULE BRITANNIA!)
            Optional<Subject> os = cause.first(Subject.class);
            if (os.isPresent()) {
                stripPermissionless(os.get(), nickname);
            }

            if (!this.pattern.matcher(plain).matches()) {
                throw new NicknameException(
                        Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.nick.nopattern", this.pattern.pattern()),
                        NicknameException.Type.INVALID_PATTERN);
            }

            int strippedNameLength = plain.length();

            // Do a regex remove to check minimum length requirements.
            if (strippedNameLength < Math.max(this.min, 1)) {
                throw new NicknameException(
                        Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.nick.tooshort"),
                        NicknameException.Type.TOO_SHORT
                );
            }

            // Do a regex remove to check maximum length requirements. Will be at least the minimum length
            if (strippedNameLength > Math.max(this.max, this.min)) {
                throw new NicknameException(
                        Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.nick.toolong"),
                        NicknameException.Type.TOO_SHORT
                );
            }
        }

        // Send an event
        Text currentNickname = getNickname(pl).orElse(null);
        ChangeNicknameEvent cne = new ChangeNicknameEvent(cause, currentNickname, nickname, pl);
        if (Sponge.getEventManager().post(cne)) {
            throw new NicknameException(
                    Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.nick.eventcancel", pl.getName()),
                    NicknameException.Type.EVENT_CANCELLED
            );
        }

        ModularUserService mus = Nucleus.getNucleus().getUserDataManager().getUnchecked(pl);
        NicknameUserDataModule nicknameUserDataModule = mus.get(NicknameUserDataModule.class);
        nicknameUserDataModule.setNickname(nickname);
        mus.set(nicknameUserDataModule);
        mus.save();
        Text set = nicknameUserDataModule.getNicknameAsText().get();
        this.updateCache(pl.getUniqueId(), nickname);

        if (pl.isOnline()) {
            pl.getPlayer().get().sendMessage(Text.builder().append(
                Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.nick.success.base")).append(Text.of(" - ", TextColors.RESET, set)).build());
        }

    }

    @Override
    public void onReload() {
        NicknameConfig nc = Nucleus.getNucleus().getConfigAdapter(NicknameModule.ID, NicknameConfigAdapter.class).get().getNodeOrDefault();
        this.pattern = nc.getPattern();
        this.min = nc.getMinNicknameLength();
        this.max = nc.getMaxNicknameLength();
    }

    private void stripPermissionless(Subject source, Text message) throws NicknameException {
        String m = TextSerializers.FORMATTING_CODE.serialize(message);
        if (m.contains("&")) {
            for (Map.Entry<String[], Tuple<Matcher, Text>> r : this.replacements.entrySet()) {
                // If we don't have the required permission...
                if (r.getValue().getFirst().reset(m).find() && Arrays.stream(r.getKey()).noneMatch(source::hasPermission)) {
                    // throw
                    throw new NicknameException(r.getValue().getSecond(), NicknameException.Type.INVALID_STYLE_OR_COLOUR);
                }
            }
        }
    }
}

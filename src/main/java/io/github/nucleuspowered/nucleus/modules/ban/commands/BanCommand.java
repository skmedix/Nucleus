/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.ban.commands;

import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.util.PermissionMessageChannel;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.profile.GameProfileManager;
import org.spongepowered.api.service.ban.BanService;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MutableMessageChannel;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.util.ban.Ban;
import org.spongepowered.api.util.ban.BanTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RegisterCommand("ban")
@Permissions(suggestedLevel = SuggestedLevel.MOD)
@NoModifiers
@EssentialsEquivalent("ban")
@NonnullByDefault
public class BanCommand extends AbstractCommand<CommandSource> {

    static final String notifyPermission = PermissionRegistry.PERMISSIONS_PREFIX + "ban.notify";
    private final String name = "name";

    @Override
    public Map<String, PermissionInformation> permissionsToRegister() {
        Map<String, PermissionInformation> ps = Maps.newHashMap();
        ps.put(notifyPermission, PermissionInformation.getWithTranslation("permission.ban.notify", SuggestedLevel.MOD));
        return ps;
    }

    @Override
    public Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> m = new HashMap<>();
        m.put("offline", PermissionInformation.getWithTranslation("permission.ban.offline", SuggestedLevel.MOD));
        m.put("exempt.target", PermissionInformation.getWithTranslation("permission.tempban.exempt.target", SuggestedLevel.MOD));
        return m;
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                GenericArguments.firstParsing(
                        NucleusParameters.ONE_GAME_PROFILE_UUID,
                        NucleusParameters.ONE_GAME_PROFILE,
                        GenericArguments.onlyOne(GenericArguments.string(Text.of(this.name)))
                ),
                GenericArguments.optionalWeak(NucleusParameters.REASON)
        };
    }

    @Override
    public CommandResult executeCommand(final CommandSource src, CommandContext args) throws Exception {
        final String r = args.<String>getOne(NucleusParameters.Keys.REASON).orElseGet(() ->
                Nucleus.getNucleus().getMessageProvider().getMessageWithFormat("ban.defaultreason"));
        Optional<GameProfile> ou = Optional.ofNullable(
                args.<GameProfile>getOne(NucleusParameters.Keys.USER_UUID).orElseGet(() ->
                        args.<GameProfile>getOne(NucleusParameters.Keys.USER).orElse(null)));
        if (ou.isPresent()) {
            Optional<User> optionalUser = Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(ou.get());
            if ((!optionalUser.isPresent() || !optionalUser.get().isOnline()) && !this.permissions.testSuffix(src, "offline")) {
                throw new ReturnMessageException(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.ban.offline.noperms"));
            }

            if (optionalUser.isPresent() && this.permissions.testSuffix(optionalUser.get(), "exempt.target", src, false)) {
                throw new ReturnMessageException(
                        Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.ban.exempt", optionalUser.get().getName()));
            }

            return executeBan(src, ou.get(), r);
        }

        if (!this.permissions.testSuffix(src, "offline")) {
            throw new ReturnMessageException(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.ban.offline.noperms"));
        }

        final String userToFind = args.<String>getOne(this.name).get();

        // Get the profile async.
        Sponge.getScheduler().createAsyncExecutor(Nucleus.getNucleus()).execute(() -> {
            GameProfileManager gpm = Sponge.getServer().getGameProfileManager();
            try {
                GameProfile gp = gpm.get(userToFind).get();

                // Ban the user sync.
                Sponge.getScheduler().createSyncExecutor(Nucleus.getNucleus()).execute(() -> {
                    // Create the user.
                    UserStorageService uss = Sponge.getServiceManager().provideUnchecked(UserStorageService.class);
                    User user = uss.getOrCreate(gp);
                    src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("gameprofile.new", user.getName()));

                    try {
                        executeBan(src, gp, r);
                    } catch (Exception e) {
                        Nucleus.getNucleus().printStackTraceIfDebugMode(e);
                    }
                });
            } catch (Exception e) {
                Nucleus.getNucleus().printStackTraceIfDebugMode(e);

                src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.ban.profileerror", userToFind));
            }
        });

        return CommandResult.empty();
    }

    private CommandResult executeBan(CommandSource src, GameProfile u, String r) {
        BanService service = Sponge.getServiceManager().provideUnchecked(BanService.class);

        UserStorageService uss = Sponge.getServiceManager().provideUnchecked(UserStorageService.class);
        User user = uss.get(u).get();
        if (!user.isOnline() && !this.permissions.testSuffix(src, "offline")) {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.ban.offline.noperms"));
            return CommandResult.empty();
        }

        if (service.isBanned(u)) {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.ban.alreadyset", u.getName().orElse(
                    Nucleus.getNucleus().getMessageProvider().getMessageWithFormat("standard.unknown"))));
            return CommandResult.empty();
        }

        // Create the ban.
        Ban bp = Ban.builder().type(BanTypes.PROFILE).profile(u).source(src).reason(TextSerializers.FORMATTING_CODE.deserialize(r)).build();
        service.addBan(bp);

        // Get the permission, "quickstart.ban.notify"
        MutableMessageChannel send = new PermissionMessageChannel(notifyPermission).asMutable();
        send.addMember(src);
        send.send(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.ban.applied", u.getName().orElse(
                Nucleus.getNucleus().getMessageProvider().getMessageWithFormat("standard.unknown")), src.getName()));
        send.send(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("standard.reasoncoloured", r));

        if (Sponge.getServer().getPlayer(u.getUniqueId()).isPresent()) {
            Sponge.getServer().getPlayer(u.getUniqueId()).get().kick(TextSerializers.FORMATTING_CODE.deserialize(r));
        }

        return CommandResult.success();
    }
}

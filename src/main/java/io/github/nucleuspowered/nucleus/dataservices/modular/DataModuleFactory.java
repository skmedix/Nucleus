/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.dataservices.modular;

import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.modules.back.datamodules.BackUserTransientModule;
import io.github.nucleuspowered.nucleus.modules.commandspy.datamodules.CommandSpyUserDataModule;
import io.github.nucleuspowered.nucleus.modules.core.datamodules.CoreUserDataModule;
import io.github.nucleuspowered.nucleus.modules.core.datamodules.UniqueUserCountTransientModule;
import io.github.nucleuspowered.nucleus.modules.environment.datamodule.EnvironmentWorldDataModule;
import io.github.nucleuspowered.nucleus.modules.fly.datamodules.FlyUserDataModule;
import io.github.nucleuspowered.nucleus.modules.freezeplayer.datamodules.FreezePlayerUserDataModule;
import io.github.nucleuspowered.nucleus.modules.home.datamodules.HomeUserDataModule;
import io.github.nucleuspowered.nucleus.modules.ignore.datamodules.IgnoreUserDataModule;
import io.github.nucleuspowered.nucleus.modules.invulnerability.datamodules.InvulnerabilityUserDataModule;
import io.github.nucleuspowered.nucleus.modules.jail.datamodules.JailGeneralDataModule;
import io.github.nucleuspowered.nucleus.modules.jail.datamodules.JailUserDataModule;
import io.github.nucleuspowered.nucleus.modules.kit.datamodules.KitUserDataModule;
import io.github.nucleuspowered.nucleus.modules.mail.datamodules.MailUserDataModule;
import io.github.nucleuspowered.nucleus.modules.message.datamodules.MessageUserDataModule;
import io.github.nucleuspowered.nucleus.modules.mute.datamodules.MuteUserDataModule;
import io.github.nucleuspowered.nucleus.modules.nickname.datamodules.NicknameUserDataModule;
import io.github.nucleuspowered.nucleus.modules.note.datamodules.NoteUserDataModule;
import io.github.nucleuspowered.nucleus.modules.powertool.datamodules.PowertoolUserDataModule;
import io.github.nucleuspowered.nucleus.modules.serverlist.datamodules.ServerListGeneralDataModule;
import io.github.nucleuspowered.nucleus.modules.spawn.datamodules.SpawnGeneralDataModule;
import io.github.nucleuspowered.nucleus.modules.spawn.datamodules.SpawnWorldDataModule;
import io.github.nucleuspowered.nucleus.modules.staffchat.datamodules.StaffChatTransientModule;
import io.github.nucleuspowered.nucleus.modules.teleport.datamodules.TeleportUserDataModule;
import io.github.nucleuspowered.nucleus.modules.vanish.datamodules.VanishUserDataModule;
import io.github.nucleuspowered.nucleus.modules.warn.datamodules.WarnUserDataModule;
import io.github.nucleuspowered.nucleus.modules.warp.datamodules.WarpGeneralDataModule;
import io.github.nucleuspowered.nucleus.modules.world.datamodules.WorldgenWorldDataModule;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

final class DataModuleFactory {

    private DataModuleFactory() {}

    private static final Map<Class<? extends DataModule<ModularGeneralService>>,
            Function<ModularGeneralService, ? extends DataModule<ModularGeneralService>>> general = Maps.newHashMap();

    private static final Map<Class<? extends TransientModule<ModularGeneralService>>,
            Function<ModularGeneralService, ? extends TransientModule<ModularGeneralService>>> generalt = Maps.newHashMap();

    private static final Map<Class<? extends DataModule<ModularUserService>>,
            Function<ModularUserService, ? extends DataModule<ModularUserService>>> user = Maps.newHashMap();

    private static final Map<Class<? extends TransientModule<ModularUserService>>,
            Function<ModularUserService, ? extends TransientModule<ModularUserService>>> usert = Maps.newHashMap();

    private static final Map<Class<? extends DataModule<ModularWorldService>>,
            Function<ModularWorldService, ? extends DataModule<ModularWorldService>>> world = Maps.newHashMap();

    private static final Map<Class<? extends TransientModule<ModularWorldService>>,
            Function<ModularWorldService, ? extends TransientModule<ModularWorldService>>> worldt = Maps.newHashMap();

    static {
        generalt.put(UniqueUserCountTransientModule.class, x -> new UniqueUserCountTransientModule());

        general.put(JailGeneralDataModule.class, x -> new JailGeneralDataModule());
        general.put(SpawnGeneralDataModule.class, x -> new SpawnGeneralDataModule());
        general.put(WarpGeneralDataModule.class, x -> new WarpGeneralDataModule());
        general.put(ServerListGeneralDataModule.class, x -> new ServerListGeneralDataModule());

        world.put(EnvironmentWorldDataModule.class, x -> new EnvironmentWorldDataModule());
        world.put(SpawnWorldDataModule.class, x -> new SpawnWorldDataModule());
        world.put(WorldgenWorldDataModule.class, x -> new WorldgenWorldDataModule());

        usert.put(BackUserTransientModule.class, x -> new BackUserTransientModule());
        usert.put(StaffChatTransientModule.class, x -> new StaffChatTransientModule());

        user.put(CommandSpyUserDataModule.class, x -> new CommandSpyUserDataModule());
        user.put(CoreUserDataModule.class, x -> new CoreUserDataModule());
        user.put(FlyUserDataModule.class, FlyUserDataModule::new);
        user.put(FreezePlayerUserDataModule.class, x -> new FreezePlayerUserDataModule());
        user.put(HomeUserDataModule.class, HomeUserDataModule::new);
        user.put(IgnoreUserDataModule.class, x -> new IgnoreUserDataModule());
        user.put(JailUserDataModule.class, JailUserDataModule::new);
        user.put(KitUserDataModule.class, x -> new KitUserDataModule());
        user.put(MailUserDataModule.class, x -> new MailUserDataModule());
        user.put(MessageUserDataModule.class, MessageUserDataModule::new);
        user.put(InvulnerabilityUserDataModule.class, InvulnerabilityUserDataModule::new);
        user.put(MuteUserDataModule.class, x -> new MuteUserDataModule());
        user.put(NicknameUserDataModule.class, NicknameUserDataModule::new);
        user.put(NoteUserDataModule.class, x -> new NoteUserDataModule());
        user.put(PowertoolUserDataModule.class, x -> new PowertoolUserDataModule());
        user.put(TeleportUserDataModule.class, x -> new TeleportUserDataModule());
        user.put(VanishUserDataModule.class, x -> new VanishUserDataModule());
        user.put(WarnUserDataModule.class, x -> new WarnUserDataModule());
    }

    @SuppressWarnings("unchecked")
    public static <T extends DataModule<ModularUserService>> Optional<T> get(Class<T> module, ModularUserService service) {
        if (user.containsKey(module)) {
            return Optional.of((T)user.get(module).apply(service));
        }

        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public static <T extends DataModule<ModularWorldService>> Optional<T> get(Class<T> module, ModularWorldService service) {
        if (world.containsKey(module)) {
            return Optional.of((T)world.get(module).apply(service));
        }

        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public static <T extends DataModule<ModularGeneralService>> Optional<T> get(Class<T> module, ModularGeneralService service) {
        if (general.containsKey(module)) {
            return Optional.of((T)general.get(module).apply(service));
        }

        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public static <T extends TransientModule<ModularUserService>> Optional<T> getTransient(Class<T> module, ModularUserService service) {
        if (usert.containsKey(module)) {
            return Optional.of((T)usert.get(module).apply(service));
        }

        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public static <T extends TransientModule<ModularWorldService>> Optional<T> getTransient(Class<T> module, ModularWorldService service) {
        if (worldt.containsKey(module)) {
            return Optional.of((T)worldt.get(module).apply(service));
        }

        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public static <T extends TransientModule<ModularGeneralService>> Optional<T> getTransient(Class<T> module, ModularGeneralService service) {
        if (generalt.containsKey(module)) {
            return Optional.of((T)generalt.get(module).apply(service));
        }

        return Optional.empty();
    }
}

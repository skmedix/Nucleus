/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.qsml.module;

import com.google.common.collect.ImmutableMap;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.NucleusPlugin;
import io.github.nucleuspowered.nucleus.annotationprocessor.Store;
import io.github.nucleuspowered.nucleus.config.CommandsConfig;
import io.github.nucleuspowered.nucleus.internal.CommandPermissionHandler;
import io.github.nucleuspowered.nucleus.internal.Constants;
import io.github.nucleuspowered.nucleus.internal.InternalServiceManager;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.TaskBase;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterCommandInterceptors;
import io.github.nucleuspowered.nucleus.internal.annotations.RegisterService;
import io.github.nucleuspowered.nucleus.internal.annotations.RequireExistenceOf;
import io.github.nucleuspowered.nucleus.internal.annotations.RequiresPlatform;
import io.github.nucleuspowered.nucleus.internal.annotations.ServerOnly;
import io.github.nucleuspowered.nucleus.internal.annotations.SkipOnError;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Scan;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.CommandBuilder;
import io.github.nucleuspowered.nucleus.internal.command.ICommandInterceptor;
import io.github.nucleuspowered.nucleus.internal.docgen.DocGenCache;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.internal.permissions.ServiceChangeListener;
import io.github.nucleuspowered.nucleus.internal.registry.NucleusRegistryModule;
import io.github.nucleuspowered.nucleus.internal.services.CommandRemapperService;
import io.github.nucleuspowered.nucleus.internal.text.Tokens;
import io.github.nucleuspowered.nucleus.internal.traits.InternalServiceManagerTrait;
import io.github.nucleuspowered.nucleus.internal.traits.MessageProviderTrait;
import io.github.nucleuspowered.nucleus.modules.playerinfo.handlers.BasicSeenInformationProvider;
import io.github.nucleuspowered.nucleus.modules.playerinfo.handlers.SeenHandler;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.context.ContextCalculator;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;
import uk.co.drnaylor.quickstart.Module;
import uk.co.drnaylor.quickstart.annotations.ModuleData;
import uk.co.drnaylor.quickstart.config.AbstractConfigAdapter;
import uk.co.drnaylor.quickstart.exceptions.MissingDependencyException;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

@Store(isRoot = true)
public abstract class StandardModule implements Module, InternalServiceManagerTrait, MessageProviderTrait {

    private final String moduleId;
    private final String moduleName;
    private String packageName;
    protected final Nucleus plugin;
    protected final InternalServiceManager serviceManager;
    private final CommandsConfig commandsConfig;
    @Nullable private Map<String, List<String>> msls;
    private final String message = NucleusPlugin.getNucleus().getMessageProvider().getMessageWithFormat("config.enabled");

    public StandardModule() {
        ModuleData md = this.getClass().getAnnotation(ModuleData.class);
        this.moduleId = md.id();
        this.moduleName = md.name();
        this.plugin = NucleusPlugin.getNucleus();
        this.serviceManager = this.plugin.getInternalServiceManager();
        this.commandsConfig = this.plugin.getCommandsConfig();
    }

    public void init(Map<String, List<String>> m) {
        this.msls = m;
    }

    @Override
    public final void checkExternalDependencies() throws MissingDependencyException {
        if (this.getClass().isAnnotationPresent(ServerOnly.class) && !Nucleus.getNucleus().isServer()) {
            throw new MissingDependencyException("This module is server only and will not be loaded.");
        }
    }

    protected Map<String, Tokens.Translator> tokensToRegister() {
        return ImmutableMap.of();
    }

    /**
     * Non-configurable module, no configuration to register.
     *
     * @return {@link Optional#empty()}
     */
    @Override
    public Optional<AbstractConfigAdapter<?>> getConfigAdapter() {
        return Optional.empty();
    }

    @Override
    public final void preEnable() {
        try {
            loadRegistries();
            registerServices();
            performPreTasks();
            registerCommandInterceptors();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot enable module!", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void registerServices() throws Exception {
        RegisterService[] annotations = getClass().getAnnotationsByType(RegisterService.class);
        if (annotations != null && annotations.length > 0) {
            // for each annotation, attempt to register the service.
            for (RegisterService service : annotations) {
                Class<?> impl = service.value();
                // create the impl
                Object serviceImpl = getInstance(service.value());
                if (serviceImpl == null) {
                    String error = "ERROR: Cannot instantiate " + impl.getName();
                    Nucleus.getNucleus().getLogger().error(error);
                    if (!service.optional()) {
                        throw new IllegalStateException(error);
                    }
                }

                Class<?> api = service.apiService();

                if (api != Object.class) {
                    if (api.isInstance(serviceImpl)) {
                        // OK
                        register((Class) api, (Class) impl, serviceImpl, service.replaceInternal());
                    } else {
                        String error = "ERROR: " + api.getName() + " does not inherit from " + impl.getName();
                        Nucleus.getNucleus().getLogger().error(error);
                        throw new IllegalStateException(error);
                    }
                } else {
                    register((Class) impl, serviceImpl, service.replaceInternal());
                }

                if (serviceImpl instanceof Reloadable) {
                    Reloadable reloadable = (Reloadable) serviceImpl;
                    Nucleus.getNucleus().registerReloadable(reloadable);
                    reloadable.onReload();
                }

                if (serviceImpl instanceof ContextCalculator) {
                    try {
                        // boolean matches(Context context, T calculable);
                        serviceImpl.getClass().getMethod("matches", Context.class, Subject.class);

                        // register it
                        ServiceChangeListener.getInstance().registerCalculator((ContextCalculator<Subject>) serviceImpl);
                    } catch (NoSuchMethodException e) {
                        // ignored
                    }
                }
            }
        }
    }

    private void registerCommandInterceptors() throws Exception {
        RegisterCommandInterceptors annotation = getClass().getAnnotation(RegisterCommandInterceptors.class);
        if (annotation != null) {
            // for each annotation, attempt to register the service.
            for (Class<? extends ICommandInterceptor> service : annotation.value()) {

                // create the impl
                ICommandInterceptor impl;
                try {
                    impl = service.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    String error = "ERROR: Cannot instantiate ICommandInterceptor " + service.getName();
                    Nucleus.getNucleus().getLogger().error(error);
                    throw new IllegalStateException(error, e);
                }

                if (impl instanceof Reloadable) {
                    Reloadable reloadable = (Reloadable) impl;
                    Nucleus.getNucleus().registerReloadable(reloadable);
                    reloadable.onReload();
                }

                AbstractCommand.registerInterceptor(impl);
            }
        }
    }

    @Override
    public final void onEnable() {
        this.packageName = this.getClass().getPackage().getName() + ".";

        // Construct commands
        loadCommands();
        loadEvents();
        loadRunnables();
        prepareAliasedCommands();
        try {
            performEnableTasks();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot enable module!", e);
        }
    }

    @Override
    public final void postEnable() {
        loadTokens();
        setPermissionPredicates();
        configTasks();
        try {
            performPostTasks();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot perform post enable on module!", e);
        }
    }

    protected void setPermissionPredicates() {}

    @SuppressWarnings("unchecked")
    private void loadCommands() {

        Set<Class<? extends AbstractCommand<?>>> cmds;
        if (this.msls != null) {
            cmds = new HashSet<>();
            List<String> l = this.msls.get(Constants.COMMAND);
            if (l == null) {
                return;
            }

            for (String s : l) {
                try {
                    checkPlatformOpt((Class<? extends AbstractCommand<?>>) Class.forName(s)).ifPresent(cmds::add);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            cmds = new HashSet<>(
                    performFilter(getStreamForModule(AbstractCommand.class).map(x -> (Class<? extends AbstractCommand<?>>) x))
                            .collect(Collectors.toSet()));

            // Find all commands that are also scannable.
            performFilter(this.plugin.getModuleContainer().getLoadedClasses().stream()
                    .filter(x -> x.getPackage().getName().startsWith(this.packageName))
                    .filter(x -> x.isAnnotationPresent(Scan.class))
                    .flatMap(x -> Arrays.stream(x.getDeclaredClasses()))
                    .filter(AbstractCommand.class::isAssignableFrom)
                    .map(x -> (Class<? extends AbstractCommand<?>>) x))
                    .forEach(cmds::add);
        }

        // We all love the special injector. We just want to provide the module with more commands, in case it needs a child.
        Set<Class<? extends AbstractCommand>> commandBases =  cmds.stream().filter(x -> {
            RegisterCommand rc = x.getAnnotation(RegisterCommand.class);
            return (rc != null && rc.subcommandOf().equals(AbstractCommand.class));
        }).collect(Collectors.toSet());

        CommandBuilder builder = new CommandBuilder(this.plugin, cmds, this.moduleId, this.moduleName);
        commandBases.forEach(builder::buildCommand);

        try {
            this.commandsConfig.mergeDefaults(builder.getNodeToMerge());
            this.commandsConfig.save();
        } catch (Exception e) {
            this.plugin.getLogger().error("Could not save defaults.");
            e.printStackTrace();
        }
    }

    private void prepareAliasedCommands() {
        ImmutableMap<String, String> toRegister = remapCommand();
        if (!toRegister.isEmpty()) {
            CommentedConfigurationNode ccn = SimpleCommentedConfigurationNode.root();
            for (Map.Entry<String, String> map : toRegister.entrySet()) {
                if (this.commandsConfig.getCommandNode(map.getKey()).getNode("enabled").getBoolean(true)) {
                    getServiceUnchecked(CommandRemapperService.class).addMapping(map.getKey().toLowerCase(), map.getValue().toLowerCase());
                }

                ccn.getNode(map.getKey(), "enabled").setComment(this.message).setValue(true);
            }

        }
    }

    private Stream<Class<? extends AbstractCommand<?>>> performFilter(Stream<Class<? extends AbstractCommand<?>>> stream) {
        return stream.filter(x -> x.isAnnotationPresent(RegisterCommand.class));
    }

    @SuppressWarnings("unchecked")
    private void loadEvents() {
        Set<Class<? extends ListenerBase>> listenersToLoad;
        if (this.msls != null) {
            listenersToLoad = new HashSet<>();
            List<String> l = this.msls.get(Constants.LISTENER);
            if (l == null) {
                return;
            }

            for (String s : l) {
                try {
                    checkPlatformOpt((Class<? extends ListenerBase>) Class.forName(s)).ifPresent(listenersToLoad::add);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            listenersToLoad = getStreamForModule(ListenerBase.class).collect(Collectors.toSet());
        }

        Optional<DocGenCache> docGenCache = this.plugin.getDocGenCache();
        listenersToLoad.stream().map(x -> this.getInstance(x, true)).filter(Objects::nonNull).forEach(c -> {
            // Register suggested permissions
            c.getPermissions().forEach((k, v) -> this.plugin.getPermissionRegistry().registerOtherPermission(k, v));
            docGenCache.ifPresent(x -> x.addPermissionDocs(this.moduleId, c.getPermissions()));

            if (c instanceof ListenerBase.Conditional) {
                // Add reloadable to load in the listener dynamically if required.
                Reloadable tae = () -> {
                    Sponge.getEventManager().unregisterListeners(c);
                    if (c instanceof Reloadable) {
                        ((Reloadable) c).onReload();
                    }

                    if (((ListenerBase.Conditional) c).shouldEnable()) {
                        Sponge.getEventManager().registerListeners(this.plugin, c);
                    }
                };

                this.plugin.registerReloadable(tae);
                try {
                    tae.onReload();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (c instanceof Reloadable) {
                this.plugin.registerReloadable(((Reloadable) c));
                Sponge.getEventManager().registerListeners(this.plugin, c);
            } else {
                Sponge.getEventManager().registerListeners(this.plugin, c);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void loadRunnables() {
        Set<Class<? extends TaskBase>> tasksToLoad;
        if (this.msls != null) {
            tasksToLoad = new HashSet<>();
            List<String> l = this.msls.get(Constants.RUNNABLE);
            if (l == null) {
                return;
            }

            for (String s : l) {
                try {
                    checkPlatformOpt((Class<? extends TaskBase>) Class.forName(s)).ifPresent(tasksToLoad::add);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            tasksToLoad = getStreamForModule(TaskBase.class).collect(Collectors.toSet());
        }

        Optional<DocGenCache> docGenCache = this.plugin.getDocGenCache();

        tasksToLoad.stream().map(this::getInstance).filter(Objects::nonNull).forEach(c -> {
            c.getPermissions().forEach((k, v) -> this.plugin.getPermissionRegistry().registerOtherPermission(k, v));
            docGenCache.ifPresent(x -> x.addPermissionDocs(this.moduleId, c.getPermissions()));
            Task.Builder tb = Sponge.getScheduler().createTaskBuilder().interval(c.interval().toMillis(), TimeUnit.MILLISECONDS);
            if (Nucleus.getNucleus().isServer()) {
                tb.execute(c);
            } else {
                tb.execute(t -> {
                    if (Sponge.getGame().isServerAvailable()) {
                        c.accept(t);
                    }
                });
            }

            if (c.isAsync()) {
                tb.async();
            }

            tb.submit(this.plugin);

            if (c instanceof Reloadable) {
                this.plugin.registerReloadable((Reloadable) c);
                try {
                    ((Reloadable) c).onReload();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void loadTokens() {
        Map<String, Tokens.Translator> map = tokensToRegister();
        if (!map.isEmpty()) {
            map.forEach((k, t) -> {
                try {
                    if (!Tokens.INSTANCE.register(k, t, true)) {
                        Nucleus.getNucleus().getLogger().warn("Could not register primary token identifier " + k);
                    }
                } catch (IllegalArgumentException e) {
                    Nucleus.getNucleus().getLogger().warn("Could not register nucleus token identifier " + k);
                }
            });
        }
    }

    private void loadRegistries() {
        Set<Class<? extends NucleusRegistryModule>> registries;
        if (this.msls != null) {
            registries = new HashSet<>();
            List<String> l = this.msls.get(Constants.REGISTRY);
            if (l == null) {
                return;
            }

            for (String s : l) {
                try {
                    checkPlatformOpt((Class<? extends NucleusRegistryModule>) Class.forName(s)).ifPresent(registries::add);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            registries = getStreamForModule(NucleusRegistryModule.class).collect(Collectors.toSet());
        }

        for (Class<? extends NucleusRegistryModule> r : registries) {
            NucleusRegistryModule instance = getInstance(r);
            try {
                instance.init();
            } catch (Exception e) {
                Nucleus.getNucleus().getLogger().error("Could not register registry " + r.getName(), e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> Stream<Class<? extends T>> getStreamForModule(Class<T> assignableClass) {
        return Nucleus.getNucleus().getModuleContainer().getLoadedClasses().stream()
                .filter(assignableClass::isAssignableFrom)
                .filter(x -> x.getPackage().getName().startsWith(this.packageName))
                .filter(x -> !Modifier.isAbstract(x.getModifiers()) && !Modifier.isInterface(x.getModifiers()))
                .filter(this::checkPlatform)
                .map(x -> (Class<? extends T>)x);
    }

    protected void performPreTasks() throws Exception { }

    protected void performEnableTasks() throws Exception { }

    protected void performPostTasks() { }

    void configTasks() {

    }

    protected ImmutableMap<String, String> remapCommand() {
        return ImmutableMap.of();
    }

    private <T> T getInstance(Class<T> clazz) {
        return getInstance(clazz, false);
    }

    private <T> T getInstance(Class<T> clazz, boolean checkMethods) {
        try {
            RequireExistenceOf[] v = clazz.getAnnotationsByType(RequireExistenceOf.class);
            if (v.length > 0) {
                try {
                    for (RequireExistenceOf r : v) {
                        String toFind = r.value();
                        String[] a;
                        if (toFind.contains("#")) {
                            a = toFind.split("#", 2);
                        } else {
                            a = new String[]{toFind};
                        }

                        // Check the class.
                        Class<?> c = Class.forName(a[0]);
                        if (a.length == 2) {
                            // Check the method
                            Method[] methods = c.getDeclaredMethods();
                            boolean methodFound = false;
                            for (Method m : methods) {
                                if (m.getName().equals(a[1])) {
                                    methodFound = true;
                                    break;
                                }
                            }

                            if (!methodFound) {
                                if (r.showError()) {
                                    throw new RuntimeException();
                                }

                                return null;
                            }
                        }
                    }
                } catch (ClassNotFoundException | RuntimeException | NoClassDefFoundError e) {
                    this.plugin.getLogger().warn(NucleusPlugin.getNucleus().getMessageProvider()
                            .getMessageWithFormat("startup.injectablenotloaded", clazz.getName()));
                    return null;
                }
            }

            if (checkMethods) {
                // This checks all the methods to ensure the classes in question exist.
                clazz.getDeclaredMethods();
            }

            return clazz.newInstance();

        // I can't believe I have to do this...
        } catch (IllegalAccessException | InstantiationException | RuntimeException | NoClassDefFoundError e) {
            if (clazz.isAnnotationPresent(SkipOnError.class)) {
                this.plugin.getLogger().warn(NucleusPlugin.getNucleus().getMessageProvider().getMessageWithFormat("startup.injectablenotloaded", clazz.getName()));
                return null;
            }

            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    private <T extends Class<?>> Optional<T> checkPlatformOpt(T clazz) {
        if (checkPlatform(clazz)) {
            return Optional.of(clazz);
        }

        return Optional.empty();
    }

    private <T extends Class<?>> boolean checkPlatform(T clazz) {
        if (clazz.isAnnotationPresent(RequiresPlatform.class)) {
            String platformId = Sponge.getPlatform().getContainer(Platform.Component.GAME).getId();
            boolean loadable = Arrays.stream(clazz.getAnnotation(RequiresPlatform.class).value()).anyMatch(platformId::equalsIgnoreCase);
            if (!loadable) {
                this.plugin.getLogger().warn("Not loading /" + clazz.getSimpleName() + ": platform " + platformId + " is not supported.");
                return false;
            }
        }

        return true;
    }

    protected final void createSeenModule(BiFunction<CommandSource, User, Collection<Text>> function) {
        createSeenModule((String)null, function);
    }

    protected final void createSeenModule(@Nullable Class<? extends AbstractCommand> permissionClass, BiFunction<CommandSource, User, Collection<Text>> function) {
        // Register seen information.
        CommandPermissionHandler permissionHandler = this.plugin.getPermissionRegistry().getPermissionsForNucleusCommand(permissionClass);
        createSeenModule(permissionHandler == null ? null : permissionHandler.getBase(), function);
    }

    protected void createSeenModule(@Nullable String permission, BiFunction<CommandSource, User, Collection<Text>> function) {
        this.plugin.getInternalServiceManager().getService(SeenHandler.class).ifPresent(x ->
                x.register(this.plugin, this.getClass().getAnnotation(ModuleData.class).name(), new BasicSeenInformationProvider(permission, function)));
    }

    protected final <I, S extends I> void register(Class<S> impl) throws IllegalAccessException, InstantiationException {
        Nucleus.getNucleus().getInternalServiceManager().registerService(impl, impl.newInstance());
    }

    protected final <I, S extends I> void register(Class<I> api, Class<S> impl, boolean re) throws IllegalAccessException, InstantiationException {
        S object = impl.newInstance();
        Sponge.getServiceManager().setProvider(Nucleus.getNucleus(), api, object);
        Nucleus.getNucleus().getInternalServiceManager().registerService(api, object);
        register(impl, object, re);
    }

    protected final <I, S extends I> void register(Class<S> impl, S object, boolean re) {
        Nucleus.getNucleus().getInternalServiceManager().registerService(impl, object, re);
    }

    protected final <I, S extends I> void register(Class<I> api, Class<S> impl, S object, boolean re) {
        Sponge.getServiceManager().setProvider(Nucleus.getNucleus(), api, object);
        Nucleus.getNucleus().getInternalServiceManager().registerService(api, object, re);
        register(impl, object, re);
    }
}

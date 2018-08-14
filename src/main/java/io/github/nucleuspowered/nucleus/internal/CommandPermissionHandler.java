/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoCooldown;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoCost;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoDocumentation;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoPermissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoWarmup;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.internal.traits.PermissionTrait;
import io.github.nucleuspowered.nucleus.modules.core.CoreModule;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfig;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfigAdapter;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.permission.Subject;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.function.Supplier;

public class CommandPermissionHandler implements PermissionTrait {

    private static boolean consoleCanBypass = true;

    public static void onReload() {
        consoleCanBypass = Nucleus.getNucleus().getConfigValue(CoreModule.ID, CoreConfigAdapter.class, CoreConfig::isConsoleOverride).orElse(true);
    }

    private final Map<String, PermissionInformation> mssl = Maps.newHashMap();
    private final String prefix;
    private final String base;
    private final String warmup;
    private final String cooldown;
    private final String cost;
    private final String others;

    private final boolean justReturnTrue;

    public CommandPermissionHandler(Class<? extends AbstractCommand> cab, Nucleus plugin) {
        this.justReturnTrue = cab.isAnnotationPresent(NoPermissions.class);

        // If there are no permissions to assign, we just return true.
        if (this.justReturnTrue) {
            this.prefix = "";
            this.base = "";
            this.warmup = "";
            this.cooldown = "";
            this.cost = "";
            this.others = "";
            return;
        }

        Permissions c = cab.getAnnotation(Permissions.class);
        if (c == null) {
            c = new Permissions() {

                @Override
                public String[] value() {
                    return new String[0];
                }

                @Override
                public String mainOverride() {
                    return "";
                }

                @Override
                public String prefix() {
                    return "";
                }

                @Override
                public String suffix() {
                    return "";
                }

                @Override
                public boolean supportsSelectors() {
                    return false;
                }

                @Override public boolean supportsOthers() {
                    return false;
                }

                @Override
                public SuggestedLevel suggestedLevel() {
                    return SuggestedLevel.ADMIN;
                }

                @Override
                public Class<? extends Annotation> annotationType() {
                    return Permissions.class;
                }
            };
        }

        RegisterCommand co = cab.getAnnotation(RegisterCommand.class);
        String command = co.value()[0];
        StringBuilder sb = new StringBuilder(PermissionRegistry.PERMISSIONS_PREFIX);
        if (!c.prefix().isEmpty()) {
            sb.append(c.prefix()).append(".");
        }

        if (c.mainOverride().isEmpty()) {
            sb.append(command);
        } else {
            sb.append(c.mainOverride());
        }

        sb.append(".");
        if (!c.suffix().isEmpty()) {
            sb.append(c.suffix()).append(".");
        }

        this.prefix = sb.toString();

        this.base = this.prefix + "base";

        if (co.subcommandOf() != AbstractCommand.class) {
            command = String.format("%s %s", co.subcommandOf().getAnnotation(RegisterCommand.class).value()[0], command);
        }

        this.warmup = this.prefix + "exempt.warmup";
        this.cooldown = this.prefix + "exempt.cooldown";
        this.cost = this.prefix + "exempt.cost";
        this.others = this.prefix + "others";

        if (!cab.isAnnotationPresent(NoDocumentation.class)) {
            this.mssl.put(this.base,
                new PermissionInformation(plugin.getMessageProvider().getMessageWithFormat("permission.base", command), c.suggestedLevel()));

            if (c.supportsOthers()) {
                this.mssl.put(this.others, new PermissionInformation(plugin.getMessageProvider().getMessageWithFormat("permission.others", co.value()[0]),
                    SuggestedLevel.ADMIN));
            }

            if (!cab.isAnnotationPresent(NoModifiers.class)) {
                if (!cab.isAnnotationPresent(NoWarmup.class) || cab.getAnnotation(NoWarmup.class).generatePermissionDocs()) {
                    this.mssl.put(this.warmup, new PermissionInformation(plugin.getMessageProvider().getMessageWithFormat("permission.exempt.warmup", command),
                            SuggestedLevel.ADMIN));
                }

                if (!cab.isAnnotationPresent(NoCooldown.class)) {
                    this.mssl.put(this.cooldown,
                            new PermissionInformation(plugin.getMessageProvider().getMessageWithFormat("permission.exempt.cooldown", command),
                                    SuggestedLevel.ADMIN));
                }

                if (!cab.isAnnotationPresent(NoCost.class)) {
                    this.mssl.put(this.cost, new PermissionInformation(plugin.getMessageProvider().getMessageWithFormat("permission.exempt.cost", command),
                            SuggestedLevel.ADMIN));
                }
            }
        }

        plugin.getPermissionRegistry().addHandler(cab, this);
    }

    public boolean isPassthrough() {
        return this.justReturnTrue;
    }

    public String getBase() {
        return this.base;
    }

    public String getOthers() {
        return this.others;
    }

    public boolean testBase(Subject src) {
        return test(src, this.base);
    }

    public boolean testWarmupExempt(Subject src) {
        return test(src, this.warmup);
    }

    public boolean testCooldownExempt(Subject src) {
        return test(src, this.cooldown);
    }

    public boolean testCostExempt(Subject src) {
        return test(src, this.cost);
    }

    public boolean testOthers(Subject src) {
        return test(src, this.others);
    }

    public void registerPermissionSuffix(String suffix, PermissionInformation pi) {
        this.mssl.put(this.prefix + suffix, pi);
    }

    public void registerPermission(String permission, PermissionInformation pi) {
        this.mssl.put(permission, pi);
    }

    public <X extends Exception> void checkSuffix(Subject src, String suffix, Supplier<X> exception) throws X {
        if (src instanceof User && !(src instanceof Player) && ((User) src).getPlayer().isPresent()) {
            src = ((User) src).getPlayer().get();
        }

        check(src, this.prefix + suffix, exception);
    }

    public boolean testSuffix(Subject src, String suffix) {
        if (src instanceof User && !(src instanceof Player) && ((User) src).getPlayer().isPresent()) {
            src = ((User) src).getPlayer().get();
        }

        return test(src, this.prefix + suffix);
    }

    /**
     * Tests a permission with the {@link #prefix} as a root, unless the actor is the console, then the resultIfOverriden is returned.
     *
     * @param src The subject to check the permission on.
     * @param suffix The suffix to add to the prefix.
     * @param actor The actor that might affect the outcome of this check.
     * @param resultIfOverriden If the actor is the console, returns this result if the console can bypass this permission.
     * @return The result of the check.
     */
    public boolean testSuffix(Subject src, String suffix, CommandSource actor, boolean resultIfOverriden) {
        if (consoleCanBypass && actor instanceof ConsoleSource) {
            return resultIfOverriden;
        }

        return testSuffix(src, suffix);
    }

    public String getPermissionWithSuffix(String suffix) {
        return this.prefix + suffix;
    }

    public Map<String, PermissionInformation> getSuggestedPermissions() {
        return ImmutableMap.copyOf(this.mssl);
    }

    private boolean test(Subject src, String permission) {
        return this.justReturnTrue || hasPermission(src, permission);
    }

    private <X extends Exception> void check(Subject src, String permission, Supplier<X> exception) throws X {
        if (!test(src, permission)) {
            throw exception.get();
        }
    }
}

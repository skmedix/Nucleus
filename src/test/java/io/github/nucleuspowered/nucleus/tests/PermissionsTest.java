/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.tests;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.github.nucleuspowered.nucleus.internal.PermissionRegistry;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.tests.util.TestModule;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.service.permission.Subject;

import java.util.Arrays;

/**
 * Tests permissions are assigned correctly.
 */
public class PermissionsTest extends TestBase {

    /**
     * Tests that the specified permissions are in the permission list.
     */
    @SuppressWarnings("CanBeFinal")
    @RunWith(Parameterized.class)
    public static class ValidTest {

        @BeforeClass
        public static void setup() throws Exception {
            TestBase.testSetup();
        }

        @Parameterized.Parameters(name = "{index}: Permission {0} on {1}")
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    { PermissionRegistry.PERMISSIONS_PREFIX + "test.base", PermissionOne.class },
                    { PermissionRegistry.PERMISSIONS_PREFIX + "prefix.test.base", PermissionRoot.class },
                    { PermissionRegistry.PERMISSIONS_PREFIX + "test.suffix.base", PermissionSub.class },
                    { PermissionRegistry.PERMISSIONS_PREFIX + "prefix.test.suffix.base", PermissionRootSub.class },
                    { PermissionRegistry.PERMISSIONS_PREFIX + "test.base", PermissionCustom.class },
                    { PermissionRegistry.PERMISSIONS_PREFIX + "mainOverride.base", PermissionAlias.class }
            });
        }

        @Parameterized.Parameter()
        public String permission;

        @Parameterized.Parameter(1)
        public Class<? extends AbstractCommand> clazz;

        @Test
        public void testPermissionIsValid() throws IllegalAccessException, InstantiationException {
            AbstractCommand c = this.clazz.newInstance();
            getInjector().injectMembers(c);
            c.postInit();
            Subject s = Mockito.mock(Subject.class);
            Mockito.when(s.hasPermission(Matchers.any())).thenReturn(false);
            Mockito.when(s.hasPermission(Matchers.eq(this.permission))).thenReturn(true);
            Mockito.validateMockitoUsage();
            Assert.assertTrue("The permission " + this.permission + " was not available for " + this.clazz.getName(), c.getPermissionHandler().testBase(s));
        }

    }

    /**
     * Tests that the specified permissions are not in the permission list.
     */
    @SuppressWarnings("CanBeFinal")
    @RunWith(Parameterized.class)
    public static class InvalidTest {

        @BeforeClass
        public static void setup() throws Exception {
            TestBase.testSetup();
        }

        @Parameterized.Parameters
        public static Iterable<Object[]> data() {
            return Arrays.asList(new Object[][] {
                    { PermissionRegistry.PERMISSIONS_PREFIX + "test2.base", PermissionOne.class },
                    { PermissionRegistry.PERMISSIONS_PREFIX + "test.base", PermissionRoot.class },
                    { PermissionRegistry.PERMISSIONS_PREFIX + "test.base", PermissionSub.class },
                    { PermissionRegistry.PERMISSIONS_PREFIX + "test.base", PermissionRootSub.class },
                    { PermissionRegistry.PERMISSIONS_PREFIX + "prefix.test.base", PermissionRootSub.class },
                    { PermissionRegistry.PERMISSIONS_PREFIX + "test.suffix.base", PermissionRootSub.class }
            });
        }

        @Parameterized.Parameter()
        public String permission;

        @Parameterized.Parameter(1)
        public Class<? extends AbstractCommand> clazz;

        @Test
        public void testPermissionIsNotValid() throws IllegalAccessException, InstantiationException {
            AbstractCommand c = this.clazz.newInstance();
            getInjector().injectMembers(c);
            c.postInit();
            Subject s = Mockito.mock(Subject.class);
            Mockito.when(s.hasPermission(Matchers.any())).thenReturn(false);
            Mockito.when(s.hasPermission(Matchers.eq(this.permission))).thenReturn(true);
            Mockito.validateMockitoUsage();
            Assert.assertFalse("The permission " + this.permission + " was not available for " + this.clazz.getName(), c.getPermissionHandler().testBase(s));
        }
    }

    @Permissions
    @RegisterCommand({"test", "test2"})
    public static class PermissionOne extends AbstractCommand<CommandSource> {

        @Override
        public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
            return null;
        }
    }

    @Permissions(prefix = "prefix")
    @RegisterCommand({"test", "test2"})
    public static class PermissionRoot extends PermissionOne { }

    @Permissions(suffix = "suffix")
    @RegisterCommand({"test", "test2"})
    public static class PermissionSub extends PermissionOne { }

    @Permissions(prefix = "prefix", suffix = "suffix")
    @RegisterCommand({"test", "test2"})
    public static class PermissionRootSub extends PermissionOne { }

    @Permissions(mainOverride = "mainOverride")
    @RegisterCommand({"test", "test2"})
    public static class PermissionAlias extends PermissionOne { }

    @Permissions({"test.test"})
    @RegisterCommand({"test", "test2"})
    public static class PermissionCustom extends PermissionOne { }

    private static Injector getInjector() {
        return Guice.createInjector(new TestModule());
    }
}

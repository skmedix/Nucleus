/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.tests.arguments;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.argumentparsers.NicknameArgument;
import io.github.nucleuspowered.nucleus.tests.TestBase;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.ServiceManager;
import org.spongepowered.api.service.user.UserStorageService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Sponge.class)
public class NicknameArgumentTests extends TestBase {

    @Test
    public void testWhenTwoPlayersWithTheSameNameAreInTheUserDatabaseOnlyAnExactMatchIsReturned() throws ArgumentParseException {
        List<?> list = getParser().accept("test", mockSource(), new CommandArgs("", new ArrayList<>()));

        Assert.assertEquals(1, list.size());
        Assert.assertEquals("test", ((User)list.get(0)).getName());
    }

    @Test
    public void testWhenTwoPlayersWithTheSameNameAreInTheUserDatabaseWithNoExactMatchReturnsBothIfTheyOtherwiseMatch() throws ArgumentParseException {
        List<?> list = getParser().accept("tes", mockSource(), new CommandArgs("", new ArrayList<>()));
        Assert.assertEquals(2, list.size());
    }

    @Test
    public void testWhenTwoPlayersWithTheSameNameAreInTheUserDatabaseWithNoExactMatchReturnsOneReturnsIfOnlyOneMatches() throws ArgumentParseException {
        List<?> list = getParser().accept("testt", mockSource(), new CommandArgs("", new ArrayList<>()));
        Assert.assertEquals(1, list.size());
        Assert.assertEquals("testtest", ((User)list.get(0)).getName());
    }

    @Test
    public void testWhenTwoPlayersWithTheSameNameAreInTheUserDatabaseWithNoExactMatchReturnsNoneReturnIfNoneMatch() throws ArgumentParseException {
        List<?> list = getParser().accept("blah", mockSource(), new CommandArgs("", new ArrayList<>()));
        Assert.assertTrue(list.isEmpty());
    }

    private NicknameArgument.UserParser getParser() {
        // Setup the mock UserStorageService
        setupSpongeMock();

        // We're testing the UserParser
        return new NicknameArgument.UserParser(false);
    }

    private void setupSpongeMock() {
        PowerMockito.mockStatic(Sponge.class);
        ServiceManager manager = Mockito.mock(ServiceManager.class);
        UserStorageService service = getMockUserStorageService();
        Mockito.when(manager.provideUnchecked(UserStorageService.class)).thenReturn(service);
        Mockito.when(Sponge.getServiceManager()).thenReturn(manager);
    }

    private UserStorageService getMockUserStorageService() {
        GameProfile gp1 = Mockito.mock(GameProfile.class);
        GameProfile gp2 = Mockito.mock(GameProfile.class);
        Collection<GameProfile> names = ImmutableList.of(gp1, gp2);
        Mockito.when(gp1.getName()).thenReturn(Optional.of("test"));
        Mockito.when(gp2.getName()).thenReturn(Optional.of("testtest"));

        UserStorageService mockUss = Mockito.mock(UserStorageService.class);
        Mockito.when(mockUss.match(Mockito.anyString())).thenAnswer((Answer<Collection<GameProfile>>) invocation ->
                names.stream().filter(x -> x.getName().get()
                        .startsWith(invocation.getArgumentAt(0, String.class).toLowerCase())).collect(Collectors.toList()));
        Mockito.when(mockUss.getAll()).thenReturn(Lists.newArrayList(gp1, gp2));

        User u1 = Mockito.mock(User.class);
        Mockito.when(u1.getName()).thenAnswer(g -> gp1.getName().get());
        Mockito.when(u1.getPlayer()).thenAnswer(g -> Optional.empty());
        User u2 = Mockito.mock(User.class);
        Mockito.when(u2.getName()).thenAnswer(g -> gp2.getName().get());
        Mockito.when(u2.getPlayer()).thenAnswer(g -> Optional.empty());

        Mockito.when(mockUss.get(gp1)).thenReturn(Optional.of(u1));
        Mockito.when(mockUss.get(gp2)).thenReturn(Optional.of(u2));
        return mockUss;
    }

    private CommandSource mockSource() {
        CommandSource mock = Mockito.mock(CommandSource.class);
        Mockito.when(mock.hasPermission(Mockito.any())).thenReturn(true);
        return mock;
    }
}

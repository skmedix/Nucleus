/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.commandspy.listeners;

import com.google.common.collect.Sets;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.dataservices.loaders.UserDataManager;
import io.github.nucleuspowered.nucleus.internal.CommandPermissionHandler;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.annotations.ConditionalListener;
import io.github.nucleuspowered.nucleus.internal.text.TextParsingUtils;
import io.github.nucleuspowered.nucleus.modules.commandspy.CommandSpyModule;
import io.github.nucleuspowered.nucleus.modules.commandspy.commands.CommandSpyCommand;
import io.github.nucleuspowered.nucleus.modules.commandspy.config.CommandSpyConfig;
import io.github.nucleuspowered.nucleus.modules.commandspy.config.CommandSpyConfigAdapter;
import io.github.nucleuspowered.nucleus.modules.commandspy.datamodules.CommandSpyUserDataModule;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandMapping;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.text.Text;
import uk.co.drnaylor.quickstart.exceptions.IncorrectAdapterTypeException;
import uk.co.drnaylor.quickstart.exceptions.NoModuleException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;

@SuppressWarnings("ALL")
@ConditionalListener(CommandSpyListener.Test.class)
public class CommandSpyListener extends ListenerBase.Reloadable {

    private CommandPermissionHandler permissionHandler =
            Nucleus.getNucleus().getPermissionRegistry().getPermissionsForNucleusCommand(CommandSpyCommand.class);

    private CommandSpyConfig config;
    private final UserDataManager dataManager;
    private final TextParsingUtils textParsingUtils;
    private boolean listIsEmpty = true;

    @Inject
    public CommandSpyListener(UserDataManager dataManager, TextParsingUtils textParsingUtils) {
        this.dataManager = dataManager;
        this.textParsingUtils = textParsingUtils;
    }

    @Listener(order = Order.LAST)
    public void onCommand(SendCommandEvent event, @Root Player player) {

        if (!permissionHandler.testSuffix(player, "exempt.target")) {
            boolean isInList;
            if (!this.listIsEmpty) {
                String command = event.getCommand().toLowerCase();
                Optional<? extends CommandMapping> oc = Sponge.getCommandManager().get(command, player);
                Set<String> cmd;

                // If the command exists, then get all aliases.
                cmd = oc.map(commandMapping -> commandMapping.getAllAliases().stream().map(String::toLowerCase).collect(Collectors.toSet()))
                        .orElseGet(() -> Sets.newHashSet(command));
                isInList = config.getCommands().stream().map(String::toLowerCase).anyMatch(cmd::contains);
            } else {
                isInList = true;
            }

            // If the command is in the list, report it.
            if (isInList == this.config.isUseWhitelist()) {
                List<Player> playerList = Sponge.getServer().getOnlinePlayers()
                    .stream()
                    .filter(x -> !x.getUniqueId().equals(player.getUniqueId()))
                    .filter(x -> permissionHandler.testBase(x))
                    .filter(x -> dataManager.get(x).get().quickGet(CommandSpyUserDataModule.class, CommandSpyUserDataModule::isCommandSpy))
                    .collect(Collectors.toList());

                if (!playerList.isEmpty()) {
                    Text prefix = config.getTemplate().getForCommandSource(player);
                    TextParsingUtils.StyleTuple st = textParsingUtils.getLastColourAndStyle(prefix, null);
                    Text messageToSend = prefix
                        .toBuilder().append(Text.of(st.colour, st.style, "/" + event.getCommand() + " " + event.getArguments())).build();
                    playerList.forEach(x -> x.sendMessage(messageToSend));
                }
            }
        }
    }

    @Override public void onReload() throws Exception {
        this.config = this.plugin.getModuleContainer().getConfigAdapterForModule(CommandSpyModule.ID, CommandSpyConfigAdapter.class)
            .getNodeOrDefault();
        this.listIsEmpty = this.config.getCommands().isEmpty();
    }

    public static class Test implements Predicate<Nucleus> {

        @Override public boolean test(Nucleus nucleus) {
            try {
                CommandSpyConfig csc = nucleus.getModuleContainer().getConfigAdapterForModule(CommandSpyModule.ID, CommandSpyConfigAdapter.class)
                    .getNodeOrDefault();
                return !csc.isUseWhitelist() || !csc.getCommands().isEmpty();
            } catch (NoModuleException | IncorrectAdapterTypeException e) {
                if (nucleus.isDebugMode()) {
                    e.printStackTrace();
                }

                return false;
            }
        }
    }
}

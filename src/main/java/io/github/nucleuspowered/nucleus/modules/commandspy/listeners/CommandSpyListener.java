/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.commandspy.listeners;

import com.google.common.collect.Sets;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.CommandPermissionHandler;
import io.github.nucleuspowered.nucleus.internal.ListenerBase;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
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

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class CommandSpyListener implements Reloadable, ListenerBase.Conditional {

    private final String basePermission;
    private final String exemptTarget;
    private CommandSpyConfig config = new CommandSpyConfig();
    private boolean listIsEmpty = true;

    public CommandSpyListener() {
        CommandPermissionHandler permissionHandler =
                Nucleus.getNucleus().getPermissionRegistry().getPermissionsForNucleusCommand(CommandSpyCommand.class);
        this.basePermission = permissionHandler.getBase();
        this.exemptTarget = permissionHandler.getPermissionWithSuffix("exempt.target");
    }

    @Listener(order = Order.LAST)
    public void onCommand(SendCommandEvent event, @Root Player player) {

        if (!hasPermission(player, this.exemptTarget)) {
            boolean isInList = false;
            if (!this.listIsEmpty) {
                String command = event.getCommand().toLowerCase();
                Optional<? extends CommandMapping> oc = Sponge.getCommandManager().get(command, player);
                Set<String> cmd;

                // If the command exists, then get all aliases.
                cmd = oc.map(commandMapping -> commandMapping.getAllAliases().stream().map(String::toLowerCase).collect(Collectors.toSet()))
                        .orElseGet(() -> Sets.newHashSet(command));
                isInList = this.config.getCommands().stream().map(String::toLowerCase).anyMatch(cmd::contains);
            }

            // If the command is in the list, report it.
            if (isInList == this.config.isUseWhitelist()) {
                List<Player> playerList = Sponge.getServer().getOnlinePlayers()
                    .stream()
                    .filter(x -> !x.getUniqueId().equals(player.getUniqueId()))
                    .filter(x -> hasPermission(x, this.basePermission))
                    .filter(x -> Nucleus.getNucleus().getUserDataManager().getUnchecked(x).get(CommandSpyUserDataModule.class).isCommandSpy())
                    .collect(Collectors.toList());

                if (!playerList.isEmpty()) {
                    Text prefix = this.config.getTemplate().getForCommandSource(player);
                    TextParsingUtils.StyleTuple st = TextParsingUtils.getLastColourAndStyle(prefix, null);
                    Text messageToSend = prefix
                            .toBuilder()
                            .append(Text.of(st.colour, st.style, "/", event.getCommand(), Util.SPACE, event.getArguments())).build();
                    playerList.forEach(x -> x.sendMessage(messageToSend));
                }
            }
        }
    }

    @Override
    public void onReload() throws Exception {
        this.config = Nucleus.getNucleus().getModuleContainer().getConfigAdapterForModule(CommandSpyModule.ID, CommandSpyConfigAdapter.class)
            .getNodeOrDefault();
        this.listIsEmpty = this.config.getCommands().isEmpty();
    }

    @Override
    public boolean shouldEnable() {
        return !this.config.isUseWhitelist() || !this.listIsEmpty;
    }
}

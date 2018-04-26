/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.world.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.UUID;
import java.util.function.Supplier;

@NonnullByDefault
@NoModifiers
@Permissions(prefix = "world", suggestedLevel = SuggestedLevel.OWNER)
@RegisterCommand(value = {"clone", "copy"}, subcommandOf = WorldCommand.class)
public class CloneWorldCommand extends AbstractCommand<CommandSource> {

    private final String newKey = "new name";

    @Override protected CommandElement[] getArguments() {
        return new CommandElement[] {
                NucleusParameters.WORLD_PROPERTIES_ALL,
                GenericArguments.string(Text.of(this.newKey))
        };
    }

    @Override
    protected CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        WorldProperties worldToCopy = args.<WorldProperties>getOne(NucleusParameters.Keys.WORLD).get();
        final String oldName = worldToCopy.getWorldName();
        final String newName = args.<String>getOne(this.newKey).get();
        if (Sponge.getServer().getWorldProperties(newName).isPresent()) {
            throw ReturnMessageException.fromKey("command.world.clone.alreadyexists", newName);
        }

        Text message = Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.world.clone.starting",
                oldName, newName);
        src.sendMessage(message);
        if (!(src instanceof ConsoleSource)) {
            Sponge.getServer().getConsole().sendMessage(message);
        }

        // Well, you never know, the player might die or disconnect - we have to be vigilant.
        final Supplier<MessageReceiver> mr;
        if (src instanceof Player) {
            UUID uuid = ((Player) src).getUniqueId();
            mr = () -> Sponge.getServer().getPlayer(uuid).map(x -> (MessageReceiver) x).orElseGet(() -> new MessageReceiver() {
                @Override public void sendMessage(Text message) {

                }

                @Override public MessageChannel getMessageChannel() {
                    return MessageChannel.TO_NONE;
                }

                @Override public void setMessageChannel(MessageChannel channel) {

                }
            });
        } else {
            mr = () -> src;
        }

        Sponge.getServer().copyWorld(worldToCopy, newName).handle((result, ex) -> {

            MessageReceiver m = mr.get();
            Text msg;
            if (ex == null && result.isPresent()) {
                msg = Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.world.clone.success", oldName, newName);
            } else {
                msg = Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.world.clone.failed", oldName, newName);
            }

            m.sendMessage(msg);
            if (!(m instanceof ConsoleSource)) {
                Sponge.getServer().getConsole().sendMessage(msg);
            }

            return result;
        });

        return CommandResult.success();
    }
}

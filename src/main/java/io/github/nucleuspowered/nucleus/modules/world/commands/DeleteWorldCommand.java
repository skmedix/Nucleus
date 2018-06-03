/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.world.commands;

import com.google.common.base.Preconditions;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.util.Tuples;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.storage.WorldProperties;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import javax.annotation.Nullable;

@NoModifiers
@Permissions(prefix = "world", suggestedLevel = SuggestedLevel.OWNER)
@RegisterCommand(value = {"delete", "del"}, subcommandOf = WorldCommand.class)
@NonnullByDefault
public class DeleteWorldCommand extends AbstractCommand<CommandSource> {

    @Nullable private Tuples.Quad<Instant, UUID, WorldProperties, Path> confirm = null;

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                NucleusParameters.WORLD_PROPERTIES_ALL,
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        WorldProperties properties = args.<WorldProperties>getOne(NucleusParameters.Keys.WORLD).get();
        if (this.confirm != null && this.confirm.getFirst().isAfter(Instant.now()) && this.confirm
                .getSecond().equals(Util.getUUID(src)) && this.confirm.getThird().getUniqueId().equals(properties.getUniqueId())) {
            try {
                completeDeletion(src, properties);
            } finally {
                this.confirm = null;
            }

            return CommandResult.success();
        }

        this.confirm = null;
        runChecks(properties);

        // Scary warning.
        Path path = Nucleus.getNucleus().getDataPath().getParent().resolve("world").resolve(properties.getWorldName());
        if (Files.exists(path)) {
            this.confirm = Tuples.of(Instant.now().plus(30, ChronoUnit.SECONDS), Util.getUUID(src), properties, path);
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.world.delete.warning1", properties.getWorldName()));
            src.sendMessage(
                    Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.world.delete.warning2", path.toAbsolutePath().toString()));
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.world.delete.warning3", properties.getWorldName()));
            return CommandResult.success();
        } else {
            throw new ReturnMessageException(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.world.delete.notfound", properties.getWorldName()));
        }
    }

    private void completeDeletion(CommandSource src, WorldProperties properties) throws Exception {
        Preconditions.checkNotNull(this.confirm);
        String worldName = this.confirm.getThird().getWorldName();
        runChecks(this.confirm.getThird());

        final ConsoleSource consoleSource = Sponge.getServer().getConsole();
        sendMessageTo(src, "command.world.delete.confirmed", worldName);
        if (src != consoleSource) {
            sendMessageTo(consoleSource, "command.world.delete.confirmed", worldName);
        }

        // Now request deletion
        CompletableFuture<Boolean> completableFuture = Sponge.getServer().deleteWorld(properties);
        final Supplier<Optional<? extends CommandSource>> source;
        if (src instanceof Player) {
            final UUID uuid = ((Player) src).getUniqueId();
            source = () -> Sponge.getServer().getPlayer(uuid);
        } else {
            source = Optional::empty;
        }

        Task.builder().async()
                .execute(task -> {
                    boolean result;
                    try {
                        result = completableFuture.get();
                    } catch (InterruptedException | ExecutionException e) {
                        result = false;
                        e.printStackTrace();
                    }

                    if (!result) {
                        source.get().ifPresent(x -> {
                            sendMessageTo(x, "command.world.delete.complete.error", worldName);
                        });

                        sendMessageTo(consoleSource, "command.world.delete.complete.error", worldName);
                    } else {
                        source.get().ifPresent(x -> sendMessageTo(x, "command.world.delete.complete.success", worldName));
                        sendMessageTo(consoleSource, "command.world.delete.complete.success", worldName);
                    }
                }).submit(Nucleus.getNucleus());

    }

    private static void runChecks(WorldProperties properties) throws ReturnMessageException {
        if (Sponge.getServer().getWorld(properties.getUniqueId()).isPresent()) {
            throw new ReturnMessageException(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.world.delete.loaded", properties.getWorldName()));
        }
    }

}

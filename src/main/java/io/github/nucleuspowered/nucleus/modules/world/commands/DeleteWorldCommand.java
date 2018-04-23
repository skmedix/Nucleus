/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.world.commands;

import com.google.common.base.Preconditions;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.argumentparsers.NucleusWorldPropertiesArgument;
import io.github.nucleuspowered.nucleus.internal.annotations.RequiresPlatform;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.util.Tuples;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.storage.WorldProperties;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import javax.annotation.Nullable;

@NoModifiers
@RunAsync
@RequiresPlatform
@Permissions(prefix = "world", suggestedLevel = SuggestedLevel.OWNER)
@RegisterCommand(value = {"delete", "del"}, subcommandOf = WorldCommand.class)
@NonnullByDefault
public class DeleteWorldCommand extends AbstractCommand<CommandSource> {

    @Nullable private Tuples.Quad<Instant, UUID, WorldProperties, Path> confirm = null;
    private final String world = "world";

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.onlyOne(new NucleusWorldPropertiesArgument(Text.of(this.world), NucleusWorldPropertiesArgument.Type.ALL)),
        };
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        WorldProperties properties = args.<WorldProperties>getOne(this.world).get();
        if (!properties.isEnabled()) {
            throw ReturnMessageException.fromKey("args.worldproperties.noexistdisabled", properties.getWorldName());
        }

        if (this.confirm != null && this.confirm.getFirst().isAfter(Instant.now()) && this.confirm
                .getSecond().equals(Util.getUUID(src)) && this.confirm.getThird().getUniqueId().equals(properties.getUniqueId())) {
            try {
                completeDeletion(src);
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

    private void completeDeletion(CommandSource src) throws Exception {
        Preconditions.checkNotNull(this.confirm);
        String worldName = this.confirm.getThird().getWorldName();
        Path path = this.confirm.getFourth();
        runChecks(this.confirm.getThird());

        src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.world.delete.confirmed", worldName));

        DeletionWalker d = new DeletionWalker();
        Files.walkFileTree(path, d);

        if (d.error) {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.world.delete.complete.error", worldName));
        } else {
            Text message = Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.world.delete.complete.success", worldName);
            src.sendMessage(message);
            Nucleus.getNucleus().getLogger().info(message.toPlain());
        }
    }

    private static void runChecks(WorldProperties properties) throws ReturnMessageException {
        if (Sponge.getServer().getWorld(properties.getUniqueId()).isPresent()) {
            throw new ReturnMessageException(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.world.delete.loaded", properties.getWorldName()));
        }

        if (properties.isEnabled()) {
            throw new ReturnMessageException(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.world.delete.enabled", properties.getWorldName()));
        }
    }

    private class DeletionWalker implements FileVisitor<Path> {

        private boolean error = false;

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            Nucleus.getNucleus().getLogger().warn(
                    Nucleus.getNucleus().getMessageProvider().getMessageWithFormat("command.world.delete.unabletoremove", file.toString()));
            this.error = true;
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            if (exc == null) {
                Files.delete(dir);
            }

            return FileVisitResult.CONTINUE;
        }
    }
}

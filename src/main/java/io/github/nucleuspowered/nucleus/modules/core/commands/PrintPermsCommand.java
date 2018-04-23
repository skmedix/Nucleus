/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.core.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@RunAsync
@NoModifiers
@Permissions(prefix = "nucleus")
@RegisterCommand(value = "printperms", subcommandOf = NucleusCommand.class)
@NonnullByDefault
public class PrintPermsCommand extends AbstractCommand<CommandSource> {

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Map<String, PermissionInformation> l = Nucleus.getNucleus().getPermissionRegistry().getPermissions();
        List<String> notsuggested =
                l.entrySet().stream().filter(x -> x.getValue().level == SuggestedLevel.NONE).map(Map.Entry::getKey).collect(Collectors.toList());
        List<String> owner =
                l.entrySet().stream().filter(x -> x.getValue().level == SuggestedLevel.NONE).map(Map.Entry::getKey).collect(Collectors.toList());
        List<String> admin =
                l.entrySet().stream().filter(x -> x.getValue().level == SuggestedLevel.ADMIN).map(Map.Entry::getKey).collect(Collectors.toList());
        List<String> mod =
                l.entrySet().stream().filter(x -> x.getValue().level == SuggestedLevel.MOD).map(Map.Entry::getKey).collect(Collectors.toList());
        List<String> user =
                l.entrySet().stream().filter(x -> x.getValue().level == SuggestedLevel.USER).map(Map.Entry::getKey).collect(Collectors.toList());

        String file = "plugin-perms.txt";
        BufferedWriter f = new BufferedWriter(new FileWriter(file));
        Consumer<String> permWriter = x -> {
            try {
                f.write(x);
                f.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        f.write("Not Suggested");
        f.write("-----");
        f.newLine();
        notsuggested.stream().sorted().forEach(permWriter);
        f.newLine();

        f.write("Owner");
        f.write("-----");
        f.newLine();
        owner.stream().sorted().forEach(permWriter);
        f.newLine();

        f.write("Admin");
        f.write("-----");
        f.newLine();

        admin.stream().sorted().forEach(permWriter);
        f.newLine();
        f.write("Mod");
        f.write("-----");
        f.newLine();

        mod.stream().sorted().forEach(permWriter);
        f.newLine();
        f.write("User");
        f.write("-----");
        f.newLine();

        user.stream().sorted().forEach(permWriter);
        f.flush();
        f.close();

        src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.printperms", file));
        return CommandResult.success();
    }
}

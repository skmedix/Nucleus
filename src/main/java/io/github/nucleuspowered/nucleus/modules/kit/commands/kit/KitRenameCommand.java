/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.commands.kit;

import io.github.nucleuspowered.nucleus.api.nucleusdata.Kit;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.modules.kit.commands.KitFallbackBase;
import io.github.nucleuspowered.nucleus.modules.kit.handlers.KitHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@Permissions(prefix = "kit")
@RegisterCommand(value = {"rename"}, subcommandOf = KitCommand.class)
@RunAsync
@NoModifiers
@NonnullByDefault
public class KitRenameCommand extends KitFallbackBase<CommandSource> {

    private final String name = "target name";

    @Override
    protected CommandElement[] getArguments() {
        return new CommandElement[] {
                KitFallbackBase.KIT_PARAMETER_NO_PERM_CHECK,
                GenericArguments.onlyOne(GenericArguments.string(Text.of(this.name)))
        };
    }

    @Override
    protected CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        try {
            String name1 = args.<Kit>getOne(KitFallbackBase.KIT_PARAMETER_KEY).get().getName();
            String name2 = args.<String>getOne(this.name).get();
            getServiceUnchecked(KitHandler.class).renameKit(name1, name2);
            sendMessageTo(src, "command.kit.rename.renamed", name1, name2);
            return CommandResult.success();
        } catch (IllegalArgumentException e) {
            throw new ReturnMessageException(Text.of(TextColors.RED, e.getMessage()));
        }
    }
}

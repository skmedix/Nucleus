/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.core.commands;

import static io.github.nucleuspowered.nucleus.PluginInfo.GIT_HASH;
import static io.github.nucleuspowered.nucleus.PluginInfo.NAME;
import static io.github.nucleuspowered.nucleus.PluginInfo.VERSION;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoCommandPrefix;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import uk.co.drnaylor.quickstart.ModuleContainer;

import java.util.Set;

import javax.annotation.Nullable;

@RunAsync
@Permissions
@NoModifiers
@RegisterCommand({ "nucleus" })
@NoCommandPrefix
@NonnullByDefault
public class NucleusCommand extends AbstractCommand<CommandSource> {

    private final Text version = Text.of(TextColors.GREEN, NAME + " version " + VERSION + " (built from commit " + GIT_HASH + ")");
    @Nullable private Text modules = null;

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) {
        if (this.modules == null) {
            Text.Builder tb = Text.builder("Modules: ").color(TextColors.GREEN);

            boolean addComma = false;
            Set<String> enabled = Nucleus.getNucleus().getModuleContainer().getModules();
            for (String module : Nucleus.getNucleus().getModuleContainer().getModules(ModuleContainer.ModuleStatusTristate.ALL)) {
                if (addComma) {
                    tb.append(Text.of(TextColors.GREEN, ", "));
                }

                tb.append(Text.of(enabled.contains(module) ? TextColors.GREEN : TextColors.RED, module));
                addComma = true;
            }

            this.modules = tb.append(Text.of(TextColors.GREEN, ".")).build();
        }

        src.sendMessage(this.version);
        src.sendMessage(this.modules);
        return CommandResult.success();
    }
}

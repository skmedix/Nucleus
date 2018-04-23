/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.rules.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.interfaces.Reloadable;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.rules.RulesModule;
import io.github.nucleuspowered.nucleus.modules.rules.config.RulesConfig;
import io.github.nucleuspowered.nucleus.modules.rules.config.RulesConfigAdapter;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.annotation.NonnullByDefault;

@Permissions(suggestedLevel = SuggestedLevel.USER)
@RunAsync
@RegisterCommand("rules")
@NoModifiers
@NonnullByDefault
@EssentialsEquivalent("rules")
public class RulesCommand extends AbstractCommand<CommandSource> implements Reloadable {

    private Text title = Text.EMPTY;

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        Nucleus.getNucleus().getTextFileController(RulesModule.RULES_KEY)
                .orElseThrow(() -> ReturnMessageException.fromKey("command.rules.empty"))
                .sendToPlayer(src, this.title);
        return CommandResult.success();
    }

    @Override public void onReload() {
        RulesConfig config = getServiceUnchecked(RulesConfigAdapter.class).getNodeOrDefault();
        String title = config.getRulesTitle();
        if (title.isEmpty()) {
            this.title = Text.EMPTY;
        } else {
            this.title = TextSerializers.FORMATTING_CODE.deserialize(title);
        }
    }
}

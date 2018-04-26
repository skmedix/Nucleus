/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.warp.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.nucleusdata.Warp;
import io.github.nucleuspowered.nucleus.api.nucleusdata.WarpCategory;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.modules.warp.WarpParameters;
import io.github.nucleuspowered.nucleus.modules.warp.handlers.WarpHandler;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@RunAsync
@NoModifiers
@NonnullByDefault
@Permissions(prefix = "warp")
@RegisterCommand(value = {"setcategory"}, subcommandOf = WarpCommand.class)
public class SetCategoryCommand extends AbstractCommand<CommandSource> {

    private final String categoryKey = "category";
    private final WarpHandler handler = getServiceUnchecked(WarpHandler.class);

    @Override public CommandElement[] getArguments() {
        return new CommandElement[] {
            GenericArguments.flags().flag("r", "-remove", "-delete").flag("n", "-new").buildWith(
                GenericArguments.seq(
                        WarpParameters.WARP_NO_PERM,
                        GenericArguments.optional(new SetCategoryWarpCategoryArgument (Text.of(this.categoryKey)))
                )
            )
        };
    }

    @Override public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        String warpName = args.<Warp>getOne(WarpParameters.WARP_KEY).get().getName();
        if (args.hasAny("r")) {
            // Remove the category.
            if (this.handler.setWarpCategory(warpName, null)) {
                src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.warp.category.removed", warpName));
                return CommandResult.success();
            }

            throw new ReturnMessageException(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.warp.category.noremove", warpName));
        }

        Optional<Tuple<String, Boolean>> categoryOp = args.getOne(this.categoryKey);
        if (!categoryOp.isPresent()) {
            throw new ReturnMessageException(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.warp.category.required"));
        }

        Tuple<String, Boolean> category = categoryOp.get();
        if (!args.hasAny("n") && !category.getSecond()) {
            src.sendMessage(
                    Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.warp.category.requirenew", category.getFirst())
                    .toBuilder().onClick(TextActions.runCommand("/warp setcategory -n " + warpName + " " + category.getFirst())).build()
            );

            return CommandResult.empty();
        }

        // Add the category.
        if (this.handler.setWarpCategory(warpName, category.getFirst())) {
            src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.warp.category.added", category.getFirst(), warpName));
            return CommandResult.success();
        }

        WarpCategory c = this.handler.getWarpCategoryOrDefault(category.getFirst());
        throw new ReturnMessageException(Nucleus.getNucleus().getMessageProvider().getTextMessageWithTextFormat("command.warp.category.couldnotadd",
                c.getDisplayName(), Text.of(warpName)));
    }

    private class SetCategoryWarpCategoryArgument extends CommandElement {

        public SetCategoryWarpCategoryArgument (@Nullable Text key) {
            super(key);
        }

        @Nullable @Override protected Object parseValue(@Nonnull CommandSource source, @Nonnull CommandArgs args) throws ArgumentParseException {
            String arg = args.next();
            return Tuple.of(arg, SetCategoryCommand.this.handler
                    .getWarpsWithCategories().keySet().stream().filter(Objects::nonNull).anyMatch(x -> x.getId().equals(arg)));
        }

        @Nonnull @Override public List<String> complete(@Nonnull CommandSource src, @Nonnull CommandArgs args, @Nonnull CommandContext context) {
            return SetCategoryCommand.this.handler.getWarpsWithCategories().keySet().stream().filter(Objects::isNull).map(WarpCategory::getId).collect(Collectors.toList());
        }
    }
}

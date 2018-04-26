/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.commands;

import com.google.common.collect.ImmutableList;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.argumentparsers.KitArgument;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.modules.kit.handlers.KitHandler;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

public abstract class KitFallbackBase<T extends CommandSource> extends AbstractCommand<T> {

    private ImmutableList<String> aliasesForKits = ImmutableList.of();

    protected final static String KIT_PARAMETER_KEY = "kit";
    protected final static CommandElement KIT_PARAMETER_PERM_CHECK = GenericArguments.onlyOne(new KitArgument(Text.of(KIT_PARAMETER_KEY), true));
    protected final static CommandElement KIT_PARAMETER_NO_PERM_CHECK = GenericArguments.onlyOne(new KitArgument(Text.of(KIT_PARAMETER_KEY), false));
    protected final static KitHandler KIT_HANDLER = Nucleus.getNucleus()
            .getInternalServiceManager().getServiceUnchecked(KitHandler.class);

    @Override
    protected void afterPostInit() {
        super.afterPostInit();

        this.aliasesForKits = ImmutableList.copyOf(this.getAliases());
    }

    @NonnullByDefault
    @Override
    protected boolean allowFallback(CommandSource source, CommandArgs args, CommandContext context) {
        if (context.hasAny(KIT_PARAMETER_KEY)
                || this.aliasesForKits.stream().noneMatch(x -> KIT_HANDLER.exists(x, true))) {
            return false;
        }
        return super.allowFallback(source, args, context);
    }


}

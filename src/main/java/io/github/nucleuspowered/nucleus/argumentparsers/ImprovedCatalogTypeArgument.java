/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.argumentparsers;

import io.github.nucleuspowered.nucleus.Nucleus;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.GameRegistry;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@NonnullByDefault
public class ImprovedCatalogTypeArgument extends CommandElement {

    private final Class<? extends CatalogType> type;

    public ImprovedCatalogTypeArgument(@Nonnull Text key, Class<? extends CatalogType> type) {
        super(key);
        this.type = type;
    }

    @Nullable
    @Override
    protected Object parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException {
        String arg = args.next().toLowerCase();

        // Try
        GameRegistry registry = Sponge.getRegistry();
        Optional<? extends CatalogType> catalogType = registry.getType(this.type, arg);
        if (!catalogType.isPresent() && !arg.contains(":")) {
            catalogType = registry.getType(this.type, "minecraft:" + arg);
            if (!catalogType.isPresent()) {
                catalogType = registry.getType(this.type, "sponge:" + arg);
            }
        }

        if (catalogType.isPresent()) {
            return catalogType.get();
        }

        throw args.createError(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("args.catalogtype.nomatch", arg));
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context) {
        try {
            String arg = args.peek().toLowerCase();
            return Sponge.getRegistry().getAllOf(this.type).stream()
                    .filter(x -> x.getId().startsWith(arg) || x.getId().startsWith("minecraft:" + arg) || x.getId().startsWith("sponge:" + arg))
                    .map(CatalogType::getId).collect(Collectors.toList());
        } catch (Exception e) {
            return Sponge.getRegistry().getAllOf(this.type).stream().map(CatalogType::getId).collect(Collectors.toList());
        }
    }
}

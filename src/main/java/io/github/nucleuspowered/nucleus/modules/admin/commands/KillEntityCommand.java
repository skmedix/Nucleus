/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.admin.commands;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.Sets;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.monster.Monster;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.storage.WorldProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Permissions
@RegisterCommand("killentity")
@NonnullByDefault
public class KillEntityCommand extends AbstractCommand<CommandSource> {

    private static final String radius = "radius";
    private static final String world = "world";
    private static final String type = "type";

    private static final Predicate<Entity> armourStand = e -> e.getType().equals(EntityTypes.ARMOR_STAND);
    private static final Predicate<Entity> hostile = e -> e instanceof Monster;
    private static final Predicate<Entity> passive = e -> e instanceof Living && !(e instanceof Player || e instanceof Monster);

    private final Map<String, ?> map = new HashMap<String, Predicate<Entity>>() {{
        put("armorstand", armourStand);
        put("armourstand", armourStand);
        put("monsters", hostile);
        put("hostile", hostile);
        put("passive", passive);
        put("animal", passive);
        put("item", e -> e instanceof Item);
        put("player", e -> e instanceof Player);
    }};

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                GenericArguments.flags()
                        .setAnchorFlags(true)
                        .valueFlag(GenericArguments.integer(Text.of(radius)), "r")
                        .valueFlag(NucleusParameters.WORLD_PROPERTIES_LOADED_ONLY, "w")
                        .buildWith(GenericArguments.allOf(GenericArguments.choices(Text.of(type), this.map)))
        };
    }

    @Override
    protected CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        if (!(src instanceof Locatable) && args.hasAny(radius)) {
            // We can't do that.
            throw ReturnMessageException.fromKey("command.killentity.commandsourceradius");
        }

        if (args.hasAny(radius) && args.hasAny(world)) {
            // Can't do that, either.
            throw ReturnMessageException.fromKey("command.killentity.radiusworld");
        }

        Set<Entity> currentEntities;
        if (args.hasAny(radius)) {
            Locatable l = ((Locatable) src);
            Vector3d locationTest = l.getLocation().getPosition();
            int r = args.<Integer>getOne(radius).get();
            currentEntities = Sets.newHashSet(l.getWorld().getEntities(entity -> entity.getTransform().getPosition().distance(locationTest) <= r));
        } else {
            WorldProperties worldProperties = this.getWorldFromUserOrArgs(src, world, args);
            currentEntities = Sets.newHashSet(Sponge.getServer().getWorld(worldProperties.getUniqueId()).get().getEntities());
        }

        Predicate<Entity> entityPredicate = args.<Predicate<Entity>>getAll(type).stream().reduce(Predicate::or)
                .orElseThrow(() -> ReturnMessageException.fromKey("command.killentity.noselection"));
        Set<Entity> toKill = currentEntities.stream().filter(entityPredicate).collect(Collectors.toSet());
        if (toKill.isEmpty()) {
            throw ReturnMessageException.fromKey("command.killentity.nothing");
        }

        int killCount = toKill.size();
        toKill.forEach(x -> {
            x.offer(Keys.HEALTH, 0d);
            x.remove();
        });

        src.sendMessage(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.killentity.success", String.valueOf(killCount)));
        return CommandResult.affectedEntities(killCount);
    }
}

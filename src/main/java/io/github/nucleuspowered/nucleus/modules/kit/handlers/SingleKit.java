/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.kit.handlers;

import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.api.nucleusdata.Kit;
import io.github.nucleuspowered.nucleus.configurate.datatypes.KitDataNode;
import io.github.nucleuspowered.nucleus.configurate.wrappers.NucleusItemStackSnapshot;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

@NonnullByDefault
public class SingleKit implements Kit {

    private final String name;
    private final List<ItemStackSnapshot> stacks = Lists.newArrayList();
    private final List<String> commands = Lists.newArrayList();
    @Nullable private Duration interval;
    private double cost = 0;
    private boolean autoRedeem = false;
    private boolean oneTime = false;
    private boolean displayOnRedeem = true;
    private boolean ignoresPermission = false;
    private boolean hidden = false;
    private boolean firstJoin = false;

    public SingleKit(String name) {
        this.name = name;
    }

    public SingleKit(String key, KitDataNode value) {
        this(key);
        value.stacks.stream().map(NucleusItemStackSnapshot::getSnapshot).map(ValueContainer::copy).forEach(this.stacks::add);
        this.commands.addAll(value.commands);
        this.cost = value.cost;
        this.interval = value.interval > 0 ? Duration.of(value.interval, ChronoUnit.SECONDS) : null;
        this.autoRedeem = value.autoRedeem;
        this.oneTime = value.oneTime;
        this.ignoresPermission = value.ignoresPermission;
        this.hidden = value.hidden;
        this.displayOnRedeem = value.displayMessage;
        this.firstJoin = value.firstJoin;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public List<ItemStackSnapshot> getStacks() {
        return Lists.newArrayList(this.stacks);
    }

    @Override
    public Kit setStacks(List<ItemStackSnapshot> stacks) {
        this.stacks.clear();
        this.stacks.addAll(stacks);
        return this;
    }

    @Override
    public Optional<Duration> getCooldown() {
        return Optional.ofNullable(this.interval);
    }

    @Override
    public Kit setCooldown(@Nullable Duration interval) {
        this.interval = interval;
        return this;
    }

    @Override
    public double getCost() {
        return Math.max(0, this.cost);
    }

    @Override
    public Kit setCost(double cost) {
        this.cost = Math.max(0, cost);
        return this;
    }

    @Override
    public boolean isAutoRedeem() {
        return this.autoRedeem;
    }

    @Override
    public Kit setAutoRedeem(boolean autoRedeem) {
        this.autoRedeem = autoRedeem;
        return this;
    }

    @Override
    public boolean isOneTime() {
        return this.oneTime;
    }

    @Override
    public Kit setOneTime(boolean oneTime) {
        this.oneTime = oneTime;
        return this;
    }

    @Override
    public List<String> getCommands() {
        return Lists.newArrayList(this.commands);
    }

    @Override
    public Kit setCommands(List<String> commands) {
        this.commands.clear();
        this.commands.addAll(commands);
        return this;
    }

    @Override
    public Kit updateKitInventory(Inventory inventory) {
        List<Inventory> slots = Lists.newArrayList(inventory.slots());
        final List<ItemStackSnapshot> stacks = slots.stream()
                .filter(x -> x.peek().isPresent() && x.peek().get().getItem() != ItemTypes.NONE)
                .map(x -> x.peek().get().createSnapshot()).collect(Collectors.toList());

        // Add all the stacks into the kit list.
        return setStacks(stacks);
    }

    @Override
    public Kit updateKitInventory(Player player) {
        return updateKitInventory(Util.getStandardInventory(player));
    }

    @Override
    public void redeemKitCommands(Player player) {
        ConsoleSource source = Sponge.getServer().getConsole();
        String playerName = player.getName();
        getCommands().forEach(x -> Sponge.getCommandManager().process(source, x.replace("{{player}}", playerName)));
    }

    @Override
    public boolean isDisplayMessageOnRedeem() {
        return this.displayOnRedeem;
    }

    @Override
    public Kit setDisplayMessageOnRedeem(boolean displayMessage) {
        this.displayOnRedeem = displayMessage;
        return this;
    }

    @Override
    public boolean ignoresPermission() {
        return this.ignoresPermission ;
    }

    @Override
    public Kit setIgnoresPermission(boolean ignoresPermission) {
        this.ignoresPermission = ignoresPermission;
        return this;
    }

    @Override
    public boolean isHiddenFromList() {
        return this.hidden;
    }

    @Override
    public Kit setHiddenFromList(boolean hide) {
        this.hidden = hide;
        return this;
    }

    @Override
    public boolean isFirstJoinKit() {
        return this.firstJoin;
    }

    @Override
    public Kit setFirstJoinKit(boolean firstJoinKit) {
        this.firstJoin = firstJoinKit;
        return this;
    }

}

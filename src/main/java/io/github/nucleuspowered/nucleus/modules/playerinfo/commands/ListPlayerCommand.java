/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.playerinfo.commands;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.Util;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.StandardAbstractCommand;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.afk.handlers.AFKHandler;
import io.github.nucleuspowered.nucleus.modules.playerinfo.config.ListConfig;
import io.github.nucleuspowered.nucleus.modules.playerinfo.config.PlayerInfoConfigAdapter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.context.Contextual;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.Subject;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.inject.Inject;

@NonnullByDefault
@RunAsync
@Permissions(suggestedLevel = SuggestedLevel.USER)
@RegisterCommand({"list", "listplayers", "ls"})
@EssentialsEquivalent({"list", "who", "playerlist", "online", "plist"})
public class ListPlayerCommand extends AbstractCommand<CommandSource> implements StandardAbstractCommand.Reloadable {

    @Nullable private AFKHandler handler;

    @SuppressWarnings("NullableProblems") @Inject private PlayerInfoConfigAdapter configAdapter;

    @SuppressWarnings("ConstantConditions")
    private ListConfig listConfig = null;

    @Nullable private Text afk = null;
    @Nullable private Text hidden = null;

    public static final Function<Subject, Integer> weightingFunction = s -> Util.getIntOptionFromSubject(s, "nucleus.list.weight").orElse(0);

    private void init() {
        if (afk == null) {
            afk = plugin.getMessageProvider().getTextMessageWithFormat("command.list.afk");
            hidden = plugin.getMessageProvider().getTextMessageWithFormat("command.list.hidden");
            handler = plugin.getInternalServiceManager().getService(AFKHandler.class).orElse(null);
        }
    }

    @Override
    public Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> m = new HashMap<>();
        m.put("seevanished", PermissionInformation.getWithTranslation("permission.list.seevanished", SuggestedLevel.ADMIN));
        return m;
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        init();
        boolean showVanished = permissions.testSuffix(src, "seevanished");

        Collection<Player> players = Sponge.getServer().getOnlinePlayers();
        long playerCount = players.size();
        long hiddenCount = players.stream().filter(x -> x.get(Keys.VANISH).orElse(false)).count();

        Text header;
        if (showVanished && hiddenCount > 0) {
            header = plugin.getMessageProvider().getTextMessageWithFormat("command.list.playercount.hidden", String.valueOf(playerCount),
                    String.valueOf(Sponge.getServer().getMaxPlayers()), String.valueOf(hiddenCount));
        } else {
            header = plugin.getMessageProvider().getTextMessageWithFormat("command.list.playercount.base", String.valueOf(playerCount - hiddenCount),
                    String.valueOf(Sponge.getServer().getMaxPlayers()));
        }

        src.sendMessage(header);

        Optional<PermissionService> optPermissionService = Sponge.getServiceManager().provide(PermissionService.class);
        if (listConfig.isGroupByPermissionGroup() && optPermissionService.isPresent()) {
            listByPermissionGroup(optPermissionService.get(), players, src, showVanished);
        } else {
            // If we have players, send them on.
            getPlayerList(players, showVanished).ifPresent(src::sendMessage);
        }

        return CommandResult.success();
    }

    private void listByPermissionGroup(final PermissionService service, Collection<Player> players, CommandSource src, boolean showVanished) {
        // Get the groups
        List<Subject> groups = Lists.newArrayList(service.getGroupSubjects().getAllSubjects());

        // If weights are the same, sort them in reverse order - that way we get the most inherited
        // groups first and display them first.
        groups.sort((x, y) -> groupComparison(weightingFunction, x, y));

        // Keep a copy of the players that we will remove from.
        final Map<Player, List<String>> playersToList = players.stream()
            .collect(Collectors.toMap(x -> x, y -> Util.getParentSubjects(y).stream().map(Contextual::getIdentifier).collect(Collectors.toList())));

        // Messages
        final List<Text> messages = Lists.newArrayList();

        final Map<String, List<Player>> groupToPlayer = linkPlayersToGroups(groups, listConfig.getAliases(), playersToList);

        // For the rest of the players...
        if (!playersToList.isEmpty()) {
            groupToPlayer.computeIfAbsent(listConfig.getDefaultGroupName(), g -> Lists.newArrayList()).addAll(playersToList.keySet());
        }

        // Create messages based on the alias list first.
        listConfig.getOrder().forEach(alias -> {
            List<Player> plList = groupToPlayer.get(alias);
            if (plList != null && !plList.isEmpty()) {
                // Get and put the player list into the map, if there is a
                // player to show. There might not be, they might be vanished!
                getPlayerList(plList, showVanished).ifPresent(y ->
                    messages.add(Text.builder().append(Text.of(TextColors.YELLOW, alias + ": ")).append(y).build()));
            }

            groupToPlayer.remove(alias);
        });

        if (listConfig.isUseAliasOnly()) {
            List<Player> playersLeft = groupToPlayer.entrySet().stream().flatMap(x -> x.getValue().stream()).collect(Collectors.toList());
            if (!playersLeft.isEmpty()) {
                getPlayerList(playersLeft, showVanished).ifPresent(y ->
                    messages.add(Text.builder().append(Text.of(TextColors.YELLOW, listConfig.getDefaultGroupName() + ": ")).append(y).build()));
            }
        } else {
            groupToPlayer.entrySet().stream()
                .filter(x -> !x.getValue().isEmpty())
                .filter(x -> !x.getKey().equals(listConfig.getDefaultGroupName()))
                .sorted((x, y) -> x.getKey().compareToIgnoreCase(y.getKey())).forEach(x ->
                    getPlayerList(x.getValue(), showVanished).ifPresent(y ->
                        messages.add(Text.builder().append(Text.of(TextColors.YELLOW, x.getKey() + ": ")).append(y).build()))
            );

            List<Player> pl = groupToPlayer.get(listConfig.getDefaultGroupName());
            if (pl != null && !pl.isEmpty()) {
                getPlayerList(pl, showVanished).ifPresent(y ->
                    messages.add(Text.builder().append(Text.of(TextColors.YELLOW, listConfig.getDefaultGroupName() + ": ")).append(y).build()));
            }
        }

        if (!messages.isEmpty()) {
            src.sendMessages(messages);
        }
    }

    /**
     * Gets {@link Text} that represents the provided player list.
     *
     * @param playersToList The {@link Player}s to list.
     * @param showVanished <code>true</code> if those who are vanished are to be
     *        shown.
     * @return An {@link Optional} of {@link Text} objects, returning
     *         <code>empty</code> if the player list is of zero length.
     */
    private Optional<Text> getPlayerList(Collection<Player> playersToList, boolean showVanished) {
        List<Text> playerList = playersToList.stream().filter(x -> showVanished || !x.get(Keys.VANISH).orElse(false))
                .sorted((x, y) -> x.getName().compareToIgnoreCase(y.getName())).map(x -> {
                    Text.Builder tb = Text.builder();
                    boolean appendSpace = false;
                    if (handler != null && handler.isAfk(x)) {
                        tb.append(afk);
                        appendSpace = true;
                    }

                    if (x.get(Keys.VANISH).orElse(false)) {
                        tb.append(hidden);
                        appendSpace = true;
                    }

                    if (appendSpace) {
                        tb.append(Text.of(" "));
                    }

                    return tb.append(plugin.getNameUtil().getName(x)).build();
                }).collect(Collectors.toList());

        if (!playerList.isEmpty()) {
            boolean isFirst = true;
            Text.Builder tb = Text.builder();
            for (Text text : playerList) {
                if (!isFirst) {
                    tb.append(Text.of(TextColors.WHITE, ", "));
                }

                tb.append(text);
                isFirst = false;
            }

            return Optional.of(tb.build());
        }

        return Optional.empty();
    }

    @Override public void onReload() {
        listConfig = configAdapter.getNodeOrDefault().getList();
    }

    public static Map<String, List<Player>> linkPlayersToGroups(List<Subject> groups, Map<String, String> groupAliases,
           Map<Player, List<String>> players) {

        final Map<String, List<Player>> groupToPlayer = Maps.newHashMap();

        for (Subject x : groups) {
            List<Player> groupPlayerList;
            String groupName = x.getIdentifier();
            if (groupAliases.containsKey(x.getIdentifier())) {
                groupName = groupAliases.get(x.getIdentifier());
            }

            // Get the players in the group.
            Collection<Player> cp = players.entrySet().stream().filter(k -> k.getValue().contains(x.getIdentifier()))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            if (!cp.isEmpty()) {
                groupPlayerList = groupToPlayer.computeIfAbsent(groupName, g -> Lists.newArrayList());
                cp.forEach(players::remove);
                groupPlayerList.addAll(cp);
            }
        }

        return groupToPlayer;
    }

    // For testing
    public static int groupComparison(Function<Subject, Integer> weightingFunction, Subject x, Subject y)  {
        // If the weight of x is bigger than y, x should go first. We therefore need a large x to provide a negative number.
        int res = weightingFunction.apply(y) - weightingFunction.apply(x);
        if (res == 0) {
            // If x is bigger than y, x should go first. We therefore need a large x to provide a negative number,
            // so x is above y.
            return y.getParents().size() - x.getParents().size();
        }

        return res;
    }
}

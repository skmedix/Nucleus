/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.nickname.commands;

import com.google.common.collect.Maps;
import io.github.nucleuspowered.nucleus.NameUtil;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.exceptions.NicknameException;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.messages.MessageProvider;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.nickname.services.NicknameService;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@NonnullByDefault
@RegisterCommand({"nick", "nickname"})
@Permissions
@EssentialsEquivalent(value = {"nick", "nickname"}, isExact = false,
        notes = "To remove a nickname, use '/delnick'")
public class NicknameCommand extends AbstractCommand<CommandSource> {

    private final NicknameService nicknameService = getServiceUnchecked(NicknameService.class);

    private final String playerKey = "subject";
    private final String nickName = "nickname";

    // Order is important here! TODO: Need to de-dup
    private final Map<String, String> permissionToDesc = Maps.newHashMap();

    @Override protected void afterPostInit() {
        super.afterPostInit();

        MessageProvider mp = Nucleus.getNucleus().getMessageProvider();

        String colPerm = this.permissions.getPermissionWithSuffix("colour.");
        String colPerm2 = this.permissions.getPermissionWithSuffix("color.");

        NameUtil.getColours().forEach((key, value) -> {
            this.permissionToDesc.put(colPerm + value.getName(),
                    mp.getMessageWithFormat("permission.nick.colourspec", value.getName().toLowerCase(), key.toString()));
            this.permissionToDesc
                    .put(colPerm2 + value.getName(), mp.getMessageWithFormat("permission.nick.colorspec", value.getName().toLowerCase(), key.toString()));
        });

        String stylePerm = this.permissions.getPermissionWithSuffix("style.");
        NameUtil.getStyleKeys().entrySet().stream().filter(x -> x.getKey() != 'k').forEach((k) -> this.permissionToDesc.put(stylePerm + k.getValue().toLowerCase(),
            mp.getMessageWithFormat("permission.nick.stylespec", k.getValue().toLowerCase(), k.getKey().toString())));
    }

    @Override
    protected Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> m = new HashMap<>();
        m.put("others", PermissionInformation.getWithTranslation("permission.nick.others", SuggestedLevel.ADMIN));
        m.put("colour", PermissionInformation.getWithTranslation("permission.nick.colour", SuggestedLevel.ADMIN, true, false));
        m.put("color", PermissionInformation.getWithTranslation("permission.nick.color", SuggestedLevel.ADMIN));
        m.put("color.<color>", PermissionInformation.getWithTranslation("permission.nick.colorsingle", SuggestedLevel.ADMIN, false, true));
        m.put("style", PermissionInformation.getWithTranslation("permission.nick.style", SuggestedLevel.ADMIN));
        m.put("style.<style>", PermissionInformation.getWithTranslation("permission.nick.stylesingle", SuggestedLevel.ADMIN, false, true));
        m.put("magic", PermissionInformation.getWithTranslation("permission.nick.magic", SuggestedLevel.ADMIN));
        return m;
    }

    @Override
    protected Map<String, PermissionInformation> permissionsToRegister() {
        return this.permissionToDesc.entrySet().stream().collect(Collectors.toMap(
            Map.Entry::getKey, v -> new PermissionInformation(v.getValue(), SuggestedLevel.ADMIN, true, false)));
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                GenericArguments.optionalWeak(GenericArguments.requiringPermission(
                        GenericArguments.onlyOne(GenericArguments.user(Text.of(this.playerKey))), this.permissions.getPermissionWithSuffix("others"))),
                GenericArguments.onlyOne(GenericArguments.string(Text.of(this.nickName)))};
    }

    @Override
    public CommandResult executeCommand(CommandSource src, CommandContext args) throws Exception {
        User pl = this.getUserFromArgs(User.class, src, this.playerKey, args);
        Text name = args.<String>getOne(this.nickName).map(TextSerializers.FORMATTING_CODE::deserialize).get();

        try {
            this.nicknameService.setNick(pl, src, name, false);
        } catch (NicknameException e) {
            throw new ReturnMessageException(e.getTextMessage());
        }

        if (!src.equals(pl)) {
            src.sendMessage(Text.builder().append(
                    Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.nick.success.other", pl.getName()))
                    .append(Text.of(" - ", TextColors.RESET, name)).build());
        }

        return CommandResult.success();
    }

}

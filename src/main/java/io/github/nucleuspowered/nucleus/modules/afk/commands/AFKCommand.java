/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.afk.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.annotations.RunAsync;
import io.github.nucleuspowered.nucleus.internal.annotations.command.NoModifiers;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.ServiceChangeListener;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.afk.handlers.AFKHandler;
import io.github.nucleuspowered.nucleus.util.CauseStackHelper;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.HashMap;
import java.util.Map;

@RegisterCommand({"afk", "away"})
@Permissions(suggestedLevel = SuggestedLevel.USER)
@NoModifiers
@RunAsync
@EssentialsEquivalent({"afk", "away"})
@NonnullByDefault
public class AFKCommand extends AbstractCommand<Player> {

    private final AFKHandler afkHandler = Nucleus.getNucleus().getInternalServiceManager().getServiceUnchecked(AFKHandler.class);
    static final String KICK_EXEMPT_SUFFIX = "exempt.kick";

    @Override
    protected Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> m = new HashMap<>();
        m.put("exempt.toggle", PermissionInformation.getWithTranslation("permission.afk.exempt.toggle", SuggestedLevel.NONE));
        m.put(KICK_EXEMPT_SUFFIX, PermissionInformation.getWithTranslation("permission.afk.exempt.kick", SuggestedLevel.ADMIN));
        m.put("notify", PermissionInformation.getWithTranslation("permission.afk.notify", SuggestedLevel.ADMIN));
        return m;
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        if (!ServiceChangeListener.isOpOnly() && this.permissions.testSuffix(src, "exempt.toggle")) {
            throw ReturnMessageException.fromKey("command.afk.exempt");
        }

        boolean isAFK = this.afkHandler.isAFK(src);

        if (isAFK) {
            this.afkHandler.stageUserActivityUpdate(src);
        } else if (!this.afkHandler.setAfkInternal(src, CauseStackHelper.createCause(src), true)) {
            throw new ReturnMessageException(Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.afk.notset"));
        }

        return CommandResult.success();
    }
}

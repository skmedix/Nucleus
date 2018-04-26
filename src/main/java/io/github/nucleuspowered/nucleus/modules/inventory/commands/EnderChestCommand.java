/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.inventory.commands;

import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.internal.annotations.Since;
import io.github.nucleuspowered.nucleus.internal.annotations.command.Permissions;
import io.github.nucleuspowered.nucleus.internal.annotations.command.RegisterCommand;
import io.github.nucleuspowered.nucleus.internal.command.AbstractCommand;
import io.github.nucleuspowered.nucleus.internal.command.NucleusParameters;
import io.github.nucleuspowered.nucleus.internal.command.ReturnMessageException;
import io.github.nucleuspowered.nucleus.internal.docgen.annotations.EssentialsEquivalent;
import io.github.nucleuspowered.nucleus.internal.permissions.PermissionInformation;
import io.github.nucleuspowered.nucleus.internal.permissions.SuggestedLevel;
import io.github.nucleuspowered.nucleus.modules.inventory.listeners.InvSeeListener;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.Container;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Map;

@NonnullByDefault
@Permissions(supportsOthers = true)
@RegisterCommand({"enderchest", "ec", "echest"})
@Since(minecraftVersion = "1.10.2", spongeApiVersion = "5.0.0", nucleusVersion = "0.13.0")
@EssentialsEquivalent({"enderchest", "echest", "endersee", "ec"})
public class EnderChestCommand extends AbstractCommand<Player> {

    @Override
    protected Map<String, PermissionInformation> permissionSuffixesToRegister() {
        Map<String, PermissionInformation> mspi = super.permissionSuffixesToRegister();
        mspi.put("exempt.target", PermissionInformation.getWithTranslation("permission.enderchest.exempt.inspect", SuggestedLevel.ADMIN));
        mspi.put("exempt.interact", PermissionInformation.getWithTranslation("permission.enderchest.exempt.modify", SuggestedLevel.ADMIN));
        mspi.put("exempt.modify", PermissionInformation.getWithTranslation("permission.enderchest.exempt.modify", SuggestedLevel.ADMIN));
        mspi.put("modify", PermissionInformation.getWithTranslation("permission.enderchest.modify", SuggestedLevel.ADMIN));
        return mspi;
    }

    @Override
    public CommandElement[] getArguments() {
        return new CommandElement[] {
                GenericArguments.optional(
                    GenericArguments.requiringPermission(
                            NucleusParameters.ONE_PLAYER,
                            this.permissions.getPermissionWithSuffix("others")
                    ))
        };
    }

    @Override
    public CommandResult executeCommand(Player src, CommandContext args) throws Exception {
        Player target = args.<Player>getOne(NucleusParameters.Keys.PLAYER).orElse(src);

        if (!target.getUniqueId().equals(src.getUniqueId())) {
            if (this.permissions.testSuffix(target, "exempt.target")) {
                throw new ReturnMessageException(
                        Nucleus.getNucleus().getMessageProvider().getTextMessageWithFormat("command.enderchest.targetexempt", target.getName()));
            }

            Container container = src.openInventory(target.getEnderChestInventory())
                        .orElseThrow(() -> ReturnMessageException.fromKey("command.invsee.failed"));

            if (this.permissions.testSuffix(target, "exempt.modify") ||
                this.permissions.testSuffix(target, "exempt.interact") || !this.permissions.testSuffix(src, "modify")) {

                InvSeeListener.addEntry(src.getUniqueId(), container);
            }
        } else {
            src.openInventory(src.getEnderChestInventory()).orElseThrow(() -> ReturnMessageException.fromKey("command.invsee.failed"));
        }

        return CommandResult.success();
    }

}

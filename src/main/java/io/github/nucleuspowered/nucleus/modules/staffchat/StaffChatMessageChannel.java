/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.staffchat;

import com.google.common.base.Preconditions;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.api.chat.NucleusChatChannel;
import io.github.nucleuspowered.nucleus.internal.text.NucleusTextTemplateImpl;
import io.github.nucleuspowered.nucleus.internal.traits.PermissionTrait;
import io.github.nucleuspowered.nucleus.modules.staffchat.commands.StaffChatCommand;
import io.github.nucleuspowered.nucleus.modules.staffchat.config.StaffChatConfigAdapter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.ProxySource;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.service.permission.SubjectReference;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.chat.ChatType;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StaffChatMessageChannel implements NucleusChatChannel.StaffChat, PermissionTrait {

    private static StaffChatMessageChannel INSTANCE = null;

    private boolean formatting = false;

    public static StaffChatMessageChannel getInstance() {
        Preconditions.checkState(INSTANCE != null, "StaffChatMessageChannel#Instance");
        return INSTANCE;
    }

    private final String basePerm;
    private NucleusTextTemplateImpl template;
    private TextColor colour;

    StaffChatMessageChannel() {
        Nucleus.getNucleus().registerReloadable(this::onReload);
        this.onReload();
        this.basePerm = Nucleus.getNucleus().getPermissionRegistry().getPermissionsForNucleusCommand(StaffChatCommand.class).getBase();
        INSTANCE = this;
    }

    @Override
    public void send(@Nullable Object sender, Text original, ChatType type) {
        CommandSource source;
        if (!(sender instanceof CommandSource)) {
            if (sender instanceof String) {
                source = new NamedSource((String) sender);
            } else {
                source = Sponge.getServer().getConsole();
            }
        } else {
            source = (CommandSource) sender;
        }

        Text prefix = this.template.getForCommandSource(source);
        NucleusChatChannel.StaffChat.super.send(sender, Text.of(prefix, this.colour, original), type);
    }

    @Override
    @Nonnull
    public Collection<MessageReceiver> getMembers() {
        List<MessageReceiver> c =
                Sponge.getServer().getOnlinePlayers().stream().filter(x -> hasPermission(x, this.basePerm)).collect(Collectors.toList());
        c.add(Sponge.getServer().getConsole());
        return c;
    }

    @Override
    public boolean formatMessages() {
        return this.formatting;
    }

    private void onReload() {
        Nucleus.getNucleus().getConfigAdapter(StaffChatModule.ID, StaffChatConfigAdapter.class)
                .ifPresent(x -> {
                    this.formatting = x.getNodeOrDefault().isIncludeStandardChatFormatting();
                    this.template = x.getNodeOrDefault().getMessageTemplate();
                    this.colour = x.getNodeOrDefault().getColour();
                });
    }

    @NonnullByDefault
    private class NamedSource implements ProxySource {

        private final String name;

        NamedSource(String name) {
            this.name = name;
        }

        @Override
        public CommandSource getOriginalSource() {
            return Sponge.getServer().getConsole();
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public Optional<CommandSource> getCommandSource() {
            return Optional.of(getOriginalSource());
        }

        @Override
        public SubjectCollection getContainingCollection() {
            return getOriginalSource().getContainingCollection();
        }

        @Override
        public SubjectReference asSubjectReference() {
            return getOriginalSource().asSubjectReference();
        }

        @Override
        public boolean isSubjectDataPersisted() {
            return getOriginalSource().isSubjectDataPersisted();
        }

        @Override
        public SubjectData getSubjectData() {
            return getOriginalSource().getSubjectData();
        }

        @Override
        public SubjectData getTransientSubjectData() {
            return getOriginalSource().getTransientSubjectData();
        }

        @Override
        public Tristate getPermissionValue(Set<Context> contexts, String permission) {
            return getOriginalSource().getPermissionValue(contexts, permission);
        }

        @Override
        public boolean isChildOf(Set<Context> contexts, SubjectReference parent) {
            return getOriginalSource().isChildOf(contexts, parent);
        }

        @Override
        public List<SubjectReference> getParents(Set<Context> contexts) {
            return getOriginalSource().getParents();
        }

        @Override
        public Optional<String> getOption(Set<Context> contexts, String key) {
            return getOriginalSource().getOption(contexts, key);
        }

        @Override
        public String getIdentifier() {
            return getName();
        }

        @Override
        public Set<Context> getActiveContexts() {
            return getOriginalSource().getActiveContexts();
        }

        @Override
        public void sendMessage(Text message) {
            getOriginalSource().sendMessage(message);
        }

        @Override
        public MessageChannel getMessageChannel() {
            return getOriginalSource().getMessageChannel();
        }

        @Override
        public void setMessageChannel(MessageChannel channel) {
            getOriginalSource().setMessageChannel(channel);
        }
    }

}

/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.playerinfo.config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import io.github.nucleuspowered.neutrino.annotations.Default;
import io.github.nucleuspowered.nucleus.api.text.NucleusTextTemplate;
import io.github.nucleuspowered.nucleus.internal.text.NucleusTextTemplateImpl;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import uk.co.drnaylor.quickstart.config.NoMergeIfPresent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NonnullByDefault
@ConfigSerializable
public class ListConfig {

    @Setting("list-grouping-by-permission")
    private GroupConfig groupByPermissionGroup = new GroupConfig();

    @Setting(value = "server-panel-compatibility", comment = "config.playerinfo.list.panel")
    private boolean panelCompatibility = false;

    @Setting(value = "template", comment = "config.playerinfo.list.template")
    @Default(value = "{{displayname}}", saveDefaultIfNull = true, useDefaultIfEmpty = true)
    private NucleusTextTemplateImpl template;

    public boolean isGroupByPermissionGroup() {
        return this.groupByPermissionGroup.enabled;
    }

    public Map<String, String> getAliases() {
        return ImmutableMap.copyOf(this.groupByPermissionGroup.groupAliasing);
    }

    public List<String> getOrder() {
        return ImmutableList.copyOf(this.groupByPermissionGroup.groupPriority);
    }

    public String getDefaultGroupName() {
        if (this.groupByPermissionGroup.defaultGroupName.isEmpty()) {
            return "Default";
        }

        return this.groupByPermissionGroup.defaultGroupName;
    }

    public boolean isUseAliasOnly() {
        return this.groupByPermissionGroup.useAliasOnly;
    }

    public boolean isPanelCompatibility() {
        return this.panelCompatibility;
    }

    public NucleusTextTemplate getListTemplate() {
        return this.template;
    }

    @ConfigSerializable
    public static class GroupConfig {

        @Setting(value = "enabled", comment = "config.playerinfo.list.groups")
        private boolean enabled = false;

        @Setting(value = "use-aliases-only", comment = "config.playerinfo.list.aliasonly")
        private boolean useAliasOnly = false;

        @NoMergeIfPresent
        @Setting(value = "group-aliases", comment = "config.playerinfo.list.groupaliases")
        private Map<String, String> groupAliasing = new HashMap<String, String>() {{
            put("example-default-group", "Default Group");
            put("example-default-group-2", "Default Group");
        }};

        @NoMergeIfPresent
        @Setting(value = "group-order", comment = "config.playerinfo.list.grouporder")
        private List<String> groupPriority = Lists.newArrayList();

        @Setting(value = "default-group-name", comment = "config.playerinfo.list.defaultname")
        private String defaultGroupName = "Default";
    }
}

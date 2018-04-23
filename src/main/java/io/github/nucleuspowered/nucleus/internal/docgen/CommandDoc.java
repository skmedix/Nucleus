/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.internal.docgen;

import io.github.nucleuspowered.nucleus.internal.annotations.Since;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.lang.annotation.Annotation;
import java.util.List;

@ConfigSerializable
public class CommandDoc {

    @Setting
    private String commandName;

    @Setting
    private String aliases;

    @Setting
    private String rootAliases;

    @Setting
    private String defaultLevel;

    @Setting
    private String usageString;

    @Setting
    private String oneLineDescription;

    @Setting
    private String extendedDescription;

    @Setting
    private String module;

    @Setting
    private String permissionbase;

    @Setting
    private boolean warmup;

    @Setting
    private boolean cooldown;

    @Setting
    private boolean cost;

    @Setting
    private boolean requiresMixin;

    @Setting
    private String nucleusVersion;

    @Setting
    private String minecraftVersion;

    @Setting
    private String spongeVersion;

    @Setting
    private List<String> essentialsEquivalents;

    @Setting
    private Boolean isExactEssEquiv = null;

    @Setting
    private String essNotes;

    @Setting
    private List<PermissionDoc> permissions;

    @Setting private String simpleUsage;

    @Setting private String subcommands;

    public String getCommandName() {
        return this.commandName;
    }

    public void setCommandName(String commandName) {
        this.commandName = commandName;
    }

    public String getAliases() {
        return this.aliases;
    }

    public void setAliases(String aliases) {
        this.aliases = aliases;
    }

    public String getDefaultLevel() {
        return this.defaultLevel;
    }

    public void setDefaultLevel(String defaultLevel) {
        this.defaultLevel = defaultLevel;
    }

    public String getUsageString() {
        return this.usageString;
    }

    public void setUsageString(String usageString) {
        this.usageString = usageString;
    }

    public String getOneLineDescription() {
        return this.oneLineDescription;
    }

    public void setOneLineDescription(String oneLineDescription) {
        this.oneLineDescription = oneLineDescription;
    }

    public String getExtendedDescription() {
        return this.extendedDescription;
    }

    public void setExtendedDescription(String extendedDescription) {
        this.extendedDescription = extendedDescription;
    }

    public String getModule() {
        return this.module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getPermissionbase() {
        return this.permissionbase;
    }

    public void setPermissionbase(String permissionbase) {
        this.permissionbase = permissionbase.replaceAll("\\.base", "");
    }

    public boolean isWarmup() {
        return this.warmup;
    }

    public void setWarmup(boolean warmup) {
        this.warmup = warmup;
    }

    public boolean isCooldown() {
        return this.cooldown;
    }

    public void setCooldown(boolean cooldown) {
        this.cooldown = cooldown;
    }

    public boolean isCost() {
        return this.cost;
    }

    public void setCost(boolean cost) {
        this.cost = cost;
    }

    public List<PermissionDoc> getPermissions() {
        return this.permissions;
    }

    public void setPermissions(List<PermissionDoc> permissions) {
        this.permissions = permissions;
    }

    public boolean isRequiresMixin() {
        return this.requiresMixin;
    }

    public void setRequiresMixin(boolean requiresMixin) {
        this.requiresMixin = requiresMixin;
    }

    public String getNucleusVersion() {
        return this.nucleusVersion;
    }

    public String getMinecraftVersion() {
        return this.minecraftVersion;
    }

    public String getSpongeVersion() {
        return this.spongeVersion;
    }

    public String getRootAliases() {
        return this.rootAliases;
    }

    public void setRootAliases(String rootAliases) {
        this.rootAliases = rootAliases;
    }

    public void setSince(Since since) {
        if (since == null) {
            since = new Since() {
                @Override public Class<? extends Annotation> annotationType() {
                    return Since.class;
                }

                @Override public String nucleusVersion() {
                    return "";
                }

                @Override public String spongeApiVersion() {
                    return "";
                }

                @Override public String minecraftVersion() {
                    return "";
                }
            };
        }

        this.nucleusVersion = since.nucleusVersion().isEmpty() ? null : since.nucleusVersion();
        this.minecraftVersion = since.minecraftVersion().isEmpty() ? null : since.minecraftVersion();
        this.spongeVersion = since.spongeApiVersion().isEmpty() ? null : since.spongeApiVersion();
    }

    public String getSimpleUsage() {
        return this.simpleUsage;
    }

    public void setSimpleUsage(String simpleUsage) {
        this.simpleUsage = simpleUsage;
    }

    public String getSubcommands() {
        return this.subcommands;
    }

    public void setSubcommands(String subcommands) {
        this.subcommands = subcommands;
    }

    public List<String> getEssentialsEquivalents() {
        return this.essentialsEquivalents;
    }

    public void setEssentialsEquivalents(List<String> essentialsEquivalents) {
        this.essentialsEquivalents = essentialsEquivalents;
    }

    public Boolean getExactEssEquiv() {
        return this.isExactEssEquiv;
    }

    public void setExactEssEquiv(Boolean exactEssEquiv) {
        this.isExactEssEquiv = exactEssEquiv;
    }

    public String getEssNotes() {
        return this.essNotes;
    }

    public void setEssNotes(String essNotes) {
        this.essNotes = essNotes;
    }
}

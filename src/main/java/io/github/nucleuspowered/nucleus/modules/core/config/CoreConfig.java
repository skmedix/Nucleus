/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.core.config;

import com.google.common.collect.Maps;
import io.github.nucleuspowered.neutrino.annotations.DoNotGenerate;
import io.github.nucleuspowered.nucleus.internal.text.NucleusTextTemplateImpl;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import uk.co.drnaylor.quickstart.config.NoMergeIfPresent;

import java.util.Map;
import java.util.UUID;

@ConfigSerializable
public class CoreConfig {

    @Setting(value = "debug-mode", comment = "config.debugmode")
    private boolean debugmode = false;

    @Setting(value = "print-on-autosave", comment = "config.printonautosave")
    private boolean printOnAutosave = false;

    @Setting(value = "use-custom-message-file", comment = "config.custommessages")
    private boolean custommessages = false;

    @Setting(value = "warmup-canceling", comment = "config.core.warmup.info")
    private WarmupConfig warmupConfig = new WarmupConfig();

    @Setting(value = "command-on-name-click", comment = "config.core.commandonname")
    private String commandOnNameClick = "/msg {{subject}}";

    @Setting(value = "kick-on-stop")
    private KickOnStopConfig kickOnStop = new KickOnStopConfig();

    @DoNotGenerate
    @Setting(value = "enable-doc-gen")
    private boolean enableDocGen = false;

    @DoNotGenerate
    @Setting(value = "simulate-error-on-startup")
    private boolean errorOnStartup = false;

    @Setting(value = "safe-teleport-check", comment = "config.core.safeteleport")
    private SafeTeleportConfig safeTeleportConfig = new SafeTeleportConfig();

    @Setting(value = "console-overrides-exemptions", comment = "config.core.consoleoverrides")
    private boolean consoleOverride = true;

    @DoNotGenerate
    @Setting(value = "trace-user-creations-level")
    private int traceUserCreations = 0;

    @DoNotGenerate
    @Setting(value = "print-file-save-load")
    private boolean printSaveLoad = false;

    @NoMergeIfPresent
    @Setting(value = "world-uuid-migration", comment = "config.core.worlduuidmigration")
    private Map<UUID, String> uuidMigration = Maps.newHashMap();

    @Setting(value = "check-for-wildcard", comment = "config.core.wildcard")
    private boolean checkForWildcard = true;

    @Setting(value = "show-warning-on-startup", comment = "config.core.warning-on-startup")
    private boolean warningOnStartup = true;

    @Setting(value = "more-accurate-visitor-count", comment = "config.core.accurate")
    private boolean moreAccurate = false;

    @Setting(value = "override-language", comment = "config.core.language")
    private String serverLocale = "default";

    @Setting(value = "data-file-location", comment = "config.core.datafilelocation")
    private String dataFileLocation = "default";

    @Setting(value = "offline-user-tab-limit", comment = "config.core.offlineusertablimit")
    private int nicknameArgOfflineLimit = 20;

    @Setting(value = "enable-parent-perms", comment = "config.core.parentperms")
    private boolean useParentPerms = true;

    public boolean isDebugmode() {
        return this.debugmode;
    }

    public boolean isPrintOnAutosave() {
        return this.printOnAutosave;
    }

    public boolean isCustommessages() {
        return this.custommessages;
    }

    public WarmupConfig getWarmupConfig() {
        return this.warmupConfig;
    }

    public String getCommandOnNameClick() {
        return this.commandOnNameClick;
    }

    public boolean isKickOnStop() {
        return this.kickOnStop.isKickOnStop();
    }

    public NucleusTextTemplateImpl getKickOnStopMessage() {
        return this.kickOnStop.getKickOnStopMessage();
    }

    public boolean isEnableDocGen() {
        return this.enableDocGen;
    }

    public boolean isErrorOnStartup() {
        return this.errorOnStartup;
    }

    public SafeTeleportConfig getSafeTeleportConfig() {
        return this.safeTeleportConfig;
    }

    public boolean isConsoleOverride() {
        return this.consoleOverride;
    }

    /**
     * For debugging. 0 is off, 1 is abnormal players, such as "offline", 2 is everyone.
     * @return The level to debug.
     */
    public int traceUserCreations() {
        return this.traceUserCreations;
    }

    public boolean isPrintSaveLoad() {
        return this.printSaveLoad;
    }

    public Map<UUID, String> getUuidMigration() {
        return this.uuidMigration;
    }

    public boolean isCheckForWildcard() {
        return this.checkForWildcard;
    }

    public boolean isWarningOnStartup() {
        return this.warningOnStartup;
    }

    public boolean isMoreAccurate() {
        return this.moreAccurate;
    }

    public String getServerLocale() {
        return this.serverLocale;
    }

    public int getNicknameArgOfflineLimit() {
        return this.nicknameArgOfflineLimit;
    }

    public boolean isUseParentPerms() {
        return useParentPerms;
    }
}

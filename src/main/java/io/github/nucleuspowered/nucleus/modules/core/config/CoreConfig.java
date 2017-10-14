/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.core.config;

import io.github.nucleuspowered.neutrino.annotations.DoNotGenerate;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class CoreConfig {

    @Setting(value = "debug-mode", comment = "config.debugmode")
    private boolean debugmode = false;

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

    public boolean isDebugmode() {
        return debugmode;
    }

    public boolean isCustommessages() {
        return custommessages;
    }

    public WarmupConfig getWarmupConfig() {
        return warmupConfig;
    }

    public String getCommandOnNameClick() {
        return commandOnNameClick;
    }

    public boolean isKickOnStop() {
        return kickOnStop.isKickOnStop();
    }

    public String getKickOnStopMessage() {
        return kickOnStop.getKickOnStopMessage();
    }

    public boolean isEnableDocGen() {
        return enableDocGen;
    }

    public boolean isErrorOnStartup() {
        return errorOnStartup;
    }

    public SafeTeleportConfig getSafeTeleportConfig() {
        return safeTeleportConfig;
    }

    public boolean isConsoleOverride() {
        return consoleOverride;
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
}

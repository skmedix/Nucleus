/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.modules.warn.config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class WarnConfig {

    @Setting(value = "show-login", comment = "config.warn.showonlogin")
    private boolean showOnLogin = true;

    @Setting(value = "expire-warnings", comment = "config.warn.expire")
    private boolean expireWarnings = true;

    @Setting(value = "minimum-warn-length", comment = "config.warn.minwarnlength")
    private long minWarnLength = -1;

    @Setting(value = "maximum-warn-length", comment = "config.warn.maxwarnlength")
    private long maxWarnLength = -1;

    @Setting(value = "default-length", comment = "config.warn.defaultlength")
    private long defaultLength = -1;

    @Setting(value = "warnings-before-action", comment = "config.warn.warningsbeforeaction")
    private int warningsBeforeAction = -1;

    @Setting(value = "action-command", comment = "config.warn.actioncommand")
    private String actionCommand = "tempban {{name}} 1d Exceeding the active warning threshold";

    public boolean isShowOnLogin() {
        return this.showOnLogin;
    }

    public boolean isExpireWarnings() {
        return this.expireWarnings;
    }

    public long getMinimumWarnLength() {
        return this.minWarnLength;
    }

    public long getMaximumWarnLength() {
        return this.maxWarnLength;
    }

    public long getDefaultLength() {
        return this.defaultLength;
    }

    public int getWarningsBeforeAction() {
        return this.warningsBeforeAction;
    }

    public String getActionCommand() {
        return this.actionCommand;
    }
}

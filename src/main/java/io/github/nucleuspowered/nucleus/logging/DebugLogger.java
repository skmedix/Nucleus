/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.logging;

import io.github.nucleuspowered.nucleus.LoggerWrapper;
import io.github.nucleuspowered.nucleus.Nucleus;
import io.github.nucleuspowered.nucleus.modules.core.config.CoreConfigAdapter;
import org.slf4j.Logger;
import org.slf4j.Marker;

public class DebugLogger extends LoggerWrapper {

    private CoreConfigAdapter cca = null;
    private final Nucleus plugin;

    public DebugLogger(Nucleus plugin, Logger wrappedLogger) {
        super(wrappedLogger);
        this.plugin = plugin;
    }

    private boolean isDebugModeOn() {
        if (this.cca == null) {
            try {
                this.cca = this.plugin.getModuleContainer().getConfigAdapterForModule("core", CoreConfigAdapter.class);
            } catch (Exception e) {
                return true;
            }
        }

        return this.cca.getNodeOrDefault().isDebugmode();
    }

    @Override public void debug(String format, Object arg) {
        if (!isDebugModeOn()) {
            return;
        }

        super.debug(format, arg);
    }

    @Override public void debug(String format, Object arg1, Object arg2) {
        if (!isDebugModeOn()) {
            return;
        }

        super.debug(format, arg1, arg2);
    }

    @Override public void debug(String format, Object... arguments) {
        if (!isDebugModeOn()) {
            return;
        }

        super.debug(format, arguments);
    }

    @Override public void debug(Marker marker, String format, Object arg) {
        if (!isDebugModeOn()) {
            return;
        }

        super.debug(marker, format, arg);
    }

    @Override public void debug(Marker marker, String format, Object arg1, Object arg2) {
        if (!isDebugModeOn()) {
            return;
        }

        super.debug(marker, format, arg1, arg2);
    }

    @Override public void debug(Marker marker, String format, Object... arguments) {
        if (!isDebugModeOn()) {
            return;
        }

        super.debug(marker, format, arguments);
    }

    @Override public void debug(Marker marker, String msg) {
        if (!isDebugModeOn()) {
            return;
        }

        super.debug(marker, msg);
    }

    @Override public void debug(Marker marker, String msg, Throwable t) {
        if (!isDebugModeOn()) {
            return;
        }

        super.debug(marker, msg, t);
    }

    @Override public void debug(String msg) {
        if (!isDebugModeOn()) {
            return;
        }

        super.debug(msg);
    }

    @Override public void debug(String msg, Throwable t) {
        if (!isDebugModeOn()) {
            return;
        }

        super.debug(msg, t);
    }
}

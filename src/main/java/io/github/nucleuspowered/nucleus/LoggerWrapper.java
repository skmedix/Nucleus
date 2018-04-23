/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus;

import org.slf4j.Logger;
import org.slf4j.Marker;

/**
 * The {@link LoggerWrapper} is simply that, a wrapper around a {@link LoggerWrapper}. By itself, it does nothing new, but
 * makes it easier to extend the logger to provide additional functionality.
 *
 * <p>
 *     (Basically, couldn't find an appropriate wrapper class, made my own.)
 * </p>
 */
public class LoggerWrapper implements Logger {

    private final Logger wrappedLogger;

    public LoggerWrapper(Logger wrappedLogger) {
        this.wrappedLogger = wrappedLogger;
    }

    @Override
    public String getName() {
        return this.wrappedLogger.getName();
    }

    @Override
    public boolean isTraceEnabled() {
        return this.wrappedLogger.isTraceEnabled();
    }

    @Override
    public void trace(String msg) {
        this.wrappedLogger.trace(msg);
    }

    @Override
    public void trace(String format, Object arg) {
        this.wrappedLogger.trace(format, arg);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        this.wrappedLogger.trace(format, arg1, arg2);
    }

    @Override
    public void trace(String format, Object... arguments) {
        this.wrappedLogger.trace(format, arguments);
    }

    @Override
    public void trace(String msg, Throwable t) {
        this.wrappedLogger.trace(msg, t);
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return this.wrappedLogger.isTraceEnabled(marker);
    }

    @Override
    public void trace(Marker marker, String msg) {
        this.wrappedLogger.trace(marker, msg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
        this.wrappedLogger.trace(marker, format, arg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        this.wrappedLogger.trace(marker, format, arg1, arg2);
    }

    @Override
    public void trace(Marker marker, String format, Object... argArray) {
        this.wrappedLogger.trace(marker, format, argArray);
    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        this.wrappedLogger.trace(marker, msg, t);
    }

    @Override
    public boolean isDebugEnabled() {
        return this.wrappedLogger.isDebugEnabled();
    }

    @Override
    public void debug(String msg) {
        this.wrappedLogger.debug(msg);
    }

    @Override
    public void debug(String format, Object arg) {
        this.wrappedLogger.debug(format, arg);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        this.wrappedLogger.debug(format, arg1, arg2);
    }

    @Override
    public void debug(String format, Object... arguments) {
        this.wrappedLogger.debug(format, arguments);
    }

    @Override
    public void debug(String msg, Throwable t) {
        this.wrappedLogger.debug(msg, t);
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return this.wrappedLogger.isDebugEnabled(marker);
    }

    @Override
    public void debug(Marker marker, String msg) {
        this.wrappedLogger.debug(marker, msg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg) {
        this.wrappedLogger.debug(marker, format, arg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        this.wrappedLogger.debug(marker, format, arg1, arg2);
    }

    @Override
    public void debug(Marker marker, String format, Object... arguments) {
        this.wrappedLogger.debug(marker, format, arguments);
    }

    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        this.wrappedLogger.debug(marker, msg, t);
    }

    @Override
    public boolean isInfoEnabled() {
        return this.wrappedLogger.isInfoEnabled();
    }

    @Override
    public void info(String msg) {
        this.wrappedLogger.info(msg);
    }

    @Override
    public void info(String format, Object arg) {
        this.wrappedLogger.info(format, arg);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        this.wrappedLogger.info(format, arg1, arg2);
    }

    @Override
    public void info(String format, Object... arguments) {
        this.wrappedLogger.info(format, arguments);
    }

    @Override
    public void info(String msg, Throwable t) {
        this.wrappedLogger.info(msg, t);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return this.wrappedLogger.isInfoEnabled();
    }

    @Override
    public void info(Marker marker, String msg) {
        this.wrappedLogger.info(marker, msg);
    }

    @Override
    public void info(Marker marker, String format, Object arg) {
        this.wrappedLogger.info(marker, format, arg);
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        this.wrappedLogger.info(marker, format, arg1, arg2);
    }

    @Override
    public void info(Marker marker, String format, Object... arguments) {
        this.wrappedLogger.info(marker, format, arguments);
    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {
        this.wrappedLogger.info(marker, msg, t);
    }

    @Override
    public boolean isWarnEnabled() {
        return this.wrappedLogger.isWarnEnabled();
    }

    @Override
    public void warn(String msg) {
        this.wrappedLogger.warn(msg);
    }

    @Override
    public void warn(String format, Object arg) {
        this.wrappedLogger.warn(format, arg);
    }

    @Override
    public void warn(String format, Object... arguments) {
        this.wrappedLogger.warn(format, arguments);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        this.wrappedLogger.warn(format, arg1, arg2);
    }

    @Override
    public void warn(String msg, Throwable t) {
        this.wrappedLogger.warn(msg, t);
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return this.wrappedLogger.isWarnEnabled(marker);
    }

    @Override
    public void warn(Marker marker, String msg) {
        this.wrappedLogger.warn(marker, msg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg) {
        this.wrappedLogger.warn(marker, format, arg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        this.wrappedLogger.warn(marker, format, arg1, arg2);
    }

    @Override
    public void warn(Marker marker, String format, Object... arguments) {
        this.wrappedLogger.warn(marker, format, arguments);
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        this.wrappedLogger.warn(marker, msg, t);
    }

    @Override
    public boolean isErrorEnabled() {
        return this.wrappedLogger.isErrorEnabled();
    }

    @Override
    public void error(String msg) {
        this.wrappedLogger.error(msg);
    }

    @Override
    public void error(String format, Object arg) {
        this.wrappedLogger.error(format, arg);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        this.wrappedLogger.error(format, arg1, arg2);
    }

    @Override
    public void error(String format, Object... arguments) {
        this.wrappedLogger.error(format, arguments);
    }

    @Override
    public void error(String msg, Throwable t) {
        this.wrappedLogger.error(msg, t);
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return this.wrappedLogger.isErrorEnabled(marker);
    }

    @Override
    public void error(Marker marker, String msg) {
        this.wrappedLogger.error(marker, msg);
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        this.wrappedLogger.error(marker, format, arg);
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        this.wrappedLogger.error(marker, format, arg1, arg2);
    }

    @Override
    public void error(Marker marker, String format, Object... arguments) {
        this.wrappedLogger.error(marker, format, arguments);
    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {
        this.wrappedLogger.error(marker, msg, t);
    }
}
package com.github.lkq.maven.plugin.deploydeps.logging;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;

public class Logger extends MarkerIgnoringBase {
    private Log logger = new SystemStreamLog();

    public void set(Log logger) {
        this.logger = logger;
    }

    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    @Override
    public void trace(String msg) { }

    @Override
    public void trace(String format, Object arg) { }

    @Override
    public void trace(String format, Object arg1, Object arg2) { }

    @Override
    public void trace(String format, Object... arguments) { }

    @Override
    public void trace(String msg, Throwable t) { }

    @Override
    public boolean isDebugEnabled() { return false; }

    @Override
    public void debug(String msg) { }

    @Override
    public void debug(String format, Object arg) { }

    @Override
    public void debug(String format, Object arg1, Object arg2) { }

    @Override
    public void debug(String format, Object... arguments) { }

    @Override
    public void debug(String msg, Throwable t) { }

    @Override
    public boolean isInfoEnabled() { return true; }

    @Override
    public void info(String msg) {
        logger.info(msg);
    }

    @Override
    public void info(String format, Object arg) {
        logger.info(MessageFormatter.arrayFormat(format, new Object[]{arg}).getMessage());
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        logger.info(MessageFormatter.arrayFormat(format, new Object[]{arg1, arg2}).getMessage());
    }

    @Override
    public void info(String format, Object... arguments) {
        logger.info(MessageFormatter.arrayFormat(format, arguments).getMessage());
    }

    @Override
    public void info(String msg, Throwable t) {
        logger.info(msg, t);
    }

    @Override
    public boolean isWarnEnabled() { return true; }

    @Override
    public void warn(String msg) {
        logger.info(msg);
    }

    @Override
    public void warn(String format, Object arg) {
        logger.info(MessageFormatter.arrayFormat(format, new Object[]{arg}).getMessage());
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        logger.info(MessageFormatter.arrayFormat(format, new Object[]{arg1, arg2}).getMessage());
    }

    @Override
    public void warn(String format, Object... arguments) {
        logger.info(MessageFormatter.arrayFormat(format, arguments).getMessage());
    }

    @Override
    public void warn(String msg, Throwable t) {
        logger.info(msg, t);
    }

    @Override
    public boolean isErrorEnabled() { return true; }

    @Override
    public void error(String msg) {
        logger.info(msg);
    }

    @Override
    public void error(String format, Object arg) {
        logger.info(MessageFormatter.arrayFormat(format, new Object[]{arg}).getMessage());
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        logger.info(MessageFormatter.arrayFormat(format, new Object[]{arg1, arg2}).getMessage());
    }

    @Override
    public void error(String format, Object... arguments) {
        logger.info(MessageFormatter.arrayFormat(format, arguments).getMessage());
    }

    @Override
    public void error(String msg, Throwable t) {
        logger.info(msg, t);
    }
}

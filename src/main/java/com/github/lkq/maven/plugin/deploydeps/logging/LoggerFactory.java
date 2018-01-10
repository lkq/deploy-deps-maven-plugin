package com.github.lkq.maven.plugin.deploydeps.logging;

import org.apache.maven.plugin.logging.Log;
import org.slf4j.ILoggerFactory;

public class LoggerFactory implements ILoggerFactory {
    private static LoggerFactory instance = new LoggerFactory();

    private static Logger logger = new Logger();

    private LoggerFactory() { }

    public static LoggerFactory getInstance() {
        return instance;
    }

    public static void bridge(Log log) {
        logger.set(log);
    }

    @Override
    public Logger getLogger(String name) {
        return logger;
    }
}

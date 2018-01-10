package org.slf4j.impl;

import com.github.lkq.maven.plugin.deploydeps.logging.LoggerFactory;
import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

/**
 * slf4j binder, redirect log from slf4j api to maven log
 */
public class StaticLoggerBinder implements LoggerFactoryBinder {

    private static StaticLoggerBinder SINGLETON = new StaticLoggerBinder();

    @Override
    public ILoggerFactory getLoggerFactory() {
        return LoggerFactory.getInstance();
    }

    @Override
    public String getLoggerFactoryClassStr() {
        return LoggerFactory.class.getName();
    }

    /**
     * required for slf4j
     * @return
     */
    public static StaticLoggerBinder getSingleton() {
        return SINGLETON;
    }
}

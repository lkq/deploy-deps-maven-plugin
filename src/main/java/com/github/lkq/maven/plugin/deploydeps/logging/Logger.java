package com.github.lkq.maven.plugin.deploydeps.logging;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;

public class Logger {

    private static Log defaultLogger = new SystemStreamLog();
    private static AbstractMojo mojo;

    public static void init(AbstractMojo mojo) {
        Logger.mojo = mojo;
    }

    public static Log get() {
        if (mojo == null) {
            return defaultLogger;
        }
        return mojo.getLog();
    }
}

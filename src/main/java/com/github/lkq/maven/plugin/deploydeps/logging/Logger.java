package com.github.lkq.maven.plugin.deploydeps.logging;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;

public class Logger {
    private static Log logger = new SystemStreamLog();

    public static void set(Log logger) {
        Logger.logger = logger;
    }

    public static Log get() {
        return logger;
    }
}

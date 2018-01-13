package com.github.lkq.maven.plugin.deploydeps;

import java.util.List;

public class DefaultConfigProcessor {

    public void process(List<DefaultConfig> configs) {
        if (configs != null && configs.size() > 0) {
            for (DefaultConfig config : configs) {
                require(config.getUser(), "missing ssh user");
                require(config.getHost(), "missing ssh host");
                require(config.getKeyFile(), "missing ssh key file");
                require(config.getTargetPath(), "missing target path");
                config.setPort(defaultValue(config.getPort(), "22"));
                config.setTargetFileMode(defaultValue(config.getTargetFileMode(), "0640"));
            }
        }
    }

    private String defaultValue(String value, String defaultValue) {
        if (value == null || "".equals(value.trim())) {
            return defaultValue;
        }
        return value;
    }

    private void require(String value, String errorMsg) {
        if (value == null || "".equals(value.trim())) {
            throw new RuntimeException(errorMsg);
        }
    }
}

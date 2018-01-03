package com.github.lkq.maven.plugin.deploydeps;

import java.util.Arrays;

public class DeployerConfig {
    private String className;
    private String[] constructorArgs;

    private String user;
    private String keyFile;
    private String passwordFile;
    private String host;

    public String getClassName() {
        return className;
    }

    public String[] getConstructorArgs() {
        return constructorArgs;
    }

    public String getUser() {
        return user;
    }

    public String getKeyFile() {
        return keyFile;
    }

    public String getPasswordFile() {
        return passwordFile;
    }

    public String getHost() {
        return host;
    }

    @Override
    public String toString() {
        return "DeployerConfig{" +
                "className='" + className + '\'' +
                ", constructorArgs=" + Arrays.toString(constructorArgs) +
                ", user='" + user + '\'' +
                ", keyFile='" + keyFile + '\'' +
                ", passwordFile='" + passwordFile + '\'' +
                ", host='" + host + '\'' +
                '}';
    }
}

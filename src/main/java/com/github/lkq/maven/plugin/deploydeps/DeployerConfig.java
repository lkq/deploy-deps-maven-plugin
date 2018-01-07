package com.github.lkq.maven.plugin.deploydeps;

import java.util.Arrays;

public class DeployerConfig {
    private SSHConfig ssh;
    private CustomConfig custom;

    public SSHConfig getSsh() {
        return ssh;
    }

    public void setSsh(SSHConfig ssh) {
        this.ssh = ssh;
    }

    public CustomConfig getCustom() {
        return custom;
    }

    public void setCustom(CustomConfig custom) {
        this.custom = custom;
    }

    @Override
    public String toString() {
        return "DeployerConfig{" +
                "ssh=" + ssh +
                ", custom=" + custom +
                '}';
    }

    public static class SSHConfig {
        private String user;
        private String keyFile;
        private String passwordFile;
        private String host;

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getKeyFile() {
            return keyFile;
        }

        public void setKeyFile(String keyFile) {
            this.keyFile = keyFile;
        }

        public String getPasswordFile() {
            return passwordFile;
        }

        public void setPasswordFile(String passwordFile) {
            this.passwordFile = passwordFile;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        @Override
        public String toString() {
            return "SSHConfig{" +
                    "user='" + user + '\'' +
                    ", keyFile='" + keyFile + '\'' +
                    ", passwordFile='" + passwordFile + '\'' +
                    ", host='" + host + '\'' +
                    '}';
        }
    }

    public static class CustomConfig {
        private String className;
        private String[] constructorArgs;

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public String[] getConstructorArgs() {
            return constructorArgs;
        }

        public void setConstructorArgs(String[] constructorArgs) {
            this.constructorArgs = constructorArgs;
        }

        @Override
        public String toString() {
            return "CustomConfig{" +
                    "className='" + className + '\'' +
                    ", constructorArgs=" + Arrays.toString(constructorArgs) +
                    '}';
        }
    }
}

package com.github.lkq.maven.plugin.deploydeps;

public class DefaultConfig {

    private String user;
    private String keyFile;
    private String passwordFile;
    private String host;
    private String targetPath;
    private String targetFileMode;
    private String port;

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

    public String getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    public String getTargetFileMode() {
        return targetFileMode;
    }

    public void setTargetFileMode(String targetFileMode) {
        this.targetFileMode = targetFileMode;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "DefaultConfig{" +
                "user='" + user + '\'' +
                ", keyFile='" + keyFile + '\'' +
                ", passwordFile='" + passwordFile + '\'' +
                ", host='" + host + '\'' +
                ", targetPath='" + targetPath + '\'' +
                ", targetFileMode='" + targetFileMode + '\'' +
                ", port='" + port + '\'' +
                '}';
    }
}

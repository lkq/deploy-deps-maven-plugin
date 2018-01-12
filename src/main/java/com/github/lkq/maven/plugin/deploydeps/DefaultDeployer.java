package com.github.lkq.maven.plugin.deploydeps;

public class DefaultDeployer {

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

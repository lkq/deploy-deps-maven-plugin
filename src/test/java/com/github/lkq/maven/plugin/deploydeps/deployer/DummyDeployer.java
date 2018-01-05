package com.github.lkq.maven.plugin.deploydeps.deployer;

public class DummyDeployer {
    public final String user;
    public final String host;
    public String localFile;
    public String remotePath;
    public String mode;

    public DummyDeployer(String user, String host) {
        this.user = user;
        this.host = host;
    }

    public void put(String localFile, String remotePath, String mode) {

        this.localFile = localFile;
        this.remotePath = remotePath;
        this.mode = mode;
    }
}

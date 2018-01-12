package com.github.lkq.maven.plugin.deploydeps;

public class DeployerStub {
    private String user;
    private String host;

    public DeployerStub(String user, String host) {
        this.user = user;
        this.host = host;
        System.out.println("created DeployerStub, user=" + user + " host=" + host);
    }

    public void put(String localRepoPath, String repoArtifactPath) {
        System.out.println("stub deployer user=" + user + " host=" + host + ", from [" + localRepoPath + "] to [" + repoArtifactPath);
    }
}

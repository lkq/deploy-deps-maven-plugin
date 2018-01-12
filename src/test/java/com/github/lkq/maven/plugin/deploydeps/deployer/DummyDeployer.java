package com.github.lkq.maven.plugin.deploydeps.deployer;

public class DummyDeployer {
    public final String user;
    public final String host;
    public String localRepoPath;
    public String repoArtifactPath;

    public DummyDeployer(String user, String host) {
        this.user = user;
        this.host = host;
    }

    public void put(String localRepoPath, String repoArtifactPath) {

        this.localRepoPath = localRepoPath;
        this.repoArtifactPath = repoArtifactPath;
    }
}

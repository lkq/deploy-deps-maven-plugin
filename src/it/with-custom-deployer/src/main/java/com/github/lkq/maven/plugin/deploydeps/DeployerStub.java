package com.github.lkq.maven.plugin.deploydeps;

public class DeployerStub {
    public DeployerStub(String user, String host, String remotePath) {
        System.out.println("created DeployerStub");
    }

    public boolean put(String localRepo, String repoArtifactPath) {
        System.out.println("stubbed put, localRepo=" + localRepo + " repoArtifactPath=" + repoArtifactPath);
        return true;
    }
}

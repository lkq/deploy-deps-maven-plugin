package com.github.lkq.maven.plugin.deploydeps.deployer;

import java.io.IOException;

public interface Deployer {
    void put(String localRepoPath, String repoArtifactPath) throws IOException;
}

package com.github.lkq.maven.plugin.deploydeps.deployer;

import java.io.IOException;

public interface Deployer {
    /**
     * deploy local file to remote hosts
     * @param localRepoPath the local maven repository base dir
     * @param repoArtifactPath the artifact relative file path in the maven repository
     * @return
     * @throws IOException
     */
    boolean put(String localRepoPath, String repoArtifactPath) throws IOException;
}

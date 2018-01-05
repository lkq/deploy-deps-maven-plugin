package com.github.lkq.maven.plugin.deploydeps.deployer;

import java.io.IOException;

public interface Deployer {
    void put(String localFile, String remotePath, String mode) throws IOException;
}

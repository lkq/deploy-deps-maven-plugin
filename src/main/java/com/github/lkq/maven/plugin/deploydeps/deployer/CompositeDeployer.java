package com.github.lkq.maven.plugin.deploydeps.deployer;

import com.github.lkq.maven.plugin.deploydeps.logging.Logger;
import org.apache.maven.plugin.logging.Log;

import java.util.ArrayList;
import java.util.List;

public class CompositeDeployer implements Deployer {

    private Log logger = Logger.get();

    private List<Deployer> deployers = new ArrayList<>();

    public CompositeDeployer(List<Deployer> deployers) {
        this.deployers.addAll(deployers);
    }

    @Override
    public void put(String localRepoPath, String repoArtifactPath) {
        if (deployers.size() <= 0) {
            throw new RuntimeException("no deployer available");
        }
        for (Deployer deployer : deployers) {
            try {
                deployer.put(localRepoPath, repoArtifactPath);
            } catch (Exception ignored) {
                String msg = "failed to deploy file: deployer=" + deployer +
                        ", localRepoPath=" + localRepoPath +
                        ", repoArtifactPath=" + repoArtifactPath;
                logger.warn(msg, ignored);
                // continue deploy
            }
        }
    }

    public int deployerCount() {
        return deployers.size();
    }

    public Deployer getDeployer(int index) {
        return deployers.get(index);
    }
}

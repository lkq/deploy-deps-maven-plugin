package com.github.lkq.maven.plugin.deploydeps.deployer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CompositeDeployer implements Deployer {

    private Logger logger = LoggerFactory.getLogger(CompositeDeployer.class);

    private List<Deployer> deployers = new ArrayList<>();

    public CompositeDeployer with(Deployer deployer) {
        deployers.add(deployer);
        return this;
    }

    @Override
    public void put(String localFile, String remotePath, String mode) throws IOException {
        if (deployers.size() <= 0) {
            throw new RuntimeException("no deployer available");
        }
        for (Deployer deployer : deployers) {
            try {
                deployer.put(localFile, remotePath, mode);
            } catch (Exception ignored) {
                logger.warn("failed to deploy file: deployer={}, localFile={}, remotePath={}, mode={}", deployer, localFile, remotePath, mode);
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

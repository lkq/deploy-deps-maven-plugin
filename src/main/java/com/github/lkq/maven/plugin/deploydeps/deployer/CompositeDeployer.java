package com.github.lkq.maven.plugin.deploydeps.deployer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CompositeDeployer implements Deployer {

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
            deployer.put(localFile, remotePath, mode);
        }
    }

    public int deployerCount() {
        return deployers.size();
    }

    public Deployer getDeployer(int index) {
        return deployers.get(index);
    }
}

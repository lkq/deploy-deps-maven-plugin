package com.github.lkq.maven.plugin.deploydeps.deployer;

import com.github.lkq.maven.plugin.deploydeps.logging.Logger;
import org.apache.maven.plugin.logging.Log;

import java.util.ArrayList;
import java.util.List;

public class CompositeDeployer implements Deployer {

    private Log logger = Logger.get();

    private List<Deployer> deployers = new ArrayList<>();

    public CompositeDeployer with(Deployer deployer) {
        deployers.add(deployer);
        return this;
    }

    @Override
    public void put(String localFile, String remotePath, String mode) {
        if (deployers.size() <= 0) {
            throw new RuntimeException("no deployer available");
        }
        for (Deployer deployer : deployers) {
            try {
                deployer.put(localFile, remotePath, mode);
            } catch (Exception ignored) {
                String msg = "failed to deploy file: deployer=" + deployer +
                        ", localFile=" + localFile +
                        ", remotePath=" + remotePath +
                        ", mode=" + mode;
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

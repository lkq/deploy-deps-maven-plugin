package com.github.lkq.maven.plugin.deploydeps.deployer;

import com.github.lkq.maven.plugin.deploydeps.logging.Logger;
import com.github.lkq.maven.plugin.deploydeps.report.Reporter;
import org.apache.maven.plugin.logging.Log;

import java.util.ArrayList;
import java.util.List;

public class CompositeDeployer {

    private Log logger = Logger.get();

    private final List<Deployer> deployers = new ArrayList<>();
    private final Reporter reporter;

    public CompositeDeployer(List<Deployer> deployers, Reporter reporter) {
        this.reporter = reporter;
        this.deployers.addAll(deployers);
    }

    public void put(String localRepoPath, String repoArtifactPath) {
        for (Deployer deployer : deployers) {
            try {
                if (deployer.put(localRepoPath, repoArtifactPath)) {
                    reporter.reportSuccess(repoArtifactPath);
                } else {
                    reporter.reportSkipped(repoArtifactPath);
                }
            } catch (Exception ignored) {
                reporter.reportFail(repoArtifactPath);
                logger.warn("failed to deploy " + repoArtifactPath + " with " + deployer, ignored);
                // continue with other deployers
            }
        }
    }

}

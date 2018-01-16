package com.github.lkq.maven.plugin.deploydeps;

import com.github.lkq.maven.plugin.deploydeps.deployer.CompositeDeployer;
import com.github.lkq.maven.plugin.deploydeps.deployer.Deployer;
import com.github.lkq.maven.plugin.deploydeps.logging.Logger;
import com.github.lkq.maven.plugin.deploydeps.report.Reporter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.util.Arrays;

@Mojo(name = "dry-run", requiresDependencyResolution = ResolutionScope.RUNTIME)
public class DryRunMojo extends AbstractDeployDepsMojo {
    @Override
    protected CompositeDeployer createDeployer(Reporter reporter) throws MojoExecutionException {
        return new CompositeDeployer(Arrays.asList((Deployer) (localRepoPath, repoArtifactPath) -> {
            Logger.get().info("dry run, not deploying " + repoArtifactPath);
            return true;
        }), reporter);
    }
}

package com.github.lkq.maven.plugin.deploydeps;

import com.github.lkq.maven.plugin.deploydeps.deployer.CompositeDeployer;
import com.github.lkq.maven.plugin.deploydeps.deployer.Deployer;
import com.github.lkq.maven.plugin.deploydeps.report.Reporter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.util.ArrayList;
import java.util.List;

@Mojo(name = "deploy-deps", requiresDependencyResolution = ResolutionScope.RUNTIME)
public class DeployDepsMojo extends AbstractDeployDepsMojo {

    protected CompositeDeployer createDeployer(Reporter reporter) throws MojoExecutionException {
        CompositeDeployer artifactDeployer;List<Deployer> deployers = new ArrayList<>();
        try {
            deployers.addAll(deployerFactory.create(this.deployers));
            deployers.addAll(deployerFactory.create(this.customDeployers, project.getBuild().getOutputDirectory()));
            artifactDeployer = new CompositeDeployer(deployers, reporter);
        } catch (Throwable e) {
            throw new MojoExecutionException("failed to create deployer", e);
        }
        if (deployers.size() <= 0) {
            throw new MojoExecutionException("no available deployer");
        }
        return artifactDeployer;
    }
}

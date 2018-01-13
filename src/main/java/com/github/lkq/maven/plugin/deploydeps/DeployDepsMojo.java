package com.github.lkq.maven.plugin.deploydeps;

import com.github.lkq.maven.plugin.deploydeps.artifact.ArtifactCollector;
import com.github.lkq.maven.plugin.deploydeps.deployer.CompositeDeployer;
import com.github.lkq.maven.plugin.deploydeps.deployer.Deployer;
import com.github.lkq.maven.plugin.deploydeps.deployer.DeployerFactory;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Mojo(name = "deploy-deps", requiresDependencyResolution = ResolutionScope.RUNTIME)
public class DeployDepsMojo extends AbstractMojo {


    @Parameter
    List<DefaultConfig> deployers;
    @Parameter
    List<CustomConfig> customDeployers;
    @Parameter
    private boolean dryRun;


    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;
    @Parameter(defaultValue = "${localRepository}", readonly = true)
    private ArtifactRepository localRepository;
    @Parameter(defaultValue = "${project.remoteArtifactRepositories}", readonly = true)
    private List remoteArtifactRepositories;


    @Component
    private ArtifactFactory artifactFactory;
    @Component
    private ArtifactMetadataSource artifactMetadataSource;
    @Component
    private ArtifactResolver artifactResolver;

    private final DeployerFactory deployerFactory;
    private final DefaultConfigProcessor configProcessor;

    public DeployDepsMojo() {
        deployerFactory = new DeployerFactory();
        configProcessor = new DefaultConfigProcessor();
    }

    public void execute() throws MojoExecutionException, MojoFailureException {

        Log logger = getLog();
        logInfo(logger);

        try {
            configProcessor.process(deployers);
        } catch (Throwable t) {
            throw new MojoExecutionException("invalid deployer config", t);
        }

        if (deployers == null || deployers.size() <= 0) {
            if (customDeployers == null || customDeployers.size() <= 0) {
                throw new MojoExecutionException("no deployer config");
            }
        }

        Deployer artifactDeployer;
        if (dryRun) {
            // requires maven-plugin-plugin:3.5, otherwise can not use lambda
            artifactDeployer = (localRepo, artifactPath) -> logger.info("dry run, deploying " + artifactPath);
        } else {
            List<Deployer> deployers = new ArrayList<>();
            try {
                deployers.addAll(deployerFactory.create(this.deployers));
                deployers.addAll(deployerFactory.create(this.customDeployers, project.getBuild().getOutputDirectory()));
                artifactDeployer = new CompositeDeployer(deployers);
            } catch (Throwable e) {
                throw new MojoExecutionException("failed to create deployer", e);
            }
            if (deployers.size() <= 0) {
                throw new MojoExecutionException("no available deployer");
            }
        }

        ArtifactCollector artifactCollector = new ArtifactCollector(artifactFactory,
                artifactMetadataSource,
                artifactResolver,
                localRepository,
                remoteArtifactRepositories);
        List<Artifact> artifacts = artifactCollector.collect(project.getDependencies());
        for (Artifact artifact : artifacts) {
            String repoArtifactPath = localRepository.pathOf(artifact);
            try {
                artifactDeployer.put(localRepository.getBasedir(), repoArtifactPath);
            } catch (IOException e) {
                logger.error("failed to deploy");
            }
        }
    }

    private void logInfo(Log logger) {

        logger.debug("project: " + project);
        logger.info("localRepository: " + localRepository);
        logger.info("remoteArtifactRepositories: " + remoteArtifactRepositories);

        logger.info("artifacts to deploy: " + project.getDependencyArtifacts());

        logger.info("dry run:  " + dryRun);

    }

}

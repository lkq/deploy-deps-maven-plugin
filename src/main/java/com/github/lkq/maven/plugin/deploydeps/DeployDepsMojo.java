package com.github.lkq.maven.plugin.deploydeps;

import com.github.lkq.maven.plugin.deploydeps.deployer.CompositeDeployer;
import com.github.lkq.maven.plugin.deploydeps.deployer.Deployer;
import com.github.lkq.maven.plugin.deploydeps.deployer.DeployerFactory;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.model.Dependency;
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

    private final DeployerFactory deployerFactory = new DeployerFactory();

    @Parameter
    List<DefaultDeployer> deployers;
    @Parameter
    List<CustomDeployer> customDeployers;
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

    public void execute() throws MojoExecutionException, MojoFailureException {

        Log logger = getLog();
        logInfo(logger);

        Deployer artifactDeployer;
        if (dryRun) {
            // requires maven-plugin-plugin:3.5, otherwise can not use lambda
            artifactDeployer = (localFile, artifactPath) -> logger.info("dry run, deploying " + localFile);
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

        List<Dependency> dependencies = project.getDependencies();
        for (Dependency dependency : dependencies) {
            logger.info("dependency: " + dependency);
            VersionRange versionRange = null;
            try {
                versionRange = VersionRange.createFromVersionSpec(dependency.getVersion());
            } catch (InvalidVersionSpecificationException e) {
                String error = "invalid version spec: " + dependency.getVersion();
                logger.error(error, e);
                throw new RuntimeException(error, e);
            }
            Artifact dependencyArtifact = artifactFactory.createDependencyArtifact(dependency.getGroupId(), dependency.getArtifactId(), versionRange, dependency.getType(), dependency.getClassifier(), dependency.getScope());
            try {
                List<ArtifactVersion> availableVersions = artifactMetadataSource.retrieveAvailableVersions(dependencyArtifact, localRepository, remoteArtifactRepositories);
                logger.info("found available versions for " + dependencyArtifact.getArtifactId() + ": " + availableVersions);
                for (ArtifactVersion version : availableVersions) {
                    if (versionRange.containsVersion(version)) {

                        Artifact artifact = artifactFactory.createArtifactWithClassifier(dependency.getGroupId(), dependency.getArtifactId(), version.toString(), dependency.getType(), dependency.getClassifier());
                        try {
                            artifactResolver.resolve(artifact, remoteArtifactRepositories, localRepository);
                        } catch (ArtifactResolutionException e) {
                            logger.info("failed to resolve artifact:" + artifact, e);
                        } catch (ArtifactNotFoundException e) {
                            logger.info("artifact not found: " + artifact, e);
                        }
                        String repoArtifactPath = localRepository.pathOf(artifact);
                        try {
                            logger.info("deploying " + repoArtifactPath);
                            artifactDeployer.put(localRepository.getBasedir(), repoArtifactPath);
                        } catch (IOException e) {
                            logger.error("failed to transfer file", e);
                        }
                    }
                }

            } catch (Exception e) {
                logger.error("failed to deploy dependencies", e);
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

package com.github.lkq.maven.plugin.deploydeps;

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

import java.io.File;
import java.io.IOException;
import java.util.List;

@Mojo(name = "deploy-deps", requiresDependencyResolution = ResolutionScope.RUNTIME)
public class DeployDepsMojo extends AbstractMojo {

    private final Log logger = getLog();

    private final DeployerFactory deployerFactory = new DeployerFactory();

    @Parameter
    private String targetRepository;
    @Parameter
    private String targetFileMode;
    @Parameter
    DeployerConfig deployer;
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

        logDebugInfo();

        Deployer artifactDeployer = null;
        if (dryRun) {
            // requires maven-plugin-plugin:3.5, otherwise can not use lambda
            artifactDeployer = (localFile, remotePath, mode) -> logger.info("dry run, not putting, from [" + localFile + "] to [" + remotePath + "], mode=" + mode);
        } else {
            artifactDeployer = deployerFactory.create(deployer, project.getBuild().getOutputDirectory());
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
                throw new RuntimeException(error);
            }
            Artifact dependencyArtifact = artifactFactory.createDependencyArtifact(dependency.getGroupId(), dependency.getArtifactId(), versionRange, dependency.getType(), dependency.getClassifier(), dependency.getScope());
            try {
                List<ArtifactVersion> availableVersions = artifactMetadataSource.retrieveAvailableVersions(dependencyArtifact, localRepository, remoteArtifactRepositories);
                logger.info("available versions for " + dependencyArtifact.getArtifactId() + ": " + availableVersions);
                for (ArtifactVersion version : availableVersions) {
                    if (versionRange.containsVersion(version)) {

                        Artifact artifact = artifactFactory.createArtifactWithClassifier(dependency.getGroupId(), dependency.getArtifactId(), version.toString(), dependency.getType(), dependency.getClassifier());
                        logger.info("artifact: " + artifact);
                        try {
                            artifactResolver.resolveAlways(artifact, remoteArtifactRepositories, localRepository);
                        } catch (ArtifactResolutionException e) {
                            logger.info("failed to resolve artifact");
                        } catch (ArtifactNotFoundException e) {
                            logger.info("artifact not found");
                        }
                        String artifactPath = localRepository.pathOf(artifact);
                        String localPath = localRepository.getBasedir() + File.separator + artifactPath;
                        String remotePath = new File(artifactPath).getPath();
                        logger.info("local path:" + localPath);
                        logger.info("remote path:" + remotePath);
                        try {
                            logger.info("copying from " + localPath + " to " + remotePath);
                            artifactDeployer.put(localPath, remotePath, targetFileMode);
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

    private void logDebugInfo() {

        logger.debug("project: " + project);
        logger.info("localRepository: " + localRepository);
        logger.info("remoteArtifactRepositories: " + remoteArtifactRepositories);

        logger.info("artifacts to deploy: " + project.getDependencyArtifacts());

        logger.info("target repository:  " + targetRepository);
        logger.info("target file mode:  " + targetFileMode);
        logger.info("deployer config:  " + deployer);
        logger.info("dry run:  " + dryRun);

    }

}

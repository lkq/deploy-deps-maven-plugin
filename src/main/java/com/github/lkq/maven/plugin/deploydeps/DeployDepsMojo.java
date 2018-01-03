package com.github.lkq.maven.plugin.deploydeps;

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
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Mojo(name = "deploy-deps")
public class DeployDepsMojo extends AbstractMojo {

    private final Log logger = getLog();
    private final DeployerFactory deployerFactory = new DeployerFactory();

    @Parameter
    private DeployerConfig deployerConfig;
    @Parameter
    private String targetPath;

    @Component
    private ArtifactFactory artifactFactory;

    @Parameter( defaultValue = "${localRepository}", readonly = true )
    protected ArtifactRepository localRepository;

    @Parameter( defaultValue = "${project.remoteArtifactRepositories}", readonly = true )
    protected List remoteArtifactRepositories;

    @Parameter( defaultValue = "${project}", required = true, readonly = true )
    private MavenProject project;

    @Component
    protected ArtifactMetadataSource artifactMetadataSource;
    @Component
    protected ArtifactResolver artifactResolver;

    public void execute() throws MojoExecutionException, MojoFailureException {

        logger.info("project: " + project);
        logger.info("dependency artifacts: " + project.getDependencyArtifacts());

        logger.info("localRepository: " + localRepository);
        logger.info("remoteArtifactRepositories: " + remoteArtifactRepositories);
        logger.info("artifactResolver: " + artifactResolver);

        logger.info("deployerConfig:  " + deployerConfig);

        Deployer deployer = deployerFactory.create(deployerConfig);

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
                        String remotePath = targetPath  + "/" + artifactPath.substring(0, artifactPath.lastIndexOf('/')) + "/";
                        logger.info("local path:" + localPath);
                        logger.info("remote path:" + remotePath);
                        try {
                            logger.info("copying from " + localPath + " to " + remotePath);
                            deployer.put(localPath, remotePath, "0640");
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

}

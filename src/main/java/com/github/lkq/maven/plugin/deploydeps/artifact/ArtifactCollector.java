package com.github.lkq.maven.plugin.deploydeps.artifact;

import com.github.lkq.maven.plugin.deploydeps.logging.Logger;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataRetrievalException;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.logging.Log;

import java.util.ArrayList;
import java.util.List;

public class ArtifactCollector {

    private ArtifactFactory artifactFactory;
    private ArtifactMetadataSource artifactMetadataSource;
    private ArtifactResolver artifactResolver;
    private final ArtifactRepository localRepository;
    private final List remoteArtifactRepositories;

    public ArtifactCollector(ArtifactFactory artifactFactory,
                             ArtifactMetadataSource artifactMetadataSource,
                             ArtifactResolver artifactResolver,
                             ArtifactRepository localRepository,
                             List remoteArtifactRepositories) {

        this.artifactFactory = artifactFactory;
        this.artifactMetadataSource = artifactMetadataSource;
        this.artifactResolver = artifactResolver;
        this.localRepository = localRepository;
        this.remoteArtifactRepositories = remoteArtifactRepositories;
    }

    public List<Artifact> collect(Dependency dependency) {

        Log logger = Logger.get();
        ArrayList<Artifact> artifacts = new ArrayList<>();

        VersionRange versionRange;
        try {
            versionRange = VersionRange.createFromVersionSpec(dependency.getVersion());
        } catch (InvalidVersionSpecificationException e) {
            String error = "invalid version spec: " + dependency.getVersion();
            logger.error(error, e);
            throw new RuntimeException(error, e);
        }
        Artifact dependencyArtifact = artifactFactory.createDependencyArtifact(dependency.getGroupId(), dependency.getArtifactId(), versionRange, dependency.getType(), dependency.getClassifier(), dependency.getScope());
        List<ArtifactVersion> availableVersions = null;
        try {
            availableVersions = artifactMetadataSource.retrieveAvailableVersions(dependencyArtifact, localRepository, remoteArtifactRepositories);
        } catch (ArtifactMetadataRetrievalException e) {
            throw new RuntimeException("failed to get artifact versions", e);
        }
        logger.info("artifact " + dependencyArtifact.getGroupId() + ":" + dependencyArtifact.getArtifactId() + " available versions: " + availableVersions);
        for (ArtifactVersion version : availableVersions) {
            if (versionRange.containsVersion(version)) {

                Artifact artifact = artifactFactory.createArtifactWithClassifier(dependency.getGroupId(), dependency.getArtifactId(), version.toString(), dependency.getType(), dependency.getClassifier());
                try {
                    artifactResolver.resolve(artifact, remoteArtifactRepositories, localRepository);
                } catch (ArtifactResolutionException e) {
                    logger.warn("failed to resolve artifact:" + artifact, e);
                } catch (ArtifactNotFoundException e) {
                    logger.warn("artifact not found: " + artifact, e);
                }
                artifacts.add(artifact);
            }
        }

        return artifacts;
    }
}

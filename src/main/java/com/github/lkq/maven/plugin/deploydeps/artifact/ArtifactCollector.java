package com.github.lkq.maven.plugin.deploydeps.artifact;

import com.github.lkq.maven.plugin.deploydeps.logging.Logger;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.logging.Log;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.*;
import org.eclipse.aether.version.Version;

import java.util.ArrayList;
import java.util.List;

public class ArtifactCollector {
    private final RepositorySystem repoSystem;
    private final RepositorySystemSession repoSession;
    private final List<RemoteRepository> remoteRepos;

    public ArtifactCollector(RepositorySystem repoSystem, RepositorySystemSession repoSession, List<RemoteRepository> remoteRepos) {

        this.repoSystem = repoSystem;
        this.repoSession = repoSession;
        this.remoteRepos = remoteRepos;
    }

    public List<Artifact> collect(Dependency dependency) throws VersionRangeResolutionException {

        Log logger = Logger.get();

        List<Artifact> artifacts = new ArrayList<>();

        Artifact artifact = new DefaultArtifact(dependency.getGroupId(), dependency.getArtifactId(), dependency.getClassifier(), dependency.getType(), dependency.getVersion());
        VersionRangeRequest request = new VersionRangeRequest(artifact, remoteRepos, null);
        VersionRangeResult versionRangeResult = repoSystem.resolveVersionRange(repoSession, request);
        List<Version> versions = versionRangeResult.getVersions();
        logger.info(artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getVersion() + " available versions: " + versions);

        for (Version version : versions) {
            try {
                ArtifactRequest artifactRequest = new ArtifactRequest(new DefaultArtifact(dependency.getGroupId(), dependency.getArtifactId(), dependency.getClassifier(), dependency.getType(), version.toString()), remoteRepos, null);
                ArtifactResult artifactResult = repoSystem.resolveArtifact(repoSession, artifactRequest);
                artifacts.add(artifactResult.getArtifact());
            } catch (ArtifactResolutionException e) {
                logger.error("failed to resolve artifact", e);
            }
        }
        return artifacts;
    }
}

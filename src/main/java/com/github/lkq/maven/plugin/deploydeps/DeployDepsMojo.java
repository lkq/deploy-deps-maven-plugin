package com.github.lkq.maven.plugin.deploydeps;

import com.github.lkq.maven.plugin.deploydeps.artifact.ArtifactCollector;
import com.github.lkq.maven.plugin.deploydeps.deployer.CompositeDeployer;
import com.github.lkq.maven.plugin.deploydeps.deployer.Deployer;
import com.github.lkq.maven.plugin.deploydeps.deployer.DeployerFactory;
import com.github.lkq.maven.plugin.deploydeps.logging.Logger;
import com.github.lkq.maven.plugin.deploydeps.report.Reporter;
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
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.repository.RemoteRepository;

import java.util.ArrayList;
import java.util.Collections;
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

    @Component()
    private RepositorySystem repoSystem;

    @Parameter(defaultValue = "${repositorySystemSession}")
    private RepositorySystemSession repoSession;

    @Parameter(defaultValue = "${project.remoteProjectRepositories}")
    private List<RemoteRepository> remoteRepos;

    private final DeployerFactory deployerFactory;
    private final DefaultConfigProcessor configProcessor;

    public DeployDepsMojo() {
        deployerFactory = new DeployerFactory();
        configProcessor = new DefaultConfigProcessor();
    }

    public void execute() throws MojoExecutionException, MojoFailureException {

        Logger.init(this);
        Log logger = getLog();

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

        CompositeDeployer artifactDeployer;
        Reporter reporter = new Reporter();
        if (dryRun) {
            logger.info("dry run mode");
            artifactDeployer = new CompositeDeployer(Collections.EMPTY_LIST, reporter);
        } else {
            List<Deployer> deployers = new ArrayList<>();
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
        }

        ArtifactCollector artifactCollector = new ArtifactCollector(repoSystem, repoSession, remoteRepos);

        List<Dependency> dependencies = project.getDependencies();
        LocalRepositoryManager localRepositoryManager = repoSession.getLocalRepositoryManager();
        String baseDir = localRepositoryManager.getRepository().getBasedir().getAbsolutePath();

        for (Dependency dependency : dependencies) {
            try {
                List<Artifact> artifacts = artifactCollector.collect(dependency);
                if (artifacts.size() > 0) {
                    for (Artifact artifact : artifacts) {
                        String repoArtifactPath = localRepositoryManager.getPathForLocalArtifact(artifact);
                        artifactDeployer.put(baseDir, repoArtifactPath);
                    }
                } else {
                    reporter.reportFail(dependency.getArtifactId());
                }
            } catch (Exception e) {
                logger.error("error when deploying: " + dependency.getArtifactId(), e);
                reporter.reportFail(dependency.getArtifactId());
            }
        }

        reporter.print(logger);

        if (reporter.totalFails() > 0) {
            throw new MojoExecutionException("one or more dependencies failed to deploy");
        }
    }
}

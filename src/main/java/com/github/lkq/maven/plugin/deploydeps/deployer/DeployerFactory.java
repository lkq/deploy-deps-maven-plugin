package com.github.lkq.maven.plugin.deploydeps.deployer;

import com.github.lkq.maven.plugin.deploydeps.DeployerConfig;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLClassLoader;

public class DeployerFactory {

    public Deployer create(DeployerConfig config, String projectTargetDirectory) throws MojoExecutionException {
        CompositeDeployer deployer = new CompositeDeployer();
        if (config.getSsh() != null) {
            deployer.with(createSSHDeployer(config.getSsh()));
        }
        if (config.getCustom() != null) {
            deployer.with(createCustomDeployer(config.getCustom(), projectTargetDirectory));
        }
        if (deployer.deployerCount() <= 0) {
            throw new RuntimeException("no deployer available");
        }
        return deployer;
    }

    private Deployer createSSHDeployer(DeployerConfig.SSHConfig config) throws MojoExecutionException {
        try {
            String password = "";
            if (config.getPasswordFile() != null && !"".equals(config.getPasswordFile().trim())) {
                password = FileUtils.readFileToString(new File(config.getPasswordFile()), "UTF-8");
            }
            // TODO: allow passing port number from config
            return new SSHDeployer(config.getHost(), 22, config.getUser(), config.getKeyFile(), password);
        } catch (Exception e) {
            throw new MojoExecutionException("failed to create ssh deployer", e);
        }
    }

    private Deployer createCustomDeployer(DeployerConfig.CustomConfig config, String projectTargetDirectory) throws MojoExecutionException {
        Object target;
        try {
            setupProjectCustomClassLoader(projectTargetDirectory);
            Class<?> clz = Thread.currentThread().getContextClassLoader().loadClass(config.getClassName());
            String[] args = config.getConstructorArgs();
            if (args != null && args.length > 0) {

                Class[] argTypes = new Class[args.length];
                for (int i = 0; i < argTypes.length; i++) {
                    argTypes[i] = String.class;
                }

                Constructor<?> constructor = clz.getConstructor(argTypes);
                target = constructor.newInstance((Object[]) args);
            } else {
                target = clz.newInstance();
            }
        } catch (Exception e) {
            throw new MojoExecutionException("failed to create custom deployer", e);
        }

        InvocationHandler handler = new ProxyDeployerHandler(target);
        return (Deployer) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{Deployer.class}, handler);
    }

    /**
     * Setup class loader which can load the classes from the target project output folder
     *
     * @param projectOutputDirectory
     * @throws MojoExecutionException
     */
    private void setupProjectCustomClassLoader(String projectOutputDirectory) throws MojoExecutionException {
        try {
            URL url = new File(projectOutputDirectory).toURI().toURL();

            URLClassLoader classLoader = new URLClassLoader(new URL[]{url}, getClass().getClassLoader());
            Thread.currentThread().setContextClassLoader(classLoader);
        } catch (Exception e) {
            throw new MojoExecutionException("failed to create classloader for custom deployer", e);
        }
    }
}

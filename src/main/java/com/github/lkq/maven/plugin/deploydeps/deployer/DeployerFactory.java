package com.github.lkq.maven.plugin.deploydeps.deployer;

import ch.ethz.ssh2.Connection;
import com.github.lkq.maven.plugin.deploydeps.CustomConfig;
import com.github.lkq.maven.plugin.deploydeps.DefaultConfig;
import com.github.lkq.maven.plugin.deploydeps.deployer.ssh.SSHClient;
import com.github.lkq.maven.plugin.deploydeps.logging.Logger;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class DeployerFactory {

    public List<Deployer> create(List<DefaultConfig> defaultConfigs) {
        List<Deployer> deployers = new ArrayList<>();
        if (defaultConfigs != null && defaultConfigs.size() > 0) {

            for (DefaultConfig sshConfig : defaultConfigs) {
                Logger.get().info("creating ssh deployer with config: " + sshConfig);
                deployers.add(createSSHDeployer(sshConfig));
            }
        }
        return deployers;
    }

    public List<Deployer> create(List<CustomConfig> customConfigs, String projectTargetDirectory) {
        List<Deployer> deployers = new ArrayList<>();
        if (customConfigs != null && customConfigs.size() > 0) {
            for (CustomConfig customConfig : customConfigs) {
                Logger.get().info("creating custom deployer with config: " + customConfig);
                deployers.add(createCustomDeployer(customConfig, projectTargetDirectory));
            }
        }
        return deployers;
    }

    private Deployer createSSHDeployer(DefaultConfig config) {
        try {
            String password = "";
            if (config.getPasswordFile() != null && !"".equals(config.getPasswordFile().trim())) {
                password = FileUtils.readFileToString(new File(config.getPasswordFile()), "UTF-8");
            }
            Connection connection = new Connection(config.getHost(), Integer.valueOf(config.getPort()));
            connection.connect();
            boolean connected = connection.authenticateWithPublicKey(config.getUser(), new File(config.getKeyFile()), password);
            if (connected) {
                return new SSHDeployer(new SSHClient(connection), config.getTargetPath(), config.getTargetFileMode(), new MD5Checker());
            } else {
                throw new RuntimeException("failed to establish connection:" + config);
            }
        } catch (IOException e) {
            throw new RuntimeException("failed to establish connection:" + config);
        }
    }

    private Deployer createCustomDeployer(CustomConfig config, String projectTargetDirectory) {
        Object target;
        try {
            URLClassLoader classLoader = createTargetProjectClassLoader(projectTargetDirectory);
            Class<?> clz = classLoader.loadClass(config.getClassName());
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
            throw new RuntimeException("failed to create custom deployer", e);
        }

        InvocationHandler handler = new ProxyDeployerHandler(target);
        return (Deployer) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{Deployer.class}, handler);
    }

    /**
     * create class loader which can load the classes from the target project output folder
     *
     * @param projectOutputDirectory
     * @throws MalformedURLException
     */
    private URLClassLoader createTargetProjectClassLoader(String projectOutputDirectory) throws MalformedURLException {
        URL url = new File(projectOutputDirectory).toURI().toURL();
        return new URLClassLoader(new URL[]{url}, getClass().getClassLoader());
    }
}

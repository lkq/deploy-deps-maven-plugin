package com.github.lkq.maven.plugin.deploydeps.deployer;

import com.github.lkq.maven.plugin.deploydeps.DeployerConfig;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;

public class DeployerFactory {
    public Deployer create(DeployerConfig config) {
        try {
            String className = config.getClassName();
            if (className == null || "".equals(className.trim())) {
                return createDefault(config);
            } else {
                return createProxy(config);

            }
        } catch (IOException e) {
            throw new RuntimeException("failed to create deployer", e);
        }
    }

    private Deployer createProxy(DeployerConfig config) {
        Object target;
        try {
            Class<?> clz = Thread.currentThread().getContextClassLoader().loadClass(config.getClassName());
//            Class<?> clz = Class.forName(config.getClassName());
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
        } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException | ClassNotFoundException e) {
            throw new RuntimeException("failed to create deployer", e);
        }

        InvocationHandler handler = new ProxyDeployerHandler(target);
        return (Deployer) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{Deployer.class}, handler);
    }

    private Deployer createDefault(DeployerConfig config) throws IOException {
        String password = "";
        if (config.getPasswordFile() != null && !"".equals(config.getPasswordFile().trim())) {
            password = FileUtils.readFileToString(new File(config.getPasswordFile()), "UTF-8");
        }
        // TODO: allow passing port number from config
        return new SSHDeployer(config.getHost(), 22, config.getUser(), config.getKeyFile(), password);
    }
}

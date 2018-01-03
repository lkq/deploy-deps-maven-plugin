package com.github.lkq.maven.plugin.deploydeps;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class DeployerFactory {
    public Deployer create(DeployerConfig config) {
        try {
            String className = config.getClassName();
            if (className == null || "".equals(className.trim())) {
                String password = "";
                if (config.getPasswordFile() != null && !"".equals(config.getPasswordFile().trim())) {
                    password = FileUtils.readFileToString(new File(config.getPasswordFile()));
                }
                // TODO: allow passing port number from config
                return new SSHDeployer(config.getHost(), 22, config.getUser(), config.getKeyFile(), password);
            } else {
                // TODO: implement later
                return new Deployer() {
                    @Override
                    public void put(String localFile, String remotePath, String mode) {

                    }
                };
            }
        } catch (IOException e) {
            throw new RuntimeException("failed to create deployer", e);
        }
    }
}

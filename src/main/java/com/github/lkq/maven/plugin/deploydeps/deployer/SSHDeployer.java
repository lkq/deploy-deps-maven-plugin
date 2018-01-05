package com.github.lkq.maven.plugin.deploydeps.deployer;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.Session;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;

public class SSHDeployer implements Deployer {

    private final Connection conn;
    private SCPClient client;

    public SSHDeployer(String hostname, int port, String user, String pemFile, String pemFilePwd) throws IOException {
        conn = new Connection(hostname, port);
        conn.connect();
        boolean connected = conn.authenticateWithPublicKey(user, new File(pemFile), pemFilePwd);
        if (connected) {
            client = conn.createSCPClient();
        } else {
            throw new RuntimeException("Authentication failed.");
        }
    }

    public void put(String localFile, String targetDirectory, String mode) throws IOException {
        if(mkdir(targetDirectory)) {
            client.put(localFile, targetDirectory, mode);
        }
    }

    private boolean mkdir(String path) throws IOException {
        Session session = null;
        try {
            session = conn.openSession();
            session.execCommand("mkdir -p " + path);
            String result = IOUtils.toString(session.getStderr());
            if (result == null || "".equals(result.trim())) {
                return true;
            } else {
                return false;
            }
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}

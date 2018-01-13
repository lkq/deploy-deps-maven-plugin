package com.github.lkq.maven.plugin.deploydeps.deployer.ssh;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.Session;
import com.github.lkq.maven.plugin.deploydeps.logging.Logger;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

public class SSHClient {
    private final Connection connection;
    private final SCPClient scp;

    public SSHClient(Connection connection) throws IOException {
        this.connection = connection;
        this.scp = this.connection.createSCPClient();
    }

    public void scp(String source, String targetPath, String mode) throws IOException {
        this.scp.put(source, targetPath, mode);
    }

    public ExecResult execute(String cmd) {
        Session session = null;
        try {
            session = this.connection.openSession();
            session.execCommand(cmd);
            return ExecResult.success(IOUtils.toString(session.getStdout(), "UTF-8"), IOUtils.toString(session.getStderr(), "UTF-8"));
        } catch (IOException e) {
            Logger.get().debug("failed to execute command " + cmd, e);
            return ExecResult.fail();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public boolean mkdir(String path) throws IOException {
        SSHClient.ExecResult result = execute("mkdir -p " + path);
        String error = result.getStderr();
        if (error == null || "".equals(error.trim())) {
            return true;
        } else {
            Logger.get().info("failed to create remote dir, error=" + error + ", path=" + path);
            return false;
        }
    }

    public static class ExecResult {
        private boolean success;
        private String stdout;
        private String stderr;

        public static ExecResult fail() {
            return new ExecResult(false);
        }

        public static ExecResult success(String stdout, String stderr) throws IOException {
            ExecResult execResult = new ExecResult(true);
            execResult.stdout = stdout;
            execResult.stderr = stderr;
            return execResult;
        }

        private ExecResult(boolean success) {
            this.success = success;
        }

        public String getStdout() {
            return stdout;
        }

        public String getStderr() {
            return stderr;
        }

        public boolean isSuccess() {
            return success;
        }

        @Override
        public String toString() {
            return "ExecResult{" +
                    "success=" + success +
                    ", stdout='" + stdout + '\'' +
                    ", stderr='" + stderr + '\'' +
                    '}';
        }
    }
}

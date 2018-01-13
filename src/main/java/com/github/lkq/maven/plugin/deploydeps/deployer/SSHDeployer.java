package com.github.lkq.maven.plugin.deploydeps.deployer;

import com.github.lkq.maven.plugin.deploydeps.deployer.ssh.SSHClient;
import com.github.lkq.maven.plugin.deploydeps.logging.Logger;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.io.IOException;

public class SSHDeployer implements Deployer {

    public static final String TARGET_SYSTEM_FILE_SEPARATOR = "/";

    private Log logger = Logger.get();

    private final SSHClient ssh;
    private final String targetPath;
    private final String targetFileMode;

    private final MD5Checker md5Checker;

    public SSHDeployer(SSHClient ssh, String targetPath, String targetFileMode, MD5Checker md5Checker) throws IOException {
        this.ssh = ssh;
        this.targetPath = targetPath;
        this.md5Checker = md5Checker;
        this.targetFileMode = targetFileMode;
    }

    public boolean put(String localRepoPath, String repoArtifactPath) throws IOException {
        String localFile = new File(localRepoPath, repoArtifactPath).getAbsolutePath();
        String targetFile = targetPath + TARGET_SYSTEM_FILE_SEPARATOR + repoArtifactPath;
        String targetFolder = targetFile.substring(0, targetFile.lastIndexOf('/'));

        if (md5Checker.existsAndMatch(targetFile, localFile, ssh)) {
            logger.debug("file skipped: " + targetFile);
            return false;
        } else {
            ssh.mkdir(targetFolder);
            doPut(localFile, targetFolder, targetFileMode);
            if (!md5Checker.existsAndMatch(targetFile, localFile, ssh)) {
                throw new RuntimeException("remote file corrupted");
            }
            return true;
        }
    }

    private void doPut(String localFile, String targetPath, String mode) throws IOException {
        logger.info("copying file from [" + localFile + "] to [" + targetPath + "], mode=" + mode);
        ssh.scp(localFile, targetPath, mode);
    }
}

package com.github.lkq.maven.plugin.deploydeps.deployer;

import com.github.lkq.maven.plugin.deploydeps.deployer.ssh.SSHClient;
import com.github.lkq.maven.plugin.deploydeps.logging.Logger;
import org.apache.maven.plugin.logging.Log;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SSHDeployer implements Deployer {

    private Log logger = Logger.get();

    private final SSHClient ssh;
    private final String targetPath;
    private final String targetFileMode;

    public SSHDeployer(SSHClient ssh, String targetPath, String targetFileMode) throws IOException {
        this.ssh = ssh;
        this.targetPath = targetPath;
        if (targetFileMode == null || "".equals(targetFileMode.trim())) {
            this.targetFileMode = "640";
        } else {
            this.targetFileMode = targetFileMode;
        }
    }

    public void put(String localRepoPath, String repoArtifactPath) throws IOException {
        String localFile = new File(localRepoPath, repoArtifactPath).getAbsolutePath();
        String targetFile = targetPath + "/" + repoArtifactPath;
        String targetFolder = targetFile.substring(0, targetFile.lastIndexOf('/'));
        if (mkdir(targetFolder)) {
            String remoteMD5 = remoteMD5(targetFile);
            logger.info("md5="+ remoteMD5 + ", file=" + targetFile);
            if ("".equals(remoteMD5)) {
                doPut(localFile, targetFolder, targetFileMode);
            } else {
                String localMD5 = localMD5(localFile);
                logger.info("md5=" + localMD5 + ", file=" + localFile);
                if (!localMD5.equals(remoteMD5)) {
                    doPut(localFile, targetFolder, targetFileMode);
                } else {
                    logger.info("file already exists, skipping... " + targetFile);
                }
            }
        }
    }

    private void doPut(String localFile, String targetPath, String mode) throws IOException {
        logger.info("copying file from [" + localFile + "] to [" + targetPath + "], mode=" + mode);
        ssh.scp(localFile, targetPath, mode);
    }

    private String localMD5(String localFile) throws IOException {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] content = Files.readAllBytes(Paths.get(localFile));
            byte[] digest = md5.digest(content);
            return DatatypeConverter.printHexBinary(digest).toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 is not supported");
        }
    }

    private String remoteMD5(String targetFile) throws IOException {
        // TODO: assuming remote system have md5sum command
        SSHClient.ExecResult result = ssh.execute("md5sum " + targetFile);
        if (result.isSuccess()) {
            return result.getStdout().split(" ")[0].toLowerCase();
        } else {
            return "";
        }
    }

    private boolean mkdir(String path) throws IOException {
        SSHClient.ExecResult result = ssh.execute("mkdir -p " + path);
        String error = result.getStderr();
        if (error == null || "".equals(error.trim())) {
            return true;
        } else {
            logger.info("failed to create remote dir, error=" + error + ", path=" + path);
            return false;
        }
    }
}

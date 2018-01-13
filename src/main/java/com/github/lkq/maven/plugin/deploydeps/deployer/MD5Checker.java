package com.github.lkq.maven.plugin.deploydeps.deployer;

import com.github.lkq.maven.plugin.deploydeps.deployer.ssh.SSHClient;
import com.github.lkq.maven.plugin.deploydeps.logging.Logger;
import org.apache.maven.plugin.logging.Log;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Checker {

    public boolean existsAndMatch(String remoteFile, String localFile, SSHClient ssh) throws IOException {

        Log logger = Logger.get();

        String remoteMD5 = remoteMD5(remoteFile, ssh);

        if ("".equals(remoteMD5)) {
            return false;
        } else {
            String localMD5 = localMD5(localFile);
            logger.debug("remote file md5="+ remoteMD5 + ", file=" + remoteFile);
            logger.debug("local file md5=" + localMD5 + ", file=" + localFile);
            return localMD5.equals(remoteMD5);
        }
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

    private String remoteMD5(String targetFile, SSHClient ssh) throws IOException {
        // TODO: assuming remote system have md5sum command
        SSHClient.ExecResult result = ssh.execute("md5sum " + targetFile);
        if (result.isSuccess()) {
            return result.getStdout().split(" ")[0].toLowerCase();
        } else {
            return "";
        }
    }
}

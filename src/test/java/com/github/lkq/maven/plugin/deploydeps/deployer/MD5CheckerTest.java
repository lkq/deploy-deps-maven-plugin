package com.github.lkq.maven.plugin.deploydeps.deployer;

import com.github.lkq.maven.plugin.deploydeps.deployer.ssh.SSHClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.xml.bind.DatatypeConverter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class MD5CheckerTest {

    private MD5Checker checker;
    @Mock
    private SSHClient ssh;

    @Before
    public void setUp() throws Exception {
        checker = new MD5Checker();
    }

    @Test
    public void willReturnFalseIfRemoteFileNotExist() throws Exception {

        SSHClient.ExecResult md5Res = SSHClient.ExecResult.success("", "md5sum: /remote/file: No such file or directory");
        given(ssh.execute("md5sum /remote/file")).willReturn(md5Res);

        assertFalse(checker.existsAndMatch("/remote/file", "", ssh));
    }

    @Test
    public void willReturnTrueIfFileExistWithSameMD5() throws Exception {

        URL localFileURL = this.getClass().getClassLoader().getResource("com/github/lkq/some-file");
        String localFile = localFileURL.getPath();
        String localFileMD5 = DatatypeConverter.printHexBinary(MessageDigest.getInstance("MD5").digest(Files.readAllBytes(Paths.get(localFile))));

        SSHClient.ExecResult md5Res = SSHClient.ExecResult.success(localFileMD5 +" /remote/com/github/lkq/some-file", "");
        given(ssh.execute("md5sum /remote/com/github/lkq/some-file")).willReturn(md5Res);

        assertTrue(checker.existsAndMatch("/remote/com/github/lkq/some-file", localFile, ssh));

    }

    @Test
    public void willReturnFalseIfFileExistWithDifferentMD5() throws Exception {

        URL localFileURL = this.getClass().getClassLoader().getResource("com/github/lkq/some-file");
        String localFile = localFileURL.getPath();

        SSHClient.ExecResult md5Res = SSHClient.ExecResult.success("abcde12345 /remote/com/github/lkq/some-file", "");
        given(ssh.execute("md5sum /remote/com/github/lkq/some-file")).willReturn(md5Res);

        assertFalse(checker.existsAndMatch("/remote/com/github/lkq/some-file", localFile, ssh));

    }
}
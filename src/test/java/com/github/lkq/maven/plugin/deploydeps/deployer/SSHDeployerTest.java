package com.github.lkq.maven.plugin.deploydeps.deployer;

import ch.ethz.ssh2.Session;
import com.github.lkq.maven.plugin.deploydeps.deployer.ssh.SSHClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SSHDeployerTest {

    private SSHDeployer deployer;

    @Mock
    private SSHClient ssh;
    @Mock
    private Session mkdirSession;
    @Mock
    private Session md5Session;

    @Before
    public void setUp() throws Exception {
        deployer = new SSHDeployer(ssh, "/remote", "0740");
    }

    @Test
    public void canPutIfRemoteFileNotExist() throws Exception {
        // mkdir success
        given(mkdirSession.getStdout()).willReturn(new ByteArrayInputStream("".getBytes()));
        given(mkdirSession.getStderr()).willReturn(new ByteArrayInputStream("".getBytes()));
        SSHClient.ExecResult mkdirRes = SSHClient.ExecResult.success(mkdirSession);
        given(ssh.execute("mkdir -p /remote/com/github/lkq")).willReturn(mkdirRes);

        // get remote file md5
        given(md5Session.getStdout()).willReturn(new ByteArrayInputStream("".getBytes()));
        given(md5Session.getStderr()).willReturn(new ByteArrayInputStream("".getBytes()));
        SSHClient.ExecResult md5Res = SSHClient.ExecResult.success(md5Session);
        given(ssh.execute("md5sum /remote/com/github/lkq/some-file")).willReturn(md5Res);

        deployer.put("/repo", "com/github/lkq/some-file");

        verify(ssh, times(1)).scp("/repo/com/github/lkq/some-file", "/remote/com/github/lkq", "0740");
    }

    @Test
    public void willNotPutIfMkdirFailed() throws Exception {
        // mkdir success
        given(mkdirSession.getStdout()).willReturn(new ByteArrayInputStream("".getBytes()));
        given(mkdirSession.getStderr()).willReturn(new ByteArrayInputStream("error".getBytes()));
        SSHClient.ExecResult success = SSHClient.ExecResult.success(mkdirSession);
        given(ssh.execute("mkdir -p /remote/com/github/lkq")).willReturn(success);

        deployer.put("/repo", "com/github/lkq/some-file");

        verify(ssh, never()).scp(anyString(), anyString(), anyString());
    }

    @Test
    public void willSkipIfFileExistWithSameMD5() throws Exception {
        // mkdir success
        given(mkdirSession.getStdout()).willReturn(new ByteArrayInputStream("".getBytes()));
        given(mkdirSession.getStderr()).willReturn(new ByteArrayInputStream("".getBytes()));
        SSHClient.ExecResult mkdirRes = SSHClient.ExecResult.success(mkdirSession);
        given(ssh.execute("mkdir -p /remote/com/github/lkq")).willReturn(mkdirRes);

        // get remote file md5, return same as local_file
        String localRepo = this.getClass().getClassLoader().getResource(".").getPath();
        URL localFileURL = this.getClass().getClassLoader().getResource("com/github/lkq/some-file");
        String localFile = localFileURL.getPath();

        String md5 = DatatypeConverter.printHexBinary(MessageDigest.getInstance("MD5").digest(Files.readAllBytes(Paths.get(localFile))));
        given(md5Session.getStdout()).willReturn(new ByteArrayInputStream(md5.getBytes()));
        given(md5Session.getStderr()).willReturn(new ByteArrayInputStream("".getBytes()));
        SSHClient.ExecResult md5Res = SSHClient.ExecResult.success(md5Session);
        given(ssh.execute("md5sum /remote/com/github/lkq/some-file")).willReturn(md5Res);

        deployer.put(localRepo, "com/github/lkq/some-file");

        verify(ssh, never()).scp(anyString(), anyString(), anyString());
    }

    @Test
    public void willOverwriteIfFileExistWithDifferentMD5() throws Exception {
        // mkdir success
        given(mkdirSession.getStdout()).willReturn(new ByteArrayInputStream("".getBytes()));
        given(mkdirSession.getStderr()).willReturn(new ByteArrayInputStream("".getBytes()));
        SSHClient.ExecResult mkdirRes = SSHClient.ExecResult.success(mkdirSession);
        given(ssh.execute("mkdir -p /remote/com/github/lkq")).willReturn(mkdirRes);

        // get remote file md5
        given(md5Session.getStdout()).willReturn(new ByteArrayInputStream("asdf".getBytes()));
        given(md5Session.getStderr()).willReturn(new ByteArrayInputStream("".getBytes()));
        SSHClient.ExecResult md5Res = SSHClient.ExecResult.success(md5Session);
        given(ssh.execute("md5sum /remote/com/github/lkq/some-file")).willReturn(md5Res);

        String localRepo = this.getClass().getClassLoader().getResource(".").getPath();
        URL localFileURL = this.getClass().getClassLoader().getResource("com/github/lkq/some-file");
        String localFile = localFileURL.getPath();

        deployer.put(localRepo, "com/github/lkq/some-file");

        verify(ssh, times(1)).scp(localFile, "/remote/com/github/lkq", "0740");
    }
}
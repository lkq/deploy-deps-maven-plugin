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
        deployer = new SSHDeployer(ssh);
    }

    @Test
    public void canPutIfRemoteFileNotExist() throws Exception {
        // mkdir success
        given(mkdirSession.getStdout()).willReturn(new ByteArrayInputStream("".getBytes()));
        given(mkdirSession.getStderr()).willReturn(new ByteArrayInputStream("".getBytes()));
        SSHClient.ExecResult mkdirRes = SSHClient.ExecResult.success(mkdirSession);
        given(ssh.execute("mkdir -p remote")).willReturn(mkdirRes);

        // get remote file md5
        given(md5Session.getStdout()).willReturn(new ByteArrayInputStream("".getBytes()));
        given(md5Session.getStderr()).willReturn(new ByteArrayInputStream("".getBytes()));
        SSHClient.ExecResult md5Res = SSHClient.ExecResult.success(md5Session);
        given(ssh.execute("md5sum remote/file")).willReturn(md5Res);

        deployer.put("local/file", "remote", "0640");

        verify(ssh, times(1)).scp("local/file", "remote", "0640");
    }

    @Test
    public void willNotPutIfMkdirFailed() throws Exception {
        // mkdir success
        given(mkdirSession.getStdout()).willReturn(new ByteArrayInputStream("".getBytes()));
        given(mkdirSession.getStderr()).willReturn(new ByteArrayInputStream("error".getBytes()));
        SSHClient.ExecResult success = SSHClient.ExecResult.success(mkdirSession);
        given(ssh.execute("mkdir -p remote")).willReturn(success);

        deployer.put("local", "remote", "0640");

        verify(ssh, never()).scp(anyString(), anyString(), anyString());
    }

    @Test
    public void willSkipIfFileExistWithSameMD5() throws Exception {
        // mkdir success
        given(mkdirSession.getStdout()).willReturn(new ByteArrayInputStream("".getBytes()));
        given(mkdirSession.getStderr()).willReturn(new ByteArrayInputStream("".getBytes()));
        SSHClient.ExecResult mkdirRes = SSHClient.ExecResult.success(mkdirSession);
        given(ssh.execute("mkdir -p remote")).willReturn(mkdirRes);

        // get remote file md5, return same as local_file
        URL localFileURL = this.getClass().getClassLoader().getResource("local_file.txt");
        String localFile = localFileURL.getPath();

        String md5 = DatatypeConverter.printHexBinary(MessageDigest.getInstance("MD5").digest(Files.readAllBytes(Paths.get(localFile))));
        given(md5Session.getStdout()).willReturn(new ByteArrayInputStream(md5.getBytes()));
        given(md5Session.getStderr()).willReturn(new ByteArrayInputStream("".getBytes()));
        SSHClient.ExecResult md5Res = SSHClient.ExecResult.success(md5Session);
        given(ssh.execute("md5sum remote/local_file.txt")).willReturn(md5Res);

        deployer.put(localFile, "remote", "0640");

        verify(ssh, never()).scp(anyString(), anyString(), anyString());
    }

    @Test
    public void willOverwriteIfFileExistWithDifferentMD5() throws Exception {
        // mkdir success
        given(mkdirSession.getStdout()).willReturn(new ByteArrayInputStream("".getBytes()));
        given(mkdirSession.getStderr()).willReturn(new ByteArrayInputStream("".getBytes()));
        SSHClient.ExecResult mkdirRes = SSHClient.ExecResult.success(mkdirSession);
        given(ssh.execute("mkdir -p remote")).willReturn(mkdirRes);

        // get remote file md5
        given(md5Session.getStdout()).willReturn(new ByteArrayInputStream("asdf".getBytes()));
        given(md5Session.getStderr()).willReturn(new ByteArrayInputStream("".getBytes()));
        SSHClient.ExecResult md5Res = SSHClient.ExecResult.success(md5Session);
        given(ssh.execute("md5sum remote/local_file.txt")).willReturn(md5Res);

        URL localFileURL = this.getClass().getClassLoader().getResource("local_file.txt");
        String localFile = localFileURL.getPath();

        deployer.put(localFile, "remote", "0640");

        verify(ssh, times(1)).scp(localFile, "remote", "0640");
    }
}
package com.github.lkq.maven.plugin.deploydeps.deployer;

import com.github.lkq.maven.plugin.deploydeps.deployer.ssh.SSHClient;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SSHDeployerTest {

    private SSHDeployer deployer;

    @Mock
    private SSHClient ssh;
    @Mock
    private MD5Checker md5Checker;

    @Before
    public void setUp() throws Exception {
        deployer = new SSHDeployer(ssh, "/remote", "0740", md5Checker);
    }

    @Test
    public void willOverwriteIfNotExistOrMD5IsDifferent() throws Exception {
        given(md5Checker.existsAndMatch("/remote/com/github/lkq/some-file", "/local/com/github/lkq/some-file", ssh)).willReturn(false).willReturn(true);
        deployer.put("/local", "com/github/lkq/some-file");

        verify(ssh, times(1)).scp("/local/com/github/lkq/some-file", "/remote/com/github/lkq", "0740");
    }

    @Test
    public void willThrowExceptionIfDifferentMD5AfterPut() throws Exception {
        given(md5Checker.existsAndMatch("/remote/com/github/lkq/some-file", "/local/com/github/lkq/some-file", ssh)).willReturn(false).willReturn(false);

        String reason = null;
        try {
            deployer.put("/local", "com/github/lkq/some-file");
        } catch (Exception e) {
            reason = e.getMessage();
        }

        assertThat(reason, is("remote file corrupted"));
        verify(ssh, times(1)).scp("/local/com/github/lkq/some-file", "/remote/com/github/lkq", "0740");
    }

    @Test
    @Ignore("need enhancement")
    public void willNotPutIfMkdirFailed() throws Exception {
        given(ssh.mkdir("/remote/com/github/lkq")).willReturn(false);

        deployer.put("/repo", "com/github/lkq/some-file");

        verify(ssh, never()).scp(anyString(), anyString(), anyString());
    }

    @Test
    public void willNotPutIfFileExistWithSameMD5() throws Exception {

        given(md5Checker.existsAndMatch("/remote/com/github/lkq/some-file", "/local/com/github/lkq/some-file", ssh)).willReturn(true).willReturn(true);

        deployer.put("/local", "com/github/lkq/some-file");

        verify(ssh, never()).scp(anyString(), anyString(), anyString());
    }
}
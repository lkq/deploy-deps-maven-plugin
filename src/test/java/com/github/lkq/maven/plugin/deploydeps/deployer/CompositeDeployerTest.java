package com.github.lkq.maven.plugin.deploydeps.deployer;

import com.github.lkq.maven.plugin.deploydeps.report.Reporter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class CompositeDeployerTest {

    CompositeDeployer deployer;
    @Mock
    private Reporter reporter;
    @Mock
    private Deployer deployer1;
    @Mock
    private Deployer deployer2;

    @Test
    public void canCallAllDeployers() throws Exception {

        given(deployer1.put(anyString(), anyString())).willReturn(true);
        given(deployer2.put(anyString(), anyString())).willReturn(true);

        deployer = new CompositeDeployer(Arrays.asList(deployer1, deployer2), reporter);

        deployer.put("/local", "com/github/lkq/some-file");

        verify(deployer1, times(1)).put("/local", "com/github/lkq/some-file");
        verify(deployer2, times(1)).put("/local", "com/github/lkq/some-file");
        verify(reporter, times(2)).reportSuccess("com/github/lkq/some-file");
    }

    @Test
    public void willContinueIfOneOfTheDeployerSkipped() throws Exception {
        given(deployer1.put(anyString(), anyString())).willReturn(false);
        given(deployer2.put(anyString(), anyString())).willReturn(true);

        deployer = new CompositeDeployer(Arrays.asList(deployer1, deployer2), reporter);

        deployer.put("/local", "com/github/lkq/some-file");

        verify(deployer1, times(1)).put("/local", "com/github/lkq/some-file");
        verify(deployer2, times(1)).put("/local", "com/github/lkq/some-file");
        verify(reporter, times(1)).reportSuccess("com/github/lkq/some-file");
        verify(reporter, times(1)).reportSkipped("com/github/lkq/some-file");
    }

    @Test
    public void willContinueIfOneOfTheDeployerFail() throws Exception {
        willThrow(new RuntimeException("Mock Error")).given(deployer1).put(anyString(), anyString());
        given(deployer2.put(anyString(), anyString())).willReturn(true);

        deployer = new CompositeDeployer(Arrays.asList(deployer1, deployer2), reporter);

        deployer.put("/local", "com/github/lkq/some-file");

        verify(deployer1, times(1)).put("/local", "com/github/lkq/some-file");
        verify(deployer2, times(1)).put("/local", "com/github/lkq/some-file");
        verify(reporter, times(1)).reportSuccess("com/github/lkq/some-file");
        verify(reporter, times(1)).reportFail("com/github/lkq/some-file");
    }
}
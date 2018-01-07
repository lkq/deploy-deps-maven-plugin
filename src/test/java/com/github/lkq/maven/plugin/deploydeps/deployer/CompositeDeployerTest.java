package com.github.lkq.maven.plugin.deploydeps.deployer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class CompositeDeployerTest {

    CompositeDeployer deployer;
    @Mock
    private Deployer deployer1;
    @Mock
    private Deployer deployer2;

    @Test
    public void canCallAllDeployers() throws Exception {
        deployer = new CompositeDeployer();
        deployer.with(deployer1).with(deployer2);

        deployer.put("local", "remote", "mode");

        verify(deployer1, times(1)).put("local", "remote", "mode");
        verify(deployer1, times(1)).put("local", "remote", "mode");
    }
}
package com.github.lkq.maven.plugin.deploydeps.deployer;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.Proxy;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ProxyDeployerHandlerTest {

    ProxyDeployerHandler handler;
    private DummyDeployer deployer;

    @Before
    public void setUp() throws Exception {
        deployer = new DummyDeployer("user", "host");
        handler = new ProxyDeployerHandler(deployer);
    }

    @Test
    public void canInvokePut() throws Throwable {
        Deployer proxy = (Deployer) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[]{Deployer.class}, handler);
        proxy.put("localPath", "remotePath", "0640");

        assertThat(deployer.localFile, is("localPath"));
        assertThat(deployer.remotePath, is("remotePath"));
        assertThat(deployer.mode, is("0640"));
    }
}
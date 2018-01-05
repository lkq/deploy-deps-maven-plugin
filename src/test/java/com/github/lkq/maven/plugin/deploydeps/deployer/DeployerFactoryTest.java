package com.github.lkq.maven.plugin.deploydeps.deployer;

import com.github.lkq.maven.plugin.deploydeps.DeployerConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.Proxy;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class DeployerFactoryTest {

    DeployerFactory factory;

    @Mock
    private DeployerConfig config;

    @Before
    public void setUp() throws Exception {
        factory = new DeployerFactory();
    }

    @Test
    public void canCreateCustomDeployer() throws Exception {
        given(config.getClassName()).willReturn(DummyDeployer.class.getName());
        given(config.getConstructorArgs()).willReturn(new String[]{"user", "host"});
        Deployer deployer = factory.create(config, "target");

        assertTrue(deployer instanceof Proxy);

        deployer.put("local", "remote", "640");

        ProxyDeployerHandler handler = (ProxyDeployerHandler) Proxy.getInvocationHandler(deployer);
        DummyDeployer target = (DummyDeployer) handler.getTarget();

        assertThat(target.user, is("user"));
        assertThat(target.host, is("host"));
        assertThat(target.localFile, is("local"));
        assertThat(target.remotePath, is("remote"));
        assertThat(target.mode, is("640"));

    }
}
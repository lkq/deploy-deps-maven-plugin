package com.github.lkq.maven.plugin.deploydeps.deployer;

import com.github.lkq.maven.plugin.deploydeps.CustomDeployer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class DeployerFactoryTest {

    DeployerFactory factory;

    @Mock
    private CustomDeployer customConfig;

    @Before
    public void setUp() throws Exception {
        factory = new DeployerFactory();
    }

    @Test
    public void canCreateCustomDeployer() throws Exception {
        List<CustomDeployer> customConfigs = Arrays.asList(customConfig);

        given(customConfig.getClassName()).willReturn(DummyDeployer.class.getName());
        given(customConfig.getConstructorArgs()).willReturn(new String[]{"user", "host"});

        List<Deployer> deployers = factory.create(customConfigs, "target");

        assertThat(deployers.size(), is(1));
        Deployer proxyDeployer = deployers.get(0);
        assertTrue(proxyDeployer instanceof Proxy);

        proxyDeployer.put("/local", "com/github/lkq/some-file");

        ProxyDeployerHandler handler = (ProxyDeployerHandler) Proxy.getInvocationHandler(proxyDeployer);
        DummyDeployer target = (DummyDeployer) handler.getTarget();

        assertThat(target.user, is("user"));
        assertThat(target.host, is("host"));
        assertThat(target.localRepoPath, is("/local"));
        assertThat(target.repoArtifactPath, is("com/github/lkq/some-file"));

    }
}
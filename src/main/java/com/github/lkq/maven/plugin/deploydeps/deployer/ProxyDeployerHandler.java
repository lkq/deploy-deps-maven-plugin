package com.github.lkq.maven.plugin.deploydeps.deployer;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ProxyDeployerHandler implements InvocationHandler {

    private Object target;

    public ProxyDeployerHandler(Object target) {

        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Method targetMethod = target.getClass().getMethod(method.getName(), method.getParameterTypes());
        return targetMethod.invoke(target, args);
    }

    public Object getTarget() {
        return target;
    }
}

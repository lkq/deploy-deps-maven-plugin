package com.github.lkq.maven.plugin.deploydeps;

import java.util.Arrays;

public class CustomDeployer {

    private String className;

    private String[] constructorArgs;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String[] getConstructorArgs() {
        return constructorArgs;
    }

    public void setConstructorArgs(String[] constructorArgs) {
        this.constructorArgs = constructorArgs;
    }

    @Override
    public String toString() {
        return "CustomConfig{" +
                "className='" + className + '\'' +
                ", constructorArgs=" + Arrays.toString(constructorArgs) +
                '}';
    }
}

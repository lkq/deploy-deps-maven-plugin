package com.github.lkq.maven.plugin.deploydeps;

public class DeployerStub {
    public DeployerStub(String user, String host) {
        System.out.println("created DeployerStub");
    }

    public void put(String localFile, String remotePath, String mode) {
        System.out.println("stubbed put, from [" + localFile + "] to [" + remotePath + "], mode:" + mode);
    }
}

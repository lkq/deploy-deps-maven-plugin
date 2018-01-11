# Deploy Dependencies Maven Plugin

[![Build Status](https://travis-ci.org/lkq/deploy-deps-maven-plugin.svg?branch=master)](https://travis-ci.org/lkq/deploy-deps-maven-plugin)

If you want to deploy your artifacts to remote hosts, there are already a bunch of solutions, e.g:
- wagon-maven-plugin
- rsync

## Why This Project

1. I'm running a private sonatype OSS instance, and want to use it as golden source
2. I want to deploy artifacts from my OSS repository to aws

#### why not wagon
-- it need to config per project\
-- it is part of the release build, this will be a problem as it may make the release build unstable

#### why not rsync
-- have to pull the individual artifact together by myself

#### why maven plugin
-- the dependencies list is feasible for configuring the artifacts to deploy\
-- maven already know how to pull the latest artifacts from OSS repository\
-- can be run in CI server, e.g Jenkins\


## How to Use


create a pom.xml file as below, with the artifacts you want to deploy in the \<dependencies\> tag
```
<dependencies>
    <dependency>
        <groupId>your group id</groupId>
        <artifactId>your artifact id</artifactId>
        <version>version spec, e.g [0.1.0,)</version>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>com.github.lkq</groupId>
            <artifactId>deploy-deps-maven-plugin</artifactId>
            <version>0.2.0</version>
            <executions>
                <execution>
                    <phase>install</phase>
                    <goals>
                        <goal>deploy-deps</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <targetPath>/home/ec2-user/repository</targetPath>
                <fileMode>0640</fileMode>
                <deployer>
                    <ssh>
                        <user>remote user name</user>
                        <host>remote host</host>
                        <keyFile>your private key - given you should have setup your public key in the remote host</keyFile>
                    </ssh>
                </deployer>
            </configuration>
        </plugin>
</plugins>
</build>
```

then run below command under the same path of your pom.xml
```
mvn install
```

### Version Range
using [version range](http://maven.apache.org/enforcer/enforcer-rules/versionRanges.html) for dependency version will deploy all versions within the range\
e.g an open end version range [0.1.0,) can be resolved to all available versions begin from 0.1.0 to the latest one


## Build
run integration tests
```
mvn -Prun-its
```
release
```
mvn release:clean release:prepare release:perform
```
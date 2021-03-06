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


## Usage

available in central repo

https://repo1.maven.org/maven2/com/github/lkq/deploy-deps-maven-plugin/

create a pom.xml file as below, with the artifacts you want to deploy in the \<dependencies\> tag

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
                <version>1.0.0</version>
                <executions>
                    <execution>
                        <phase>install</phase>
                        <goals>
                            <goal>deploy-deps</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <deployers>
                        <deployer>
                            <user>[required] remote user name</user>
                            <host>[required] remote host</host>
                            <keyFile>[required] your private key - given you should have setup your public key in the remote host</keyFile>
                            <passwordFile>[optional] if your key file is password protected, please provide the password file here</passwordFile>
                            <targetPath>[required] remote parent path, e.g /home/ec2-user/repository</targetPath>
                            <fileMode>[optional] remote file mode, default is 0640</fileMode>
                            <port>[optional] specify an ssh port other than 22</port>
                        </deployer>
                    </deployers>
                </configuration>
            </plugin>
    </plugins>
    </build>


then run below command under the same path of your pom.xml

    mvn install


### Deploy Path
the artifacts will be deployed to target path under the same package structure as your local maven repository.

given your local maven repository: /.m2/repository

and your artifact package: com/github/youracct/maven/plugin/some-plugin.jar

with below targetPath config

    <targetPath>/var/repository</targetPath>

the file will be deployed to

    /var/repository/com/github/youracct/maven/plugin/some-plugin.jar

### Version Range
using [version range](http://maven.apache.org/enforcer/enforcer-rules/versionRanges.html) for dependency version will deploy all versions within the range\
e.g an open end version range [0.1.0,) can be resolved to all available versions begin from 0.1.0 to the latest one


### Custom Deployer
you can use your preferred deploy mechanism, such as ftp by specifying a customDeployers config

    <customDeployers>
        <className>your deployer class</className>
        <constructorArgs>
            <arg>user2</arg>
            <arg>host2</arg>
        </constructorArgs>
    </customDeployers>

your deployer class have to implement below method signature, in order to make the plugin load your deployer class, the plugin need to be execute after compile phase

    boolean put(String localRepoPath, String repoArtifactPath)

where the localRepoPath is the your local maven repository base dir

the repoArtifactPath is your artifact relative path in the repository


### Dry Run
use dry-run to verify the pom without actual deploying

    mvn deploy-deps:dry-run

## Compatibility

requires maven 3.5.2 or above

## Build
run integration tests

    mvn -Prun-its

release

    mvn release:clean release:prepare release:perform

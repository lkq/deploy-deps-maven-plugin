# Deploy Dependencies Maven Plugin

If you want to deploy your artifacts to remote hosts, there are already a bunch of solutions, e.g:
- wagon-maven-plugin
- rsync

## Why This Project

1. I'm running a private sonatype OSS instance, and want to use it as golden source
2. I want to deploy artifacts from my OSS repository to aws

#### wagon
-- it need to config per project\
-- it is part of the release build, this will be a problem as I'm in a network with restricted access to the outside world

#### rsync
-- have to pull the individual artifact together by myself

#### why maven plugin
-- the dependencies list is a good place for configuring the artifacts to deploy\
-- maven already know how to pull the latest artifacts from remote repository\
-- can be run in CI/CD, e.g Jenkins\


you only have to create a dedicated maven project to host the pom.xml with the dependency list and the plugin config

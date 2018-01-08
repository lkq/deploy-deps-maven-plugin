# TODO List

## do not overwrite existing files
if a target file already exists and the checksum is valid, skip the file.\
use config to allow force overwrite
```xml
<forceOverwrite>true</forceOverwrite>
```


## allow custom ssh port
default ssh port is 22, I want to be able to deploy via sshd with custom port

## migrate to maven 3 api
ArtifactResolver is deprecated in maven 3
migrate to use aether to resolve artifacts

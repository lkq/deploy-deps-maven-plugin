# TODO List

## do not overwrite existing files
if a target file already exists and the checksum is valid, skip the file.\
use config to allow force overwrite
```xml
<forceOverwrite>true</forceOverwrite>
```

## can specify target file mode
if target file mode doesn't match with the config, update the target file mode
```xml
<fileMode>640</fileMode>
```

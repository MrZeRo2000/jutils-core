# jutils-core

Small, dependency-free Java library of file, zip, backup and process helpers. Shared by the server (`*-wss`) and Android (`library-*-android`) projects.

- Group / artifact: `com.romanpulov:jutils-core`
- Java 17 source and target level
- No runtime dependencies (logging uses `java.util.logging`)

## Usage

```groovy
dependencies {
    implementation 'com.romanpulov:jutils-core:0.6.0'
}
```

## Packages

### `com.romanpulov.jutilscore.io`

**`FileUtils`**: file name helpers and copy routines.

| Method | Description |
|---|---|
| `copy(src, dst)` | Copies a file, returns `true` on success |
| `copyWithZip(entryName, src, dst)` | Copies a file into a single-entry zip archive |
| `copyStream`, `copyStreamWithZip` | Stream variants of the above |
| `listFiles(folder, mask)` | Lists regular files in a folder matching a mask such as `*.pdf` (`*` is any number of characters, `?` is one character, everything else is literal), no subfolders, order not defined, case-insensitive on Windows. The result can be passed to `ZipFileUtils.zipFiles` |
| `delete(fileName)` | Deletes a file if it exists |
| `getTempFileName`, `getFileNameFromTemp`, `renameTempFile` | Temp file (`.temp`) convention: write to a temp file, then rename it over the real one |
| `removeExtension`, `getExtension`, `isBackupFileName` | File name helpers |
| `saveCopies`, `renameCopies` | Rolling copies of a file (`data.db`, `data.db.bak01`, `data.db.bak02` ...), copy or rename variant |
| `saveListCopies`, `renameListCopies`, `processListCopies` | Same, working from a list of existing file names; `processListCopies` takes a custom `FileProcessor` (used by the Android MediaStore backup) |
| `setFileKeepCopiesCount(n)` | Number of rolling copies kept, default 5, minimum 2 |

**`ZipFileUtils`**: zip and unzip.

| Method | Description |
|---|---|
| `zipFile(path, name)` | Zips one file to `<name>.zip` in the same folder, returns the zip path or `null` |
| `zipFiles(zipFileName, files)` | Zips several files into one archive, each stored under its own file name |
| `zipStream(entryName, in, out)` | Writes one stream as a single zip entry |
| `unZipFile(path, name)` | Extracts the **first** entry of an archive next to it |
| `unZipStream(in, out)` | Extracts the first entry to a stream, returns the entry name |
| `getZipFileName(name)` | `report.txt` becomes `report.zip` |

```java
List<File> pdfs = FileUtils.listFiles("C:/data/reports", "*.pdf");
boolean ok = ZipFileUtils.zipFiles("C:/data/reports.zip", pdfs);
```

`zipFiles` keeps only the file name for each entry, so two files with the same name in different folders collide. Extraction (`unZipFile`, `unZipStream`) only handles the first entry.

### `com.romanpulov.jutilscore.storage`

Zip-based backup and restore of a single data file, with rolling history.

- **`BackupUtils`**: `createRollingLocalBackup(dataFile, backupFolder, backupFile)` shifts existing backups (`x.zip` becomes `x.zip.bak01` and so on, see `setFileKeepCopiesCount`) and writes a new `x.zip`. `restoreBackup(...)` unzips and copies the data back. `getBackupFiles` and `getBackupFileNames` list backups in a folder.
- **`BackupProcessor`**: interface for a backup target (data file, folder, backup name, list, create, restore, open stream).
- **`FileBackupProcessor`**: `BackupProcessor` implementation on the local file system. The Android library `library-common-android` provides a MediaStore implementation.

### `com.romanpulov.jutilscore.process`

**`ProcessExecutor`**: runs an external process with stderr merged into stdout and returns `ExecutionResult(exitCode, output)`. `executeCommand(String...)` runs any command, and `executePowershell(script, args...)` runs a script with `-ExecutionPolicy Bypass -NonInteractive`. Windows only for the PowerShell variant.

## Error handling and logging

Most methods report failure through their return value (`false` or `null`) instead of throwing. The cause is logged through `java.util.logging` at `WARNING` level with the stack trace, logger names being the class names (`com.romanpulov.jutilscore.io.FileUtils` and so on). Configure or route it with the usual JUL mechanisms (`jul-to-slf4j` on SLF4J based apps, `logging.properties` otherwise).

## Android compatibility

The Android consumers have `minSdk` 24 and do not use core library desugaring. The library sticks to `java.io`, `java.util.zip`, `java.util.regex` and `java.util.logging`, so it works on API 24 and above. Avoid `java.nio.file` (`Files`, `Path`, `DirectoryStream`, API 26+) when adding code.

## Build and test

```powershell
./gradlew.ps1 test
```

## Publish to local Maven repository

```powershell
./gradlew.ps1 publishToMavenLocal
```

Consumers pin the version explicitly, so bump `libraryVersion` in `build.gradle` for any change you publish rather than overwriting an existing version.
